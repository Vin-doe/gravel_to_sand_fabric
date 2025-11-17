package gravel_to_sand.graveltosand;

import gravel_to_sand.graveltosand.config.Config;
import net.fabricmc.api.ModInitializer;

import net.minecraft.block.*;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.*;

public class Gravel_to_sand implements ModInitializer {
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
        NbtComponent tag = itemEntity.getStack().get(DataComponentTypes.CUSTOM_DATA);
        if(tag == null){
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("waterCauldronAge", Config.CAULDRON_TIME);
            tag = NbtComponent.of(nbt);
            itemEntity.getStack().set(DataComponentTypes.CUSTOM_DATA, tag);
            return false;
        }

        NbtCompound nbt = tag.copyNbt();
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

        NbtComponent newTag = NbtComponent.of(nbt);
        itemEntity.getStack().set(DataComponentTypes.CUSTOM_DATA, newTag);
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

        for(ServerWorld world : minecraftServer.getWorlds()) {
            for(ItemEntity itemEntity : world.getEntitiesByType(EntityType.ITEM, EntityPredicates.VALID_ENTITY)) {
                if (!itemEntity.getStack().isOf(Items.GRAVEL)) {
                    continue;
                }

                BlockPos blockPos = itemEntity.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);

                if(blockState.getBlock() instanceof LeveledCauldronBlock cauldronBlock){
                    CauldronInfo cauldronInfo = new CauldronInfo(Config.CAULDRON_TICKS, blockPos, world.getRegistryKey().hashCode());
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
                    int stackSize = itemEntity.getStack().getCount();

                    boolean succeeded = false;
                    int failed = 0, waterLevel = blockState.get(LeveledCauldronBlock.LEVEL);
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
                        world.setBlockState(blockPos, Blocks.CAULDRON.getDefaultState());
                    }
                    else{
                        world.setBlockState(blockPos, blockState.with(LeveledCauldronBlock.LEVEL, waterLevel));
                    }

                    if (succeeded){
                        ItemStack sandStack = new ItemStack(Items.SAND);

                        ItemEntity sandEntity = new ItemEntity(world, itemEntity.getEntityPos().x, itemEntity.getEntityPos().y, itemEntity.getEntityPos().z, sandStack);
                        sandEntity.setVelocity(0, 0, 0);
                        world.spawnEntity(sandEntity);

                        //pop sound :3
                        if(!world.isClient()){
                            world.playSound(null, blockPos, SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }

                        //set the stack size of the gravel item entity to stackSize - converted amount (if 0 then just kill the item entity)
                        if(stackSize - 1 > 0){
                            itemEntity.getStack().setCount(stackSize - 1);
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