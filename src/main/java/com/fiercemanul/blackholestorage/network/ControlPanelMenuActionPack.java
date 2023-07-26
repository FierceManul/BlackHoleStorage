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
    private final String type;
    private final String id;

    public ControlPanelMenuActionPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.actionId = buf.readInt();
        this.type = buf.readUtf();
        this.id = buf.readUtf();
    }

    public ControlPanelMenuActionPack(int containerId, int actionId, String[] object) {
        this.containerId = containerId;
        this.actionId = actionId;
        this.type = object[0];
        this.id = object[1];
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(actionId);
        buf.writeUtf(type);
        buf.writeUtf(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId == containerId) {
                if (!player.containerMenu.stillValid(player)) {
                    BlackHoleStorage.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                } else {
                    ((ControlPanelMenu) player.containerMenu).action(actionId, type, id);
                    player.containerMenu.broadcastChanges();
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
