package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.block.PassivePortBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public final class PassivePortMenuProvider implements MenuProvider {

    private final PassivePortBlockEntity blockEntity;

    public PassivePortMenuProvider(PassivePortBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("");
    }

    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PassivePortMenu(pContainerId, pPlayer, blockEntity);
    }
}
