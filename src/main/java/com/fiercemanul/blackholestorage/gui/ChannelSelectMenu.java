package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.channel.IChannelTerminal;
import com.fiercemanul.blackholestorage.channel.ServerChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ChannelSelectMenu extends AbstractContainerMenu {

    private final Player player;
    public final IChannelTerminal terminal;

    public ChannelSelectMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(BlackHoleStorage.CHANNEL_SELECT_MENU.get(), containerId);
        this.player = playerInv.player;
        this.terminal = null;
    }
    public ChannelSelectMenu(int containerId, Player player, IChannelTerminal terminal) {
        super(BlackHoleStorage.CHANNEL_SELECT_MENU.get(), containerId);
        this.player = player;
        this.terminal = terminal;
        ServerChannelManager.getInstance().addChannelSelector((ServerPlayer) player, terminal.getTerminalOwner());
        this.terminal.addChannelSelector((ServerPlayer) player);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> removeChannel();
            case 1 -> tryBack();
        }
        return true;
    }

    public void setChannel(byte type, int id) {
        switch (type) {
            case (byte) 0 -> terminal.setChannel(player.getUUID(), id);
            case (byte) 1 -> terminal.setChannel(terminal.getTerminalOwner(), id);
            case (byte) 2 -> terminal.setChannel(BlackHoleStorage.FAKE_PLAYER_UUID, id);
        }
    }

    public void removeChannel() {
        terminal.removeChannel((ServerPlayer) player);
    }

    public void renameChannel(String name) {
        terminal.renameChannel((ServerPlayer) player, name);
    }

    private void tryBack() {
        terminal.removeChannelSelector((ServerPlayer) player);
        if (terminal.getChannelInfo() == null) {
            player.closeContainer();
        } else {
            terminal.tryReOpenMenu((ServerPlayer) player);
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return terminal.stillValid();
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        if (pPlayer.isLocalPlayer()) ClientChannelManager.getInstance().onScreenClose();
        else {
            terminal.removeChannelSelector((ServerPlayer) pPlayer);
            ServerChannelManager.getInstance().removeChannelSelector((ServerPlayer) player);
        }
    }
}
