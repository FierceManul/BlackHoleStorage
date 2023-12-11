package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.block.PassivePortBlockEntity;
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

public final class PassivePortMenuProvider implements MenuProvider {

    private final PassivePortBlockEntity blockEntity;
    private final Level level;
    private final BlockPos pos;

    public PassivePortMenuProvider(PassivePortBlockEntity blockEntity, Level level, BlockPos pos) {
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
        return new PassivePortMenu(pContainerId, pPlayer, blockEntity, ContainerLevelAccess.create(level, pos));
    }
}
