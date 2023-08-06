package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetChannelPack {

    private final int containerId;
    private final byte type;
    private final int id;

    public SetChannelPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.type = buf.readByte();
        this.id = buf.readInt();
    }

    public SetChannelPack(int containerId, byte type, int id) {
        this.containerId = containerId;
        this.type = type;
        this.id = id;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeByte(type);
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                BlackHoleStorage.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                ((ChannelSelectMenu) player.containerMenu).setChannel(type, id);
            }
        });
        context.get().setPacketHandled(true);
    }
}
