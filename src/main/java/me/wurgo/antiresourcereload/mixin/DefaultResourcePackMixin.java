package me.wurgo.antiresourcereload.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DefaultResourcePack.class)
public abstract class DefaultResourcePackMixin {
    @Nullable
    @Unique
    private static PackResourceMetadata METADATA;

    @WrapMethod(method = "parseMetadata")
    private Object cacheMetadata(ResourceMetadataReader<?> reader, Operation<?> original) {
        if (reader != PackResourceMetadata.READER) {
            return original.call(reader);
        }
        if (METADATA == null) {
            METADATA = (PackResourceMetadata) original.call(reader);
        }
        return METADATA;
    }
}
