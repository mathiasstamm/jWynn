package me.bed0.jWynn.api;

import io.netty.handler.codec.http.HttpHeaders;
import me.bed0.jWynn.exceptions.*;
import me.bed0.jWynn.util.ReactiveFileUtils;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

public abstract class APIRequest<T> {

    protected String requestURL;
    private final Map<String, String> requestHeaders;
    private String userAgent;
    private int timeout;
    private boolean ignoreRateLimit;
    private File fallbackFile;

    private final APIMidpoint midpoint;

    public APIRequest(String requestURL, APIMidpoint midpoint) {
        this(requestURL, new HashMap<>(), midpoint.getAPIConfig().getDefaultUserAgent(), midpoint.getAPIConfig().getDefaultConnectionTimeout(), midpoint, false, null);
    }

    public APIRequest(String requestURL, Map<String, String> requestHeaders, String userAgent, int timeout, APIMidpoint midpoint, boolean ignoreRateLimit, File fallbackFile) {
        this.requestURL = requestURL;
        this.requestHeaders = requestHeaders;
        this.userAgent = userAgent;
        this.timeout = timeout;
        this.midpoint = midpoint;
        this.ignoreRateLimit = ignoreRateLimit;
        this.fallbackFile = fallbackFile;
    }

    /**
     * Run this request, getting the response data directly (therefore destroying meta data)
     */
    public Mono<T> run() {
        return runIncludeMeta().map(APIResponse::getData);
    }

    /**
     * Run this request, the data returned is wrapped inside a APIResponse object, that also
     * contains the request meta data
     */
    public abstract Mono<APIResponse<T>> runIncludeMeta();

    /**
     * Run this request, getting the response data directly and passing it to the specified
     * consumer. If the request fails, the onFailure consumer will be used instead.
     */
    public void runAsync(Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        run().subscribe(onSuccess, onFailure);
    }

    /**
     * Overload for {@link #runAsync(Consumer, Consumer)} that silently ignores failures
     */
    public void runAsync(Consumer<T> onSuccess) {
        runAsync(onSuccess, ignored -> {});
    }

    /**
     * Run this request, the data returned is wrapped inside a APIResponse object, that also
     * contains the request meta data. Pass to the onSuccess consumer if the request was successful,
     * otherwise the relevant exception will be passed to the onFailure consumer.
     */
    public void runIncludeMetaAsync(Consumer<APIResponse<T>> onSuccess, Consumer<Throwable> onFailure) {
        runIncludeMeta().subscribe(onSuccess, onFailure);
    }

    /**
     * Overload for {@link #runIncludeMetaAsync(Consumer, Consumer)} that silently ignores failures
     */
    public void runIncludeMetaAsync(Consumer<APIResponse<T>> onSuccess) {
        runIncludeMetaAsync(onSuccess, ignore -> {});
    }

    /**
     * When this request is run, the specified HTTP header will be included with the request
     */
    @CheckReturnValue
    public APIRequest<T> withHeader(String header, String value) {
        this.requestHeaders.put(header, value);
        return this;
    }

    /**
     * When this request is run, it will instead be sent to the specified URL
     */
    @CheckReturnValue
    public APIRequest<T> toURL(String requestURL) {
        this.requestURL = requestURL;
        return this;
    }

    /**
     * When this request is run, the user agent the request would normally be made as is
     * replaced with this agent
     */
    @CheckReturnValue
    public APIRequest<T> asAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * When this request is run, instead of timing-out the request with the timeout specified
     * by the API config, instead timeout the request after this amount of time (milliseconds)
     */
    @CheckReturnValue
    public APIRequest<T> withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * When this request is run, don't do any rate limit management. The internal rate limits will
     * not be checked and won't be updated after the response is received
     */
    @CheckReturnValue
    public APIRequest<T> ignoreRateLimit() {
        this.ignoreRateLimit = true;
        return this;
    }

    /**
     * When this request is run, if it is successful, the response data is saved to the specified file,
     * if the request fails, attempt to load the request data stored in that file
     */
    @CheckReturnValue
    public APIRequest<T> fallbackFile(File file) {
        this.fallbackFile = file;
        return this;
    }

