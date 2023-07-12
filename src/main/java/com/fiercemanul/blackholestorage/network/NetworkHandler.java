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
        INSTANCE.registerMessage(1,
                NameCachePack.class,
                NameCachePack::makePack,
                NameCachePack::new,
                NameCachePack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(2,
                ControlPanelMenuActionPack.class,
                ControlPanelMenuActionPack::makePack,
                ControlPanelMenuActionPack::new,
                ControlPanelMenuActionPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(3,
                ChannelUpdatePack.class,
                ChannelUpdatePack::makePack,
                ChannelUpdatePack::new,
                ChannelUpdatePack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(4,
                ChannelPack.class,
                ChannelPack::makePack,
                ChannelPack::new,
                ChannelPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
