package me.bed0.jWynn.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlUtil {

    /**
     * Encodes a string for use in an URL, using percent-escaped spaces (%20) instead of a +
     * This "mocks" RFC3986 encoding but doesn't follow the complete standard
     * @param string The string to encode
     * @param encoding The encoding to use (Should be UTF-8)
     * @return The encoded string
     * @throws UnsupportedEncodingException Thrown if the encoding is not supported
     */
    public static String encode(String string, String encoding) throws UnsupportedEncodingException {
        return URLEncoder.encode(string, encoding).replaceAll("\\+", "%20");
    }

}
