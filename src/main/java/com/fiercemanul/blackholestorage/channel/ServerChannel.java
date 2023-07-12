package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.network.ChannelPack;
import com.fiercemanul.blackholestorage.network.ChannelUpdatePack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;

public class ServerChannel extends Channel{

    private HashSet<String> changedItems = new HashSet<>();
    private boolean itemListChanged = false;
    private HashSet<ServerPlayer> players = new HashSet<>();
    private boolean removed = false;


    public ServerChannel() {}

    public ServerChannel(CompoundTag dat) {
        if (dat.contains("items")) {
            CompoundTag items = dat.getCompound("items");
            if (items.isEmpty()) return;
            items.getAllKeys().forEach(itemId -> {
                if (items.getInt(itemId) > 0 || ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemId))) {
                    storageItems.put(itemId, items.getInt(itemId));
                }
            });
        }
    }

    @Override
    public void onItemChanged(String itemId, boolean listChanged) {
        changedItems.add(itemId);
        itemListChanged = listChanged;
    }

    public void initialize(CompoundTag dat) {
        storageItems.clear();
        if (dat.contains("items")) {
            CompoundTag items = dat.getCompound("items");
            if (items.isEmpty()) return;
            items.getAllKeys().forEach(itemId -> {
                if (items.getInt(itemId) > 0 || ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemId))) {
                    storageItems.put(itemId, items.getInt(itemId));
                }
            });
        }
    }

    public CompoundTag getSaveData() {
        CompoundTag items = new CompoundTag();
        storageItems.forEach(items::putInt);
        CompoundTag saveData = new CompoundTag();
        saveData.put("items", items);
        return saveData;
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        CompoundTag tag = new CompoundTag();
        CompoundTag items = new CompoundTag();
        storageItems.forEach(items::putInt);
        tag.put("items", items);
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelPack(tag));
    }

    public void removeListener(ServerPlayer player) {
        players.remove(player);
    }

    public void sendUpdate() {
        if (changedItems.isEmpty()) return;
        if (!players.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            CompoundTag items = new CompoundTag();
            changedItems.forEach(itemId -> items.putInt(itemId, storageItems.getOrDefault(itemId, 0)));
            tag.put("items", items);
            ChannelUpdatePack pack = new ChannelUpdatePack(tag);
            players.forEach(player -> {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), pack);
            });
        }
        changedItems.clear();
    }

    public void sendFullUpdate() {
        if (changedItems.isEmpty()) return;
        if (!players.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            CompoundTag items = new CompoundTag();
            storageItems.forEach(items::putInt);
            tag.put("items", items);
            ChannelPack pack = new ChannelPack(tag);
            players.forEach(player -> {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), pack);
            });
        }
        changedItems.clear();
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved() {
        this.removed = true;
    }
}
