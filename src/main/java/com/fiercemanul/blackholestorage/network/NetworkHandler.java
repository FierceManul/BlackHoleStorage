package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
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
        INSTANCE.registerMessage(5,
                ControlPanelFilterPack.class,
                ControlPanelFilterPack::makePack,
                ControlPanelFilterPack::new,
                ControlPanelFilterPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(6,
                ChannelListPack.class,
                ChannelListPack::makePack,
                ChannelListPack::new,
                ChannelListPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(7,
                AddChannelPack.class,
                AddChannelPack::makePack,
                AddChannelPack::new,
                AddChannelPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(8,
                ChannelAddPack.class,
                ChannelAddPack::makePack,
                ChannelAddPack::new,
                ChannelAddPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(9,
                ChannelSetPack.class,
                ChannelSetPack::makePack,
                ChannelSetPack::new,
                ChannelSetPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(10,
                SetChannelPack.class,
                SetChannelPack::makePack,
                SetChannelPack::new,
                SetChannelPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(11,
                ChannelRemovePack.class,
                ChannelRemovePack::makePack,
                ChannelRemovePack::new,
                ChannelRemovePack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(12,
                RenameChannelPack.class,
                RenameChannelPack::makePack,
                RenameChannelPack::new,
                RenameChannelPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(13,
                ClientPortResultPack.class,
                ClientPortResultPack::makePack,
                ClientPortResultPack::new,
                ClientPortResultPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(14,
                RecipePack.class,
                RecipePack::makePack,
                RecipePack::new,
                RecipePack::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
