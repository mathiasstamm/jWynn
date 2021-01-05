package me.bed0.jWynn.api.v2.endpoints;

import com.google.gson.reflect.TypeToken;
import me.bed0.jWynn.api.APIMidpoint;
import me.bed0.jWynn.api.APIRequest;
import me.bed0.jWynn.api.APIResponse;
import me.bed0.jWynn.api.v2.APIResponseV2;
import me.bed0.jWynn.api.v2.ingredient.WynncraftIngredient;
import reactor.core.publisher.Mono;

import static me.bed0.jWynn.WynncraftAPI.GSON;

public class APIV2IngredientRequest extends APIRequest<WynncraftIngredient[]> {

    public APIV2IngredientRequest(String requestURL, APIMidpoint midpoint) {
        super(requestURL, midpoint);
    }

    @Override
    public Mono<APIResponse<WynncraftIngredient[]>> runIncludeMeta() {
        return getResponse().map(s -> GSON.fromJson(s, new TypeToken<APIResponseV2<WynncraftIngredient[]>>() {}.getType()));
    }
}
