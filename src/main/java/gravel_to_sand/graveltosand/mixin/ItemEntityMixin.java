package gravel_to_sand.graveltosand.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
    public abstract ItemStack getStack();

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("meow");

    @Inject(at = @At("HEAD"), method ="onPlayerCollision")
    private void injectOnPlayerCollision(PlayerEntity player , CallbackInfo ci) {
        NbtComponent tag = this.getStack().get(DataComponentTypes.CUSTOM_DATA);
        if(tag != null){
            NbtCompound nbt = tag.copyNbt();
            nbt.remove("waterCauldronAge");
            if(!nbt.isEmpty()){
                tag = NbtComponent.of(nbt);
            }
            else{
                tag = null;
            }
            this.getStack().set(DataComponentTypes.CUSTOM_DATA, null);
        }
    }
}