    protected Mono<String> getResponse() {
        if (!ignoreRateLimit && midpoint.isRateLimited()) {
            return Mono.error(new APIRateLimitExceededException("Cannot execute request " + requestURL + ", rate limit would be exceeded", midpoint.getRateLimitReset(), false));
        }

        return HttpClient.create()
                .responseTimeout(Duration.of(timeout, ChronoUnit.MILLIS))
                .headers(this::setHeaders)
                .get().uri(requestURL)
                .responseSingle(this::validateResponse)
                // wrap IOExceptions in APIConnectionExceptions
                .onErrorResume(IOException.class, e -> Mono.error(new APIConnectionException(e)))
                .onErrorResume(this::handleErrors);
    }

    private void setHeaders(final HttpHeaders builder) {
        requestHeaders.forEach(builder::set);

        builder.set("user-agent", userAgent);

        if (midpoint.getAPIConfig().hasApiKey()) {
            builder.set("apikey", midpoint.getAPIConfig().getApiKey());
        }
    }

    private Mono<String> validateResponse(final HttpClientResponse response, final ByteBufMono body) {
        final HttpHeaders headers = response.responseHeaders();

        if (midpoint.getAPIConfig().isHandleRatelimits()) {
            updateRateLimit(headers);
        }

        int status = response.status().code();
        switch (status) {
            case 200: // ok
                return body.asString()
                        .filter(Objects::nonNull)
                        .switchIfEmpty(Mono.error(new APIResponseException("No body in request response for " + requestURL, -1)))
                        .filter(s -> Optional.ofNullable(headers.get("content-type")).map(s1 -> s1.contains("application/json")).orElse(false))
                        .switchIfEmpty(Mono.error(new APIResponseException("Unexpected content type (not application/json): " + headers.get("content-type"), -1)))
                        .flatMap(this::validateResponseBody);
            case 429: // too many requests
                long resetTime;
                try {
                    resetTime = Long.parseLong(headers.get("ratelimit-reset")) * 1000 + System.currentTimeMillis();
                } catch (NumberFormatException ignore) {
                    resetTime = -1;
                }
                return Mono.error(new APIRateLimitExceededException("429: Too Many Requests for " + requestURL, resetTime, true));
            case 404: // not found
                return Mono.error(new APIResponseException("404: Not Found for " + requestURL, 404));
            case 503: // service unavailable
                return Mono.error(new APIResponseException("503: Service Unavailable " + requestURL, 503));
            default: // unknown
                return Mono.error(new APIResponseException("Unexpected status code " + status + " returned by API for request " + requestURL, status));
        }
    }

    private Mono<String> validateResponseBody(final String body) {
        if (body.matches("\\{\"message\":\".*\"}")) {
            return Mono.error(new APIResponseException("API error when requesting " + requestURL + ": " + body.split("\"message\":")[1].replace("\"", "").replace("}", ""), -1));
        } else if (body.matches("\\{\"error\":\".*\"}")) {
            return Mono.error(new APIResponseException("API error when requesting " + requestURL + ": " + body.split("\"error\":")[1].replace("\"", "").replace("}", ""), -1));
        }

        return Mono.justOrEmpty(fallbackFile)
                .filter(file -> !file.isDirectory())
                .flatMap(file -> {
                    file.getParentFile().mkdirs();
                    return ReactiveFileUtils.writeStringToFile(fallbackFile, body, StandardCharsets.UTF_8);
                })
                .thenReturn(body);
    }

    private void updateRateLimit(final HttpHeaders headers) {
        try {
            // TODO: Multiple rate limit headers now being returned?
            final long reset = Long.parseLong(headers.get("ratelimit-reset")) * 1000 + System.currentTimeMillis();
            final int limit = headers.getInt("ratelimit-limit");
            final int remaining = headers.getInt("ratelimit-remaining");
            midpoint.updateRateLimit(reset, limit, remaining);
        } catch (NumberFormatException | NullPointerException ignored) {
            midpoint.decrementRateLimit();
        }
    }

    private Mono<String> handleErrors(final Throwable throwable) {
        if (fallbackFile == null) {
            return Mono.error(throwable);
        }

        return ReactiveFileUtils.readFileToString(fallbackFile, StandardCharsets.UTF_8)
                .onErrorResume(__ -> Mono.error(throwable)); // Return the original throwable if file-write fails
    }

}
