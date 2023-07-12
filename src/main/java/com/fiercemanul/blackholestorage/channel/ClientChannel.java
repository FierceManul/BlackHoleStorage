package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ClientChannel extends Channel {

    @Nullable
    public ControlPanelMenu.DummyContainer container;

    public ClientChannel() {}

    public void initialize(CompoundTag dat) {
        storageItems.clear();
        if (dat.contains("items")) {
            CompoundTag items = dat.getCompound("items");
            if (items.isEmpty()) return;
            items.getAllKeys().forEach(itemId -> storageItems.put(itemId, items.getInt(itemId)));
        }
    }

    public void addListener(ControlPanelMenu.DummyContainer container) {
        this.container = container;
    }

    public void removeListener() {
        this.container = null;
        storageItems.clear();
    }

    @Override
    public void onItemChanged(String itemId, boolean listChanged) {
        if (container != null) {
            container.refreshContainer(listChanged);
        }
    }

    public void updateItems(CompoundTag tag) {
        if (container != null) {
            CompoundTag items = tag.getCompound("items");
            items.getAllKeys().forEach(itemId -> {
                int count = items.getInt(itemId);
                if (count <= 0 ) {
                    storageItems.remove(itemId);
                } else {
                    storageItems.put(itemId, count);
                }
            });
            container.refreshContainer(false);
        }
    }

    public void fullUpdateItems(CompoundTag tag) {
        CompoundTag items = tag.getCompound("items");
        storageItems.clear();
        items.getAllKeys().forEach(itemId -> storageItems.put(itemId, items.getInt(itemId)));
        if (container != null) {
            container.refreshContainer(true);
        }
    }

    @Override
    public boolean isRemoved() {
        return false;
    }
}
