package me.bed0.jWynn.api.v2.endpoints;

import com.google.gson.reflect.TypeToken;
import me.bed0.jWynn.api.APIMidpoint;
import me.bed0.jWynn.api.APIRequest;
import me.bed0.jWynn.api.APIResponse;
import me.bed0.jWynn.api.v2.APIResponseV2;
import reactor.core.publisher.Mono;

import static me.bed0.jWynn.WynncraftAPI.GSON;

public class APIV2IngredientList extends APIRequest<String[]> {

    public APIV2IngredientList(String requestURL, APIMidpoint midpoint) {
        super(requestURL, midpoint);
    }

    @Override
    public Mono<APIResponse<String[]>> runIncludeMeta() {
        return getResponse().map(s -> GSON.fromJson(s,  new TypeToken<APIResponseV2<String[]>>() {}.getType()));
    }
}
