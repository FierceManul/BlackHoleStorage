package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.block.PassivePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PassivePortMenu extends AbstractContainerMenu {

    protected final Player player;
    protected final UUID owner;
    protected boolean locked;
    public final PassivePortBlockEntity passivePort;
    public final BlockPos blockPos;
    public final UUID channelOwner;
    public final String channelName;

    public PassivePortMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(BlackHoleStorage.PASSIVE_PORT_MENU.get(), containerId);
        this.player = playerInv.player;
        this.owner = extraData.readUUID();
        this.locked = extraData.readBoolean();
        this.passivePort = null;
        this.blockPos = extraData.readBlockPos();
        this.channelOwner = extraData.readUUID();
        this.channelName = extraData.readUtf();
    }
    public PassivePortMenu(int containerId, Player player, PassivePortBlockEntity passivePort) {
        super(BlackHoleStorage.PASSIVE_PORT_MENU.get(), containerId);
        this.player = player;
        this.owner = passivePort.getOwner();
        this.locked = passivePort.isLocked();
        this.passivePort = passivePort;
        this.blockPos = passivePort.getBlockPos();
        this.channelOwner = passivePort.getChannelInfo().owner();
        this.channelName = passivePort.getChannelName();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> switchState(BlockStateProperties.NORTH);
            case 1 -> switchState(BlockStateProperties.SOUTH);
            case 2 -> switchState(BlockStateProperties.WEST);
            case 3 -> switchState(BlockStateProperties.EAST);
            case 4 -> switchState(BlockStateProperties.UP);
            case 5 -> switchState(BlockStateProperties.DOWN);
            case 6 -> toggleLock();
            case 7 -> openChannelScreen();
        }
        return true;
    }

    public void switchState(BooleanProperty property) {
        if (locked) return;
        BlockState state = passivePort.getBlockState();
        player.level.setBlock(blockPos, state.setValue(property, !state.getValue(property)), 11);
    }

    protected void toggleLock() {
        if (owner.equals(player.getUUID()) || owner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
            this.locked = !locked;
            passivePort.setLocked(locked);
        }
    }

    private void openChannelScreen() {
        if (locked) return;
        NetworkHooks.openScreen((ServerPlayer) player, new ChannelSelectMenuProvider(passivePort), buf -> {});
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return !passivePort.isRemoved() &&
                player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 32.0D;
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        if (!player.level.isClientSide) passivePort.setUser(null);
    }
}
