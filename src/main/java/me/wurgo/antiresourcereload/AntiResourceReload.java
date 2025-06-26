package me.wurgo.antiresourcereload;

import com.google.gson.JsonElement;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.structure.Structure;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AntiResourceReload {
    private static final Logger LOGGER = LogManager.getLogger();

    public static ReloadableResourceManager dataManager;
    public static RecipeManager recipeManager;
    public static RegistryTagManager tagManager;
    public static LootConditionManager predicateManager;
    public static LootManager lootManager;
    public static ServerAdvancementLoader advancementLoader;
    public static CommandFunctionManager commandFunctionManager;
    public static Map<Identifier, JsonElement> recipes;

    public static CommandManager commandManager;

    public static final Map<Identifier, Structure> structures = Collections.synchronizedMap(new HashMap<>());

    public static UserCache userCache;

    public static boolean hasInitializedShapeCache;
    public static boolean hasSeenRecipes;

    public static void log(String message) {
        LOGGER.info("[AntiResourceReload] {}", message);
    }
}
