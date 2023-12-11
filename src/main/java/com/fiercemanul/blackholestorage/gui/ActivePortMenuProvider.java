package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.block.ActivePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public final class ActivePortMenuProvider implements MenuProvider {

    private final ActivePortBlockEntity blockEntity;
    private final Level level;
    private final BlockPos pos;

    public ActivePortMenuProvider(ActivePortBlockEntity blockEntity, Level level, BlockPos pos) {
        this.blockEntity = blockEntity;
        this.level = level;
        this.pos = pos;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ActivePortMenu(pContainerId, pPlayer, blockEntity, ContainerLevelAccess.create(level, pos));
    }
}
