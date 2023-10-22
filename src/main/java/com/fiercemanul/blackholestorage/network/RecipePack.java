package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RecipePack {

    private final int containerId;
    private final String recipeId;
    private final CompoundTag itemNbt;
    private final boolean maxTransfer;

    public RecipePack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.recipeId = buf.readUtf();
        this.itemNbt = buf.readNbt();
        this.maxTransfer = buf.readBoolean();
    }

    public RecipePack(int containerId, String recipeId, Map<String, Integer> itemsMap, boolean maxTransfer) {
        this.containerId = containerId;
        this.recipeId = recipeId;
        CompoundTag tag = new CompoundTag();
        itemsMap.forEach(tag::putInt);
        this.itemNbt = tag;
        this.maxTransfer = maxTransfer;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeUtf(recipeId);
        buf.writeNbt(itemNbt);
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
                Map<String, Integer> itemsMap = new HashMap<>();
                itemNbt.getAllKeys().forEach(s -> itemsMap.put(s, itemNbt.getInt(s)));
                menu.receivedRecipe(recipeId, itemsMap, maxTransfer);
            }
        });
        context.get().setPacketHandled(true);
    }
}
