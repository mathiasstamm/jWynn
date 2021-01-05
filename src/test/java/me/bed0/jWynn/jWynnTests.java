package me.bed0.jWynn;

import me.bed0.jWynn.api.common.WynncraftProfession;
import me.bed0.jWynn.api.v1.guild.GuildList;
import me.bed0.jWynn.api.v1.guild.WynncraftGuild;
import me.bed0.jWynn.api.v1.item.ItemCategory;
import me.bed0.jWynn.api.v1.item.WynncraftItem;
import me.bed0.jWynn.api.v1.leaderboard.GuildLeaderboardPos;
import me.bed0.jWynn.api.v1.leaderboard.PlayerLeaderboardPos;
import me.bed0.jWynn.api.v1.map.WynncraftMapLocation;
import me.bed0.jWynn.api.v1.network.WynncraftOnlinePlayerSum;
import me.bed0.jWynn.api.v1.network.WynncraftOnlinePlayers;
import me.bed0.jWynn.api.v1.search.StatsSearchResult;
import me.bed0.jWynn.api.v1.territory.WynncraftTerritory;
import me.bed0.jWynn.api.v2.ingredient.WynncraftIngredient;
import me.bed0.jWynn.api.v2.player.WynncraftPlayer;
import me.bed0.jWynn.api.v2.recipe.WynncraftRecipe;
import me.bed0.jWynn.api.v2.recipe.WynncraftRecipeType;
import me.bed0.jWynn.exceptions.APIConnectionException;
import me.bed0.jWynn.exceptions.APIResponseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class jWynnTests {

    private static WynncraftAPI api = new WynncraftAPI();

    // Helpers

    private void assertNotEmpty(Object[] array) {
        assertNotNull(array);
        assertTrue(array.length > 0);
    }

    private void assertNotEmpty(String string) {
        assertNotNull(string);
        assertTrue(string.length() > 0);
    }

    // Tests

    @Test
    void exceptionThrownWhenAPIDown() {
        assertThrows(APIConnectionException.class, () -> api.v1().guildStats("Imperial").toURL("aokegfjkjiadvfjidasnjvsnvjuiqebvhuirqnevjuibqnrecn.com").run().block());
    }

    @Test
    void exceptionThrownWhenNotJSON() {
        assertThrows(APIResponseException.class, () -> api.v1().guildStats("Imperial").toURL("https://example.com").run().block());
    }

    @Test
    void v1GuildStatsSuccessful() {
        api.v1().guildStats("Imperial").run().block();
    }

    @Test
    void v1GuildStatsValid() {
        WynncraftGuild stats = api.v1().guildStats("Imperial").run().block();
        assertNotNull(stats);
        assertNotEmpty(stats.getName());
    }

    @Test
    void v1GuildListSuccessful() {
        api.v1().guildList().run().block();
    }

    @Test
    void v1GuildListValid() {
        GuildList list = api.v1().guildList().run().block();
        assertNotNull(list);
        assertNotEmpty(list.getList());
    }

    @Test
    void v1GuildLeaderboardSuccessful() {
        api.v1().guildLeaderboard().run().block();
    }

    @Test
    void v1GuildLeaderboardValid() {
        GuildLeaderboardPos[] leaderboard = api.v1().guildLeaderboard().run().block();
        assertNotEmpty(leaderboard);

        GuildLeaderboardPos pos = leaderboard[0];
        assertNotEmpty(pos.getName());
    }

    @Test
    void v1ItemDBSuccessful() {
        api.v1().itemDBByCategory(ItemCategory.BOOTS).run().block();
    }

    @Test
    void v1ItemDBValid() {
        WynncraftItem[] items = api.v1().itemDBByCategory(ItemCategory.BOOTS).run().block();
        assertNotEmpty(items);

        WynncraftItem item = items[0];
        assertNotEmpty(item.getType());
    }

    @Test
    void v1MapLocationsSuccessful() {
        api.v1().mapLocations().run().block();
    }

    @Test
    void v1MapLocationsValid() {
        WynncraftMapLocation[] locations = api.v1().mapLocations().run().block();
        assertNotEmpty(locations);

        WynncraftMapLocation location = locations[0];
        assertNotEmpty(location.getName());
    }

    @Test
    void v1OnlinePlayersSuccessful() {
        api.v1().onlinePlayers().run().block();
    }

    @Test
    void v1OnlinePlayersValid() {
        WynncraftOnlinePlayers onlinePlayers = api.v1().onlinePlayers().run().block();
        assertNotNull(onlinePlayers);
        assertNotEmpty(onlinePlayers.getOnlinePlayers());
    }

    @Test
    void v1OnlinePlayerSumSuccessful() {
        api.v1().onlinePlayerSum().run().block();
    }

    @Test
    void v1OnlinePlayerSumValid() {
        WynncraftOnlinePlayerSum sum = api.v1().onlinePlayerSum().run().block();
        assertNotNull(sum);
        assertTrue(sum.getPlayersOnline() >= 0);
    }

    @Test
    void v1PlayerLeaderboardSuccessful() {
        api.v1().playerLeaderboard().run().block();
    }

    @Test
    void v1PlayerLeaderboardValid() {
        PlayerLeaderboardPos[] leaderboard = api.v1().playerLeaderboard().run().block();
        assertNotEmpty(leaderboard);

        PlayerLeaderboardPos pos = leaderboard[0];
        assertNotEmpty(pos.getName());
    }

    @Disabled
    @Test
    void v1StatsSearchSuccessful() {
        api.v1().statsSearch("Test").run().block();
    }

    @Disabled
    @Test
    void v1StatsSearchValid() {
        StatsSearchResult result = api.v1().statsSearch("Test").run().block();
        assertNotNull(result);
        assertNotEmpty(result.getGuilds());
        assertNotEmpty(result.getPlayers());
    }

    @Test
    void v1TerritoryListSuccessful() {
        api.v1().territoryList().run().block();
    }

    @Test
    void v1TerritoryListValid() {
        WynncraftTerritory[] territories = api.v1().territoryList().run().block();
        assertNotEmpty(territories);

        WynncraftTerritory territory = territories[0];
        assertNotEmpty(territory.getName());
    }

    @Test
    void v2IngredientListSuccessful() {
        api.v2().ingredient().list().run().block();
    }

    @Test
    void v2IngredientListValid() {
        String[] ingredients = api.v2().ingredient().list().run().block();
        assertNotEmpty(ingredients);
    }

    @Test
    void v2IngredientRequestSuccessful() {
        api.v2().ingredient().get("Accursed Effigy").run().block();
    }

    @Test
    void v2IngredientRequestValid() {
        WynncraftIngredient[] ingredients = api.v2().ingredient().get("Accursed Effigy").run().block();
        assertNotEmpty(ingredients);

        WynncraftIngredient ingredient = ingredients[0];
        assertNotEmpty(ingredient.getName());
    }

    @Test
    void v2IngredientSearchSuccessful() {
        api.v2().ingredient().search().name("Test").run().block();
    }

    @Test
    void v2IngredientSearchValid() {
        final String NAME = "Elephant Toenail";

        WynncraftIngredient[] ingredients = api.v2().ingredient().search().name(NAME).run().block();
        assertNotEmpty(ingredients);

        WynncraftIngredient ingredient = ingredients[0];
        assertNotEmpty(ingredient.getName());
        assertEquals(ingredient.getName(), NAME);
    }

    @Test
    void v2PlayerStatsSuccessful() {
        api.v2().player().statsUUID("5aa0ae01-8c1b-4e0a-b31b-825389a7cb7b").run().block();
    }

    @Test
    void v2PlayerStatsValid() {
        final String UUID = "5aa0ae01-8c1b-4e0a-b31b-825389a7cb7b";

        WynncraftPlayer[] players = api.v2().player().statsUUID(UUID).run().block();
        assertNotEmpty(players);

        WynncraftPlayer player = players[0];
        assertNotEmpty(player.getUuid());
        assertEquals(player.getUuid(), UUID);
    }

    @Test
    void v2RecipeListSuccessful() {
        api.v2().recipe().list().run().block();
    }

    @Test
    void v2RecipeListValid() {
        String[] recipes = api.v2().recipe().list().run().block();
        assertNotEmpty(recipes);
    }

    @Test
    void v2RecipeSearchSuccessful() {
        api.v2().recipe().search().profession(WynncraftProfession.ALCHEMISM).run().block();
    }

    @Test
    void v2RecipeSearchValid() {
        WynncraftRecipe[] recipes = api.v2().recipe().search().profession(WynncraftProfession.ALCHEMISM).run().block();
        assertNotEmpty(recipes);

        WynncraftRecipe recipe = recipes[0];
        assertNotEmpty(recipe.getId());
    }

    @Test
    void v2RecipeSuccessful() {
        api.v2().recipe().get("Boots-1-3").run().block();
    }

    @Test
    void v2RecipeValid() {
        WynncraftRecipe[] recipes = api.v2().recipe().get("Boots-1-3").run().block();

        WynncraftRecipe recipe = recipes[0];
        assertNotEmpty(recipe.getId());
        assertEquals(recipe.getType(), WynncraftRecipeType.BOOTS);
    }
}
