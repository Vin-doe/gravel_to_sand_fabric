package gravel_to_sand.graveltosand.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//public static boolean HopperBlockEntity.extract(Inventory inventory, ItemEntity itemEntity)
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Shadow
    protected abstract boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side);

    @Unique
    private static boolean canInsert(ItemStack itemStack, Inventory inventory) {
        for(int i = 0; i < inventory.size(); i++) {
            Item item = itemStack.getItem();
            if(inventory.getStack(i).getItem().equals(item)) {
                if(inventory.getStack(i).get(DataComponentTypes.CUSTOM_DATA) == null && inventory.getStack(i).getCount() < item.getMaxCount()){
                    return true;
                }
            }
        }
        return false;
    }

	@Inject(at = @At("HEAD"), method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z")
	private static void injectExtract(Inventory inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> ci) {

        NbtComponent tag = itemEntity.getStack().get(DataComponentTypes.CUSTOM_DATA);
        if(tag != null && canInsert(itemEntity.getStack(), inventory)){
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