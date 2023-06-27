package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BlackHoleStorage.MODID, "net"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void init() {
        INSTANCE.registerMessage(1, NameCachePack.class, NameCachePack::makePack, NameCachePack::new, NameCachePack::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
