package me.wurgo.antiresourcereload.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfileRepository;
import me.wurgo.antiresourcereload.AntiResourceReload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Nullable
    private IntegratedServer server;

    @Inject(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;connectLocal(Ljava/net/SocketAddress;)Lnet/minecraft/network/ClientConnection;"
            )
    )
    private void reloadRecipes(CallbackInfo ci) {
        // reloading is done when actually joining the world instead of on world creation because of SeedQueue
        if (this.server != null && this.server.getRecipeManager() == AntiResourceReload.recipeManager && AntiResourceReload.hasSeenRecipes) {
            ((RecipeManagerAccess) AntiResourceReload.recipeManager).antiresourcereload$apply(AntiResourceReload.recipes, null, null);
            AntiResourceReload.hasSeenRecipes = false;
        }
    }

    @WrapOperation(
            method = {
                    "startIntegratedServer",
                    "joinWorld"
            },
            at = @At(
                    value = "NEW",
                    target = "(Lcom/mojang/authlib/GameProfileRepository;Ljava/io/File;)Lnet/minecraft/util/UserCache;"
            ),
            require = 2
    )
    private UserCache cacheUserCache(GameProfileRepository profileRepository, File cacheFile, Operation<UserCache> original) {
        if (AntiResourceReload.userCache == null) {
            AntiResourceReload.userCache = original.call(profileRepository, cacheFile);
        }
        return AntiResourceReload.userCache;
    }
}
