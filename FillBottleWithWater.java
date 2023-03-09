package net.mani.vm.features;

import net.blf02.vrapi.api.IVRAPI;
import net.blf02.vrapi.client.ReflectionConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.mani.vm.VRMod;
import net.minecraft.block.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.minecraft.util.math.Vec3d;

public class FillBottleWithWater implements ClientTickEvents.StartTick{

    private int timer = 0;

    private Vec3d lastControllerPos = new Vec3d(0,0,0);


    @Override
    public void onStartTick(MinecraftClient client) {
        if(client.player == null) return;
        if(VRMod.getWorld() == null) return;

        // get player
        PlayerEntity player = client.player;
//        VRMod.LOGGER.info(VRPlugin.apiInstance.getClass().toString());
//        VRMod.LOGGER.info(String.valueOf(VRPlugin.apiInstance.getClass()));
//        VRMod.LOGGER.info(String.valueOf(player));
        // controller velocity calculated by taking the current controller position with the last position **TIMES 100** to make it fast enough
        // VRPlugin.apiInstance.isLeftHanded(player) ? 1 : 0 means that if the player is left-handed (true) say 1 else say 0
        Vec3d controllerVel;
        try{
            controllerVel = (VRPlugin.apiInstance.getRenderVRPlayer().getController(VRPlugin.apiInstance.isLeftHanded(player) ? 1 : 0).position().subtract(lastControllerPos)).multiply(100d);

        } catch (RuntimeException re){
            return;
        }
        VRMod.LOGGER.info(String.valueOf(VRPlugin.apiInstance.isLeftHanded(player)));

        // calculate the magnitude of the vector AKA speed
        double controllerVelMag = Math.sqrt(controllerVel.x * controllerVel.x + controllerVel.y * controllerVel.y + controllerVel.z * controllerVel.z);
        // get the block at the position of the correct hand
        BlockPos blockPos = new BlockPos(VRPlugin.apiInstance.getRenderVRPlayer().getController(VRPlugin.apiInstance.isLeftHanded(player) ? 1 : 0).position());
        // set lastControllerPos to calculate the velocity
        lastControllerPos = VRPlugin.apiInstance.getRenderVRPlayer().getController(VRPlugin.apiInstance.isLeftHanded(player) ? 1 : 0).position();
        // just find the block state at the block position
        BlockState blockState = VRMod.getWorld().getBlockState(blockPos);
        // find the blockState's block
        Block block = blockState.getBlock();
        // if hand touches liquid
        boolean blockIsLiquid = blockState.getMaterial().isLiquid();
        // if hand touches water
        boolean blockIsWater = blockIsLiquid && (block == Blocks.WATER);
        // if hand touches cauldron
        boolean blockIsCauldron = block == Blocks.WATER_CAULDRON;
        // if the cauldron is not empty
        boolean CauldronIsFull;
        if(blockIsCauldron){
            CauldronIsFull = blockState.get(LeveledCauldronBlock.LEVEL) > 0;
        } else{
            CauldronIsFull = false;
        }
//        VRMod.LOGGER.info("block: " + String.valueOf(block));
//        VRMod.LOGGER.info("blockState: " + String.valueOf(blockState));
//        VRMod.LOGGER.info("blockIsCauldron: " + String.valueOf(blockIsCauldron));
//        VRMod.LOGGER.info("CauldronIsFull: " + String.valueOf(CauldronIsFull));
//        VRMod.LOGGER.info("timer: " + String.valueOf(timer));
//        VRMod.LOGGER.info("position: " + String.valueOf(VRPlugin.apiInstance.getRenderVRPlayer().getController(VRPlugin.apiInstance.isLeftHanded(player) ? 1 : 0).position()));
//        VRMod.LOGGER.info("lastControllerPos: " + String.valueOf(lastControllerPos));
//        VRMod.LOGGER.info("controllerVel: " + String.valueOf(controllerVel));
//        VRMod.LOGGER.info("controllerVelMag: " + String.valueOf(controllerVelMag));


        if(player.isHolding(Items.GLASS_BOTTLE) && blockIsWater){
            // adding controller mag because if you swing the bottle faster it will fill it faster
            // just so it's not too slow
            timer += controllerVelMag + 1;
        }
        else if(player.isHolding(Items.GLASS_BOTTLE) && CauldronIsFull && blockIsCauldron){
            timer += controllerVelMag + 1;
        }
        else{
            // if you leave what you are doing set timer to 0
            timer = 0;
        }

        // if 2 seconds pass AKA 40 ticks
        if(timer >= 2*20){
            // give water bottle and replace the one you have in your hand
            ItemStack heldItem = player.getMainHandStack();
            if (heldItem.getItem() == Items.GLASS_BOTTLE) {
                heldItem.decrement(1);
                ItemStack potion = PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                player.giveItemStack(potion);
                timer = 0;
            }
            // if the block is cauldron
            if(blockIsCauldron){

                int level = blockState.get(LeveledCauldronBlock.LEVEL);
                if(level > 0){
                    // take one water level out of it
                    LeveledCauldronBlock.decrementFluidLevel(blockState, VRMod.getWorld(), blockPos);
                    // play sound (dunno if it works)
                    VRMod.getWorld().playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    // i don't know what that does but its in the source code of minecraft so yeah
                    VRMod.getWorld().emitGameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                    player.incrementStat(Stats.USE_CAULDRON);
                }

            }


        }
    }

}
