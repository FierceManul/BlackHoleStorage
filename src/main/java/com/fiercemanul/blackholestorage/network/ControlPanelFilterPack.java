package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ControlPanelFilterPack {

    private final int containerId;
    private final String filter;

    public ControlPanelFilterPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.filter = buf.readUtf(64);
    }

    public ControlPanelFilterPack(int containerId, String filter) {
        this.containerId = containerId;
        this.filter = filter;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeUtf(filter, 64);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId == containerId) {
                if (!player.containerMenu.stillValid(player)) {
                    BlackHoleStorage.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                } else {
                    ((ControlPanelMenu) player.containerMenu).filter = filter;
                    player.containerMenu.broadcastChanges();
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
