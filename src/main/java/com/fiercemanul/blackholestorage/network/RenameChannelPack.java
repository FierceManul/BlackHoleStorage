package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RenameChannelPack {

    private final int containerId;
    private final String name;

    public RenameChannelPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.name = buf.readUtf();
    }

    public RenameChannelPack(int containerId, String name) {
        this.containerId = containerId;
        this.name = name;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeUtf(name, 64);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (name.isEmpty()) return;
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                BlackHoleStorage.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                ((ChannelSelectMenu) player.containerMenu).renameChannel(name);
            }
        });
        context.get().setPacketHandled(true);
    }
}
