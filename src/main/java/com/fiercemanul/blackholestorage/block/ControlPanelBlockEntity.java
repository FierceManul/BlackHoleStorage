package com.fiercemanul.blackholestorage.block;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.ChannelInfo;
import com.fiercemanul.blackholestorage.channel.IChannelTerminal;
import com.fiercemanul.blackholestorage.channel.ServerChannelManager;
import com.fiercemanul.blackholestorage.network.ChannelSetPack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.CONTROL_PANEL_BLOCK_ENTITY;

public class ControlPanelBlockEntity extends BlockEntity implements IChannelTerminal {

    private UUID owner;
    private boolean locked = false;
    private boolean craftingMode = false;
    private String filter = "";
    private byte sortType = 4;
    private byte viewType = 0;
    private UUID channelOwner;
    private int channelID = -1;
    private final HashSet<ServerPlayer> channelSelectors = new HashSet<>();


    public ControlPanelBlockEntity(BlockPos pos, BlockState state) {
        super(CONTROL_PANEL_BLOCK_ENTITY.get(), pos, state);
    }



    @Override
    public void load(CompoundTag pTag) {
        //这里要防null
        if (pTag.contains("owner")) {
            owner = pTag.getUUID("owner");
            locked = pTag.getBoolean("locked");
        }
        if (pTag.contains("craftingMode")) craftingMode = pTag.getBoolean("craftingMode");
        if (pTag.contains("filter")) filter = pTag.getString("filter");
        if (pTag.contains("sortType")) sortType = pTag.getByte("sortType");
        if (pTag.contains("viewType")) viewType = pTag.getByte("viewType");
        if (pTag.contains("channel")) {
            CompoundTag channel = pTag.getCompound("channel");
            channelOwner = channel.getUUID("channelOwner");
            channelID = channel.getInt("channelID");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (owner != null) {
            pTag.putUUID("owner", owner);
            pTag.putBoolean("locked", locked);
        }
        pTag.putBoolean("craftingMode", craftingMode);
        pTag.putString("filter", filter);
        pTag.putByte("sortType", sortType);
        pTag.putByte("viewType", viewType);
        if (channelID >= 0) {
            CompoundTag channel =  new CompoundTag();
            channel.putUUID("channelOwner", channelOwner);
            channel.putInt("channelID", channelID);
            pTag.put("channel", channel);
        }
    }


    public UUID getOwner() {
        return owner;
    }

    public boolean isLocked() {
        return locked;
    }

    public Boolean getCraftingMode() {
        return craftingMode;
    }

    public String getFilter() {
        return filter;
    }

    public byte getSortType() {
        return sortType;
    }

    public byte getViewType() {
        return viewType;
    }

    public UUID getChannelOwner() {
        return channelOwner;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.setChanged();
    }

    public void setCraftingMode(Boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.setChanged();
    }

    public void setFilter(String filter) {
        this.filter = filter;
        this.setChanged();
    }

    public void setSortType(byte sortType) {
        this.sortType = sortType;
        this.setChanged();
    }

    public void setViewType(byte viewType) {
        this.viewType = viewType;
        this.setChanged();
    }



    @Override
    public UUID getTerminalOwner() {
        return owner;
    }

    @Override
    public @Nullable ChannelInfo getChannelInfo() {
        if (channelID >= 0) return new ChannelInfo(channelOwner, channelID);
        return null;
    }

    @Override
    public void setChannel(UUID channelOwner, int channelID) {
        this.channelOwner = channelOwner;
        this.channelID = channelID;
        channelSelectors.forEach(player -> ServerChannelManager.sendChannelSet(player, owner, channelOwner, channelID));
    }

    @Override
    public void removeChannel(ServerPlayer actor) {
        this.channelID = -1;
        this.channelOwner = null;
        channelSelectors.forEach(player -> NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelSetPack((byte) -1, -1, "")));
        if (!actor.addItem(new ItemStack(BlackHoleStorage.STORAGE_CORE.get())))
            actor.drop(new ItemStack(BlackHoleStorage.STORAGE_CORE.get()), false);
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        if (channelID < 0) return;
        if (actor.getUUID().equals(channelOwner) || channelOwner.equals(BlackHoleStorage.FAKE_PLAYER_UUID))
            ServerChannelManager.getInstance().renameChannel(new ChannelInfo(channelOwner, channelID), name);
    }

    @Override
    public void addChannelSelector(ServerPlayer player) {
        channelSelectors.add(player);
        if (channelID < 0) return;
        ServerChannelManager.sendChannelSet(player, owner, channelOwner, channelID);
    }

    @Override
    public void removeChannelSelector(ServerPlayer player) {
        channelSelectors.remove(player);
    }

    @Override
    public boolean stillValid() {
        return !isRemoved();
    }

    @Override
    public void tryReOpenMenu(ServerPlayer player) {
        if (channelID >= 0) this.getBlockState().use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(
                new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5), Direction.UP, worldPosition, false));
    }

}
