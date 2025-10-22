package gravel_to_sand.graveltosand;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.Hopper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.*;

public class Gravel_to_sand implements ModInitializer {
	public static final String MOD_ID = "gravel_to_sand";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final double WATER_DEPLEAT_CHANCE = 0.1;
    public  static  final double CONVERSION_CHANCE = 0.5;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        START_SERVER_TICK.register(this::processItems);

		LOGGER.info(MOD_ID + " has been initialized!");
	}

    private boolean attemptHalflife(ItemEntity itemEntity) {
        //presumed to be a gravel item entity
        NbtComponent tag = itemEntity.getStack().get(DataComponentTypes.CUSTOM_DATA);
        if(tag == null){
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("waterCauldronAge", 20);
            tag = NbtComponent.of(nbt);
            itemEntity.getStack().set(DataComponentTypes.CUSTOM_DATA, tag);
            return false;
        }
        NbtCompound nbt = tag.copyNbt();
        boolean succeeded = false;
        if(!nbt.contains("waterCauldronAge")){
            nbt.putInt("waterCauldronAge", 20);
        }
        else if(nbt.getInt("waterCauldronAge").get() == 0){
            //if 1 second has passed in the cauldron
            nbt.remove("waterCauldronAge");
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
        for(ServerWorld world : minecraftServer.getWorlds()) {
            for(ItemEntity itemEntity : world.getEntitiesByType(EntityType.ITEM, EntityPredicates.VALID_ENTITY)) {
                if (!itemEntity.getStack().isOf(Items.GRAVEL)) {
                    continue;
                }

                BlockState blockState = world.getBlockState(itemEntity.getBlockPos());
                if(blockState.getBlock() instanceof LeveledCauldronBlock cauldronBlock){
                    //add condition and nbt tag or something for measuring how long it's been in there

                    if(!attemptHalflife(itemEntity)){
                        continue;
                    }

                    //all conditions met, lovely stuff
                    int stackSize = itemEntity.getStack().getCount();

                    //public ItemEntity(World world, double x, double y, double z, ItemStack stack)

                    int x = 0, failed = 0, waterLevel = blockState.get(LeveledCauldronBlock.LEVEL);
                    while(waterLevel > 0 && x + failed < stackSize){
                        if(world.getRandom().nextDouble() > CONVERSION_CHANCE) {
                            if (world.getRandom().nextDouble() < WATER_DEPLEAT_CHANCE) {
                                waterLevel--;
                            }
                            x++;
                        }
                        else{
                            failed++;
                        }
                    }

                    if(waterLevel <= 0){
                        world.setBlockState(itemEntity.getBlockPos(), Blocks.CAULDRON.getDefaultState());
                    }
                    else{
                        world.setBlockState(itemEntity.getBlockPos(), blockState.with(LeveledCauldronBlock.LEVEL, waterLevel));
                    }

                    ItemStack sandStack = new ItemStack(Items.SAND,  x);

                    ItemEntity sandEntity = new ItemEntity(world, itemEntity.getEntityPos().x, itemEntity.getEntityPos().y, itemEntity.getEntityPos().z, sandStack);
                    sandEntity.setVelocity(0, 0, 0);
                    world.spawnEntity(sandEntity);


                    //set the stack size of the gravel item entity to stackSize - converted amount (if 0 then just kill the item entity)
                    if(stackSize - x > 0){
                        ItemStack newGravelStack = new  ItemStack(Items.GRAVEL, stackSize - x);
                        itemEntity.setStack(newGravelStack);
                    }
                    else{
                        itemEntity.kill(world);
                    }
                }
            }
        }
    }
}