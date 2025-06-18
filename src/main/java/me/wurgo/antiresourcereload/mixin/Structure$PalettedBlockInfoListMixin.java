package me.wurgo.antiresourcereload.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.structure.Structure;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.Map;

@Mixin(Structure.PalettedBlockInfoList.class)
public abstract class Structure$PalettedBlockInfoListMixin {

    @WrapOperation(
            method = "<init>*",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/structure/Structure$PalettedBlockInfoList;blockToInfos:Ljava/util/Map;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void synchronizeBlockToInfosMap(Structure.PalettedBlockInfoList list, Map<?, ?> blockToInfos, Operation<Void> original) {
        original.call(list, Collections.synchronizedMap(blockToInfos));
    }
}
