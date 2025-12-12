package gravel_to_sand.graveltosand.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
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


    @Unique
    private static boolean canInsertStack(ItemStack itemStack, Container inventory) {
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            Item item = itemStack.getItem();
            if(inventory.getItem(i).getItem().equals(item)) {
                if(inventory.getItem(i).get(DataComponents.CUSTOM_DATA) == null && inventory.getItem(i).getCount() < item.getDefaultMaxStackSize()){
                    return true;
                }
            }
        }
        return false;
    }

	@Inject(at = @At("HEAD"), method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z")
	private static void injectExtract(Container inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> ci) {

        CustomData tag = itemEntity.getItem().get(DataComponents.CUSTOM_DATA);
        if(tag != null && canInsertStack(itemEntity.getItem(), inventory)){
            CompoundTag nbt = tag.copyTag();
            nbt.remove("waterCauldronAge");
            if(!nbt.isEmpty()){
                tag = CustomData.of(nbt);
            }
            else{
                tag = null;
            }
            itemEntity.getItem().set(DataComponents.CUSTOM_DATA, null);
        }
	}
}