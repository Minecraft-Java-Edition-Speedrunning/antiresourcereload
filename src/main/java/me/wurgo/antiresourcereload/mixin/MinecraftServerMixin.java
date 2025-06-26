package me.wurgo.antiresourcereload.mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.wurgo.antiresourcereload.AntiResourceReload;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.world.level.LevelProperties;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private ResourcePackManager<ResourcePackProfile> dataPackManager;

    @Mutable
    @Shadow
    @Final
    private ReloadableResourceManager dataManager;
    @Mutable
    @Shadow
    @Final
    private RegistryTagManager tagManager;
    @Mutable
    @Shadow
    @Final
    private LootConditionManager predicateManager;
    @Mutable
    @Shadow
    @Final
    private RecipeManager recipeManager;
    @Mutable
    @Shadow
    @Final
    private LootManager lootManager;
    @Mutable
    @Shadow
    @Final
    private CommandFunctionManager commandFunctionManager;
    @Mutable
    @Shadow
    @Final
    private ServerAdvancementLoader advancementLoader;

    @WrapOperation(
            method = "loadWorldDataPacks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;reloadDataPacks(Lnet/minecraft/world/level/LevelProperties;)V"
            )
    )
    private void cachedReload(MinecraftServer server, LevelProperties properties, Operation<Void> original) {
        if (!properties.getEnabledDataPacks().isEmpty() || !properties.getDisabledDataPacks().isEmpty()) {
            AntiResourceReload.log("Using data-packs, reloading.");
            original.call(server, properties);
            return;
        }

        if (AntiResourceReload.dataManager == null) {
            AntiResourceReload.log("Cached resources unavailable, reloading & caching.");
            AntiResourceReload.dataManager = this.dataManager;
            original.call(server, properties);
            AntiResourceReload.tagManager = this.tagManager;
            AntiResourceReload.predicateManager = this.predicateManager;
            AntiResourceReload.recipeManager = this.recipeManager;
            AntiResourceReload.lootManager = this.lootManager;
            AntiResourceReload.commandFunctionManager = this.commandFunctionManager;
            AntiResourceReload.advancementLoader = this.advancementLoader;
        } else {
            AntiResourceReload.log("Using cached server resources.");
            this.dataManager = AntiResourceReload.dataManager;
            this.tagManager = AntiResourceReload.tagManager;
            this.predicateManager = AntiResourceReload.predicateManager;
            this.recipeManager = AntiResourceReload.recipeManager;
            this.lootManager = AntiResourceReload.lootManager;
            this.commandFunctionManager = AntiResourceReload.commandFunctionManager;
            this.advancementLoader = AntiResourceReload.advancementLoader;

            // should only be the vanilla pack
            // logic taken from MinecraftServer#reloadDataPacks
            List<ResourcePackProfile> list = Lists.newArrayList(this.dataPackManager.getEnabledProfiles());

            for (ResourcePackProfile resourcePackProfile : this.dataPackManager.getProfiles()) {
                if (!properties.getDisabledDataPacks().contains(resourcePackProfile.getName()) && !list.contains(resourcePackProfile)) {
                    LOGGER.info("Found new data pack {}, loading it automatically", resourcePackProfile.getName());
                    resourcePackProfile.getInitialPosition().insert(list, resourcePackProfile, profile -> profile, false);
                }
            }
            this.dataPackManager.setEnabledProfiles(list);
        }
    }

    @WrapWithCondition(
            method = "loadWorldDataPacks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;method_24154()V"
            )
    )
    private boolean skipInitializingShapeCache(MinecraftServer server) {
        if (!AntiResourceReload.hasInitializedShapeCache) {
            AntiResourceReload.hasInitializedShapeCache = true;
            return true;
        }
        return false;
    }
}
