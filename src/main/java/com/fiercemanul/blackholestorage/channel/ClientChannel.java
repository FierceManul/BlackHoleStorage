package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

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
        storageFluids.clear();
        storageEnergies.clear();
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
        String name = tag.getString("name");
        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicBoolean flag1 = new AtomicBoolean(false);
        items.getAllKeys().forEach(itemId -> {
            long count = items.getLong(itemId);
            if (count <= 0L) {
                if (storageItems.containsKey(itemId)) {
                    storageItems.remove(itemId);
                    flag.set(true);
                    flag1.set(true);
                }
            } else {
                if (storageItems.containsKey(itemId)) {
                    if (storageItems.get(itemId) != count) {
                        storageItems.replace(itemId, count);
                        flag1.set(true);
                    }
                } else {
                    storageItems.put(itemId, count);
                    flag.set(true);
                    flag1.set(true);
                }
            }
        });
        fluids.getAllKeys().forEach(fluidId -> {
            long count = fluids.getLong(fluidId);
            if (count <= 0L ) {
                if (storageFluids.containsKey(fluidId)) {
                    storageFluids.remove(fluidId);
                    flag.set(true);
                    flag1.set(true);
                }
            } else {
                if (storageFluids.containsKey(fluidId)) {
                    if (storageFluids.get(fluidId) != count) {
                        storageFluids.replace(fluidId, count);
                        flag1.set(true);
                    }
                } else {
                    storageFluids.put(fluidId, count);
                    flag.set(true);
                    flag1.set(true);
                }
            }
        });
        energies.getAllKeys().forEach(energyId -> {
            long count = energies.getLong(energyId);
            if (count <= 0L ) {
                if (storageEnergies.containsKey(energyId)) {
                    storageEnergies.remove(energyId);
                    flag.set(true);
                    flag1.set(true);
                }
            } else {
                if (storageEnergies.containsKey(energyId)) {
                    if (storageEnergies.get(energyId) != count) {
                        storageEnergies.replace(energyId, count);
                        flag1.set(true);
                    }
                } else {
                    storageEnergies.put(energyId, count);
                    flag.set(true);
                    flag1.set(true);
                }
            }
        });
        if (!name.isEmpty()) setName(name);
        if (flag1.get()) container.refreshContainer(flag.get());
    }

    public void fullUpdate(CompoundTag tag) {
        CompoundTag items = tag.getCompound("items");
        CompoundTag fluids = tag.getCompound("fluids");
        CompoundTag energies = tag.getCompound("energies");
        String name = tag.getString("name");
        storageItems.clear();
        storageFluids.clear();
        storageEnergies.clear();
        items.getAllKeys().forEach(itemId -> storageItems.put(itemId, items.getLong(itemId)));
        fluids.getAllKeys().forEach(fluidId -> storageFluids.put(fluidId, fluids.getLong(fluidId)));
        energies.getAllKeys().forEach(energyId -> storageEnergies.put(energyId, energies.getLong(energyId)));
        setName(name);
        if (container != null) {
            container.refreshContainer(true);
        }
    }

    @Override
    public boolean isRemoved() {
        return false;
    }
}
