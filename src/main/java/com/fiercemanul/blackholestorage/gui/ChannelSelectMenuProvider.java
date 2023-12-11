package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.channel.IChannelTerminal;
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

public final class ChannelSelectMenuProvider implements MenuProvider {

    private final IChannelTerminal terminal;
    private final ContainerLevelAccess access;

    public ChannelSelectMenuProvider(IChannelTerminal terminal, ContainerLevelAccess access) {
        this.terminal = terminal;
        this.access = access;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ChannelSelectMenu(pContainerId, pPlayer, terminal, access);
    }
}
