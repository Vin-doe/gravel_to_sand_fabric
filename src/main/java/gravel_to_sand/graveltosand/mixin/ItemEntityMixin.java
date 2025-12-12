package gravel_to_sand.graveltosand.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    public abstract ItemStack getItem();

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("meow");

    @Inject(at = @At("HEAD"), method ="playerTouch")
    private void injectOnPlayerCollision(Player player , CallbackInfo ci) {
        CustomData tag = this.getItem().get(DataComponents.CUSTOM_DATA);
        if(tag != null){
            CompoundTag nbt = tag.copyTag();
            nbt.remove("waterCauldronAge");
            if(!nbt.isEmpty()){
                tag = CustomData.of(nbt);
            }
            else{
                tag = null;
            }
            this.getItem().set(DataComponents.CUSTOM_DATA, null);
        }
    }
}
