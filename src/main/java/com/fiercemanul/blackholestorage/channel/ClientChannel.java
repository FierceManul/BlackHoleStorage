package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class ClientChannel extends Channel {

    @Nullable
    public ControlPanelMenu.DummyContainer container;

    public ClientChannel() {}

    public void addListener(ControlPanelMenu.DummyContainer container) {
        this.container = container;
    }

    public void removeListener() {
        this.container = null;
        storageItems.clear();
    }

    @Override
    public void onItemChanged(String itemId, boolean listChanged) {
        if (container != null) container.refreshContainer(listChanged);
    }

    @Override
    public void onFluidChanged(String fluidId, boolean listChanged) {
        if (container != null) container.refreshContainer(listChanged);
    }

    @Override
    public void onEnergyChanged(String energyId, boolean listChanged) {
        if (container != null) container.refreshContainer(listChanged);
    }

    public void update(CompoundTag tag) {
        if (container == null) return;
        CompoundTag items = tag.getCompound("items");
        CompoundTag fluids = tag.getCompound("fluids");
        CompoundTag energies = tag.getCompound("energies");
        items.getAllKeys().forEach(itemId -> {
            long count = items.getLong(itemId);
            if (count <= 0L ) storageItems.remove(itemId);
            else storageItems.put(itemId, count);
        });
        fluids.getAllKeys().forEach(fluidId -> {
            long count = fluids.getLong(fluidId);
            if (count <= 0L ) storageFluids.remove(fluidId);
            else storageFluids.put(fluidId, count);
        });
        energies.getAllKeys().forEach(energyId -> {
            long count = energies.getLong(energyId);
            if (count <= 0L ) storageEnergies.remove(energyId);
            else storageEnergies.put(energyId, count);
        });
        container.refreshContainer(false);
    }

    public void fullUpdate(CompoundTag tag) {
        CompoundTag items = tag.getCompound("items");
        CompoundTag fluids = tag.getCompound("fluids");
        CompoundTag energies = tag.getCompound("energies");
        storageItems.clear();
        storageFluids.clear();
        storageEnergies.clear();
        items.getAllKeys().forEach(itemId -> storageItems.put(itemId, items.getLong(itemId)));
        fluids.getAllKeys().forEach(fluidId -> storageFluids.put(fluidId, fluids.getLong(fluidId)));
        energies.getAllKeys().forEach(energyId -> storageEnergies.put(energyId, energies.getLong(energyId)));
        if (container != null) {
            container.refreshContainer(true);
        }
    }

    @Override
    public boolean isRemoved() {
        return false;
    }
}
