package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import com.fiercemanul.blackholestorage.util.Tools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RecipeItemPack {

    private final int containerId;
    private final Map<String, Integer> itemNeed;

    public RecipeItemPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        CompoundTag tag = buf.readNbt();
        this.itemNeed = new HashMap<>();
        if (tag != null) tag.getAllKeys().forEach(s -> {
            int x = tag.getInt(s);
            if (x > 0) itemNeed.put(s, x);
        });
    }

    public RecipeItemPack(int containerId, Map<String, Integer> itemNeed) {
        this.containerId = containerId;
        this.itemNeed = itemNeed;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        CompoundTag tag = new CompoundTag();
        itemNeed.forEach(tag::putInt);
        buf.writeNbt(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                BlackHoleStorage.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else if (player.containerMenu instanceof ControlPanelMenu menu) {
                menu.receivedRecipe(itemNeed);
            }
        });
        context.get().setPacketHandled(true);
    }
}
