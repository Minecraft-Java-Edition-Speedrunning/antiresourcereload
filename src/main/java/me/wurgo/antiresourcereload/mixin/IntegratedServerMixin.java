package me.wurgo.antiresourcereload.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.wurgo.antiresourcereload.AntiResourceReload;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Z)Lnet/minecraft/server/command/CommandManager;"
            )
    )
    private static CommandManager cacheCommandManager(boolean isDedicatedServer, Operation<CommandManager> original) {
        if (AntiResourceReload.commandManager == null) {
            AntiResourceReload.commandManager = original.call(isDedicatedServer);
        }
        return AntiResourceReload.commandManager;
    }
}
