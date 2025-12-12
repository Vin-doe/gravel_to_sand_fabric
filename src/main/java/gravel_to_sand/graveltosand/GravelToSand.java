package gravel_to_sand.graveltosand;

import gravel_to_sand.graveltosand.config.Config;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
//import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.*;

public class GravelToSand implements ModInitializer {
	public static final String MOD_ID = "gravel_to_sand";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final List<CauldronInfo> checkedCauldrons = new ArrayList<>();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        START_SERVER_TICK.register(this::processItems);
        Config.load();
		LOGGER.info(MOD_ID + " has been initialized!");
	}

    private boolean attemptHalflife(ItemEntity itemEntity) {
        //presumed to be a gravel item entity
        CustomData tag = itemEntity.getItem().get(DataComponents.CUSTOM_DATA);
        if(tag == null){
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("waterCauldronAge", Config.CAULDRON_TIME);
            tag = CustomData.of(nbt);
            itemEntity.getItem().set(DataComponents.CUSTOM_DATA, tag);
            return false;
        }

        CompoundTag nbt = tag.copyTag();
        boolean succeeded = false;
        if(!nbt.contains("waterCauldronAge")){
            nbt.putInt("waterCauldronAge", Config.CAULDRON_TIME);
        }
        else if(nbt.getInt("waterCauldronAge").get() <= 0){
            //if 1 second has passed in the cauldron
            succeeded = true;
        }
        else{
            int age = nbt.getInt("waterCauldronAge").get();
            age--;
            nbt.putInt("waterCauldronAge", age);
        }

        CustomData newTag = CustomData.of(nbt);
        itemEntity.getItem().set(DataComponents.CUSTOM_DATA, newTag);
        return succeeded;
    }

    public void processItems(MinecraftServer minecraftServer) {
        //check if cauldron has been in list for X ticks (would mean having more info than just block pos due to multiple dimensions)
        List<CauldronInfo> toRemove = new ArrayList<>();
        for (CauldronInfo cauldronInfo : checkedCauldrons){
            if(cauldronInfo.decrementTicks()){
                toRemove.add(cauldronInfo);
            }
        }
        checkedCauldrons.removeAll(toRemove);

        for(ServerLevel world : minecraftServer.getAllLevels()) {
            for(ItemEntity itemEntity : world.getEntities(EntityType.ITEM, EntitySelector.ENTITY_STILL_ALIVE)) {
                if (!itemEntity.getItem().is(Items.GRAVEL)) {
                    continue;
                }

                BlockPos blockPos = itemEntity.blockPosition();
                BlockState blockState = world.getBlockState(blockPos);

                if(blockState.getBlock() instanceof LayeredCauldronBlock cauldronBlock){
                    CauldronInfo cauldronInfo = new CauldronInfo(Config.CAULDRON_TICKS, blockPos, world.dimension().hashCode());
                    if(!attemptHalflife(itemEntity)){
                        continue;
                    }

                    if(checkedCauldrons.contains(cauldronInfo)){
                        continue;
                    }
                    else{
                        checkedCauldrons.add(cauldronInfo);
                    }


                    //all conditions met, lovely stuff
                    int stackSize = itemEntity.getItem().getCount();

                    boolean succeeded = false;
                    int failed = 0, waterLevel = blockState.getValue(LayeredCauldronBlock.LEVEL);
                    while(failed < stackSize){
                        if(world.getRandom().nextDouble() < Config.CONVERSION_CHANCE) {
                            if (world.getRandom().nextDouble() < Config.WATER_DEPLETE_CHANCE) {
                                waterLevel--;
                            }
                            succeeded = true;

                            // converts 1 item from the stack if possible, if it either converts, it'll break out of the loop
                            break;
                        }
                        else{
                            failed++;
                        }
                    }

                    if(waterLevel <= 0){
                        world.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState());
                    }
                    else{
                        world.setBlockAndUpdate(blockPos, blockState.setValue(LayeredCauldronBlock.LEVEL, waterLevel));
                    }

                    if (succeeded){
                        ItemStack sandStack = new ItemStack(Items.SAND);

                        ItemEntity sandEntity = new ItemEntity(world, itemEntity.position().x, itemEntity.position().y, itemEntity.position().z, sandStack);
                        sandEntity.setDeltaMovement(0, 0, 0);
                        world.addFreshEntity(sandEntity);

                        //pop sound :3
                        if(!world.isClientSide()){
                            world.playSound(null, blockPos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }

                        //set the stack size of the gravel item entity to stackSize - converted amount (if 0 then just kill the item entity)
                        if(stackSize - 1 > 0){
                            itemEntity.getItem().setCount(stackSize - 1);
                        }
                        else{
                            itemEntity.kill(world);
                        }
                    }
                }
            }
        }
    }
}