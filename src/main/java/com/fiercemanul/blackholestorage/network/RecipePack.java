package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RecipePack {

    private final int containerId;
    private final String recipeId;
    private final boolean maxTransfer;

    public RecipePack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.recipeId = buf.readUtf();
        this.maxTransfer = buf.readBoolean();
    }

    public RecipePack(int containerId, String recipeId, boolean maxTransfer) {
        this.containerId = containerId;
        this.recipeId = recipeId;
        this.maxTransfer = maxTransfer;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeUtf(recipeId);
        buf.writeBoolean(maxTransfer);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                BlackHoleStorage.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else if (player.containerMenu instanceof ControlPanelMenu menu) {
                menu.receivedRecipe(recipeId, maxTransfer);
            }
        });
        context.get().setPacketHandled(true);
    }
}
