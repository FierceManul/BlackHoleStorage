package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ControlPanelMenuActionPack {

    private final int containerId;
    private final int actionId;
    private final String itemId;
    private final int count;

    public ControlPanelMenuActionPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.actionId = buf.readInt();
        this.itemId = buf.readUtf();
        this.count = buf.readInt();
    }

    public ControlPanelMenuActionPack(int containerId, int actionId, String itemId, int count) {
        this.containerId = containerId;
        this.actionId = actionId;
        this.itemId = itemId;
        this.count = count;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(actionId);
        buf.writeUtf(itemId);
        buf.writeInt(count);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId == containerId) {
                if (!player.containerMenu.stillValid(player)) {
                    BlackHoleStorage.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                } else {
                    ((ControlPanelMenu) player.containerMenu).action(actionId, itemId, count);
                    player.containerMenu.broadcastChanges();
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
