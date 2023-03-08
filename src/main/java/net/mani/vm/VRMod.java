package net.mani.vm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.mani.vm.features.FillBottleWithWater;
import net.mani.vm.features.VRPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class VRMod implements ModInitializer {

    public static final String MOD_ID = "WaterBottleVRMod";
    public static final Logger LOGGER = LoggerFactory.getLogger("WaterBottleVRMod");
    public static boolean hasVRPlugin = false;
    private static World world;

    private static boolean worldGenerated = false;

    @Override
    public void onInitialize() {
        ClientTickEvents.START_CLIENT_TICK.register(new FillBottleWithWater());


        // load only after world has started.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            ClientTickEvents.END_CLIENT_TICK.register(client1 -> {
                ClientPlayerEntity player = minecraftClient.player;
                if (player != null && player.world != null) {
                    world = player.world;
                    worldGenerated = true;
                }
            });
        });
        try {
            Class.forName("net.blf02.vrapi.api.IVRAPI");
            VRPlugin.initVR();
        } catch (ClassNotFoundException e) {
            LOGGER.info("API WASN'T FOUND!!!");
        }
    }



    public static World getWorld() {
        return world;
    }

}