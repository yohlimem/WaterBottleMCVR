package net.mani.vm.features;

import net.blf02.vrapi.api.IVRAPI;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.mani.vm.VRMod;

import java.util.List;

public class VRPlugin {

    public static IVRAPI apiInstance;
    public static void initVR() {
        List<EntrypointContainer<IVRAPI>> apis = FabricLoader.getInstance().getEntrypointContainers("vrapi", IVRAPI.class);
        if (apis.size() > 0) {
            IVRAPI apiInstance = apis.get(0).getEntrypoint();
            VRMod.hasVRPlugin = true;
            VRPlugin.apiInstance = apiInstance;
        }
    }
}
