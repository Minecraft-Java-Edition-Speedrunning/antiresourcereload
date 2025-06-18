package me.wurgo.antiresourcereload;

import com.google.gson.JsonElement;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.structure.Structure;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AntiResourceReload {
    private static final Logger LOGGER = LogManager.getLogger();

    public static CompletableFuture<ServerResourceManager> cache;
    public static Map<Identifier, JsonElement> recipes;
    public static boolean hasSeenRecipes;

    public static UserCache userCache;

    public static final Map<Identifier, Structure> structures = Collections.synchronizedMap(new HashMap<>());

    public static void log(String message) {
        LOGGER.info("[AntiResourceReload] {}", message);
    }
}
