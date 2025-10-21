package gravel_to_sand.graveltosand.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//public static boolean HopperBlockEntity.extract(Inventory inventory, ItemEntity itemEntity)
@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
	@Inject(at = @At("HEAD"), method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z")
	private static void injectExtract(Inventory inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> ci) {
        NbtComponent tag = itemEntity.getStack().get(DataComponentTypes.CUSTOM_DATA);
        if(tag != null){
            NbtCompound nbt = tag.copyNbt();
            nbt.remove("waterCauldronAge");
            if(!nbt.isEmpty()){
                tag = NbtComponent.of(nbt);
            }
            else{
                tag = null;
            }
            itemEntity.getStack().set(DataComponentTypes.CUSTOM_DATA, null);
        }
	}
}