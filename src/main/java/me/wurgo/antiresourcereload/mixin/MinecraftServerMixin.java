package me.wurgo.antiresourcereload.mixin;

import com.google.common.collect.Lists;
import me.wurgo.antiresourcereload.AntiResourceReload;
import net.minecraft.loot.LootManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.*;
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
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow protected abstract void reloadDataPacks(LevelProperties levelProperties);
    @Mutable @Shadow @Final private ReloadableResourceManager dataManager;
    @Mutable @Shadow @Final private RegistryTagManager tagManager;
    @Mutable @Shadow @Final private RecipeManager recipeManager;
    @Mutable @Shadow @Final private LootManager lootManager;
    @Mutable @Shadow @Final private CommandFunctionManager commandFunctionManager;
    @Mutable @Shadow @Final private ServerAdvancementLoader advancementManager;

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private ResourcePackManager<ResourcePackProfile> dataPackContainerManager;

    @Redirect(
            method = "loadWorldDataPacks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;reloadDataPacks(Lnet/minecraft/world/level/LevelProperties;)V"
            )
    )
    private void antiresourcereload_cachedReload(MinecraftServer instance, LevelProperties levelProperties) {
        if (levelProperties.getEnabledDataPacks().size() + levelProperties.getDisabledDataPacks().size() != 0) {
            AntiResourceReload.log("Using data-packs, reloading.");
            this.reloadDataPacks(levelProperties);
            return;
        }

        if (AntiResourceReload.dataManager == null) {
            AntiResourceReload.log("Cached resources unavailable, reloading & caching.");
            AntiResourceReload.dataManager = this.dataManager;
            this.reloadDataPacks(levelProperties);
            AntiResourceReload.tagManager = this.tagManager;
            AntiResourceReload.recipeManager = this.recipeManager;
            AntiResourceReload.lootManager = this.lootManager;
            AntiResourceReload.commandFunctionManager = this.commandFunctionManager;
            AntiResourceReload.advancementManager = this.advancementManager;
        } else {
            AntiResourceReload.log("Using cached server resources.");
            this.dataManager = AntiResourceReload.dataManager;
            this.tagManager = AntiResourceReload.tagManager;
            this.recipeManager = AntiResourceReload.recipeManager;
            this.lootManager = AntiResourceReload.lootManager;
            this.commandFunctionManager = AntiResourceReload.commandFunctionManager;
            this.advancementManager = AntiResourceReload.advancementManager;
            if (AntiResourceReload.hasSeenRecipes) {
                ((RecipeManagerAccess) this.recipeManager).invokeApply(AntiResourceReload.recipes, null, null);
            }

            // should only be the vanilla pack
            // logic taken from MinecraftServer#reloadDataPacks
            List<ResourcePackProfile> list = Lists.newArrayList(this.dataPackContainerManager.getEnabledProfiles());

            for (ResourcePackProfile resourcePackProfile : this.dataPackContainerManager.getProfiles()) {
                if (!levelProperties.getDisabledDataPacks().contains(resourcePackProfile.getName()) && !list.contains(resourcePackProfile)) {
                    LOGGER.info("Found new data pack {}, loading it automatically", resourcePackProfile.getName());
                    resourcePackProfile.getInitialPosition().insert(list, resourcePackProfile, profile -> profile, false);
                }
            }

            this.dataPackContainerManager.setEnabledProfiles(list);

            levelProperties.getEnabledDataPacks().clear();
            levelProperties.getDisabledDataPacks().clear();
            this.dataPackContainerManager.getEnabledProfiles().forEach(profile -> levelProperties.getEnabledDataPacks().add(profile.getName()));
            this.dataPackContainerManager.getProfiles().forEach(profile -> {
                if (!this.dataPackContainerManager.getEnabledProfiles().contains(profile)) {
                    levelProperties.getDisabledDataPacks().add(profile.getName());
                }
            });
        }
    }
}
