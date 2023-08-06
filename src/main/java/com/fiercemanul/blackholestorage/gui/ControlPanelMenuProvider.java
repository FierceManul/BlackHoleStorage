package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.block.ControlPanelBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public final class ControlPanelMenuProvider implements MenuProvider {

    private final ControlPanelBlockEntity blockEntity;
    private final int slotIndex;

    public ControlPanelMenuProvider(ControlPanelBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.slotIndex = -2;
    }

    public ControlPanelMenuProvider(int slotIndex) {
        this.blockEntity = null;
        this.slotIndex = slotIndex;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ControlPanelMenu(pContainerId, pPlayer, blockEntity, slotIndex);
    }
}
