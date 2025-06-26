package me.wurgo.antiresourcereload.mixin;

import me.wurgo.antiresourcereload.AntiResourceReload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

@Mixin(StructureManager.class)
public abstract class StructureManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @ModifyArg(
            method = "getStructure",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
            ),
            index = 1
    )
    private Function<Identifier, Structure> getCachedStructure(Function<Identifier, Structure> function) {
        return id -> {
            if (AntiResourceReload.dataManager == this.server.getDataManager()) {
                return AntiResourceReload.structures.computeIfAbsent(id, function);
            }
            return function.apply(id);
        };
    }
}
