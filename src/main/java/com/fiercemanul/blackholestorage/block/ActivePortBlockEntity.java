package com.fiercemanul.blackholestorage.block;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.*;
import com.fiercemanul.blackholestorage.network.ChannelSetPack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.ACTIVE_PORT_BLOCK_ENTITY;

public class ActivePortBlockEntity extends BlockEntity implements IChannelTerminal {

    private UUID owner;
    private boolean locked = false;
    private UUID channelOwner;
    private int channelID = -1;
    private ServerChannel channel = NullChannel.INSTANCE;
    protected int rate = 20;
    protected final Port northPort = new Port(Direction.SOUTH);
    protected final Port southPort = new Port(Direction.NORTH);
    protected final Port westPort = new Port(Direction.EAST);
    protected final Port eastPort = new Port(Direction.WEST);
    protected final Port downPort = new Port(Direction.UP);
    protected final Port upPort = new Port(Direction.DOWN);
    private @Nullable ServerPlayer user;

    public ActivePortBlockEntity(BlockPos pos, BlockState state) {
        super(ACTIVE_PORT_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void load(CompoundTag pTag) {
        if (pTag.contains("owner")) {
            owner = pTag.getUUID("owner");
            locked = pTag.getBoolean("locked");
        }
        if (pTag.contains("channel")) {
            CompoundTag channel = pTag.getCompound("channel");
            channelOwner = channel.getUUID("channelOwner");
            channelID = channel.getInt("channelID");
        }
        channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
        northPort.fromNbt(pTag.getCompound("northPort"));
        southPort.fromNbt(pTag.getCompound("southPort"));
        westPort.fromNbt(pTag.getCompound("westPort"));
        eastPort.fromNbt(pTag.getCompound("eastPort"));
        downPort.fromNbt(pTag.getCompound("downPort"));
        upPort.fromNbt(pTag.getCompound("upPort"));
        int rate = pTag.getInt("rate");
        if (rate > 0) this.rate = rate;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void saveAdditional(CompoundTag pTag) {
        if (owner != null) {
            pTag.putUUID("owner", owner);
            pTag.putBoolean("locked", locked);
        }
        if (channelID >= 0) {
            CompoundTag channel = new CompoundTag();
            channel.putUUID("channelOwner", channelOwner);
            channel.putInt("channelID", channelID);
            pTag.put("channel", channel);
        }
        pTag.put("northPort", northPort.toNbt());
        pTag.put("southPort", southPort.toNbt());
        pTag.put("westPort", westPort.toNbt());
        pTag.put("eastPort", eastPort.toNbt());
        pTag.put("downPort", downPort.toNbt());
        pTag.put("upPort", upPort.toNbt());
        pTag.putInt("rate", rate);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ActivePortBlockEntity activePort) {
        if (level.isClientSide) return;
        if (level.getServer() != null && level.getServer().getTickCount() % activePort.rate != 0) return;
        if (activePort.channel.isRemoved() && activePort.channelID >= 0) activePort.setChannel(null, -1);
        if (activePort.channel != null && !activePort.channel.isRemoved()) {
            activePort.northPort.onTick(activePort.channel, level.getBlockEntity(pos.north()));
            activePort.southPort.onTick(activePort.channel, level.getBlockEntity(pos.south()));
            activePort.westPort.onTick(activePort.channel, level.getBlockEntity(pos.west()));
            activePort.eastPort.onTick(activePort.channel, level.getBlockEntity(pos.east()));
            activePort.downPort.onTick(activePort.channel, level.getBlockEntity(pos.below()));
            activePort.upPort.onTick(activePort.channel, level.getBlockEntity(pos.above()));
        }
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.setChanged();
    }

    public void setUser(@Nullable ServerPlayer user) {
        this.user = user;
    }

    public boolean hasUser() {
        return user != null;
    }

    public String getChannelName() {
        return channel.getName();
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
        this.setChanged();
        this.channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
        if (user != null) ServerChannelManager.sendChannelSet(user, owner, channelOwner, channelID);
    }

    @Override
    public void removeChannel(ServerPlayer actor) {
        if (channelOwner == null) return;
        if (channelOwner.equals(actor.getUUID()) || channelOwner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
            if (!ServerChannelManager.getInstance().tryRemoveChannel(channelOwner, channelID)) return;
            this.channelID = -1;
            this.channelOwner = null;
            this.setChanged();
            this.channel = NullChannel.INSTANCE;
            if (user != null) NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> user), new ChannelSetPack((byte) -1, -1, ""));
            if (!actor.addItem(new ItemStack(BlackHoleStorage.STORAGE_CORE.get())))
                actor.drop(new ItemStack(BlackHoleStorage.STORAGE_CORE.get()), false);
        }
    }

    @Override
    public void renameChannel(ServerPlayer actor, String name) {
        if (channelID < 0) return;
        if (actor.getUUID().equals(channelOwner) || channelOwner.equals(BlackHoleStorage.FAKE_PLAYER_UUID))
            ServerChannelManager.getInstance().renameChannel(new ChannelInfo(channelOwner, channelID), name);
    }

    @Override
    public void addChannelSelector(ServerPlayer player) {
        this.user = player;
        if (channelID < 0) return;
        ServerChannelManager.sendChannelSet(player, owner, channelOwner, channelID);
    }

    @Override
    public void removeChannelSelector(ServerPlayer player) {
        this.user = null;
    }

    @Override
    public boolean stillValid() {
        return !isRemoved();
    }

    @Override
    public Block getBlock() {
        return BlackHoleStorage.ACTIVE_PORT.get();
    }

    @Override
    public void tryReOpenMenu(ServerPlayer player) {
        if (channelID >= 0) this.getBlockState().use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(
                new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5), Direction.UP, worldPosition, false));
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = Integer.min(1200, Integer.max(1, rate));
    }

    public void setNorthPort(CompoundTag tag) {
        northPort.fromNbt(tag);
    }

    public void setSouthPort(CompoundTag tag) {
        southPort.fromNbt(tag);
    }

    public void setWestPort(CompoundTag tag) {
        westPort.fromNbt(tag);
    }

    public void setEastPort(CompoundTag tag) {
        eastPort.fromNbt(tag);
    }

    public void setDownPort(CompoundTag tag) {
        downPort.fromNbt(tag);
    }

    public void setUpPort(CompoundTag tag) {
        upPort.fromNbt(tag);
    }

    public void updateBlockState() {
        if (level != null) {
            BlockState state = this.getBlockState();
            state = state.setValue(ActivePortBlock.NORTH, northPort.enable);
            state = state.setValue(ActivePortBlock.SOUTH, southPort.enable);
            state = state.setValue(ActivePortBlock.WEST, westPort.enable);
            state = state.setValue(ActivePortBlock.EAST, eastPort.enable);
            state = state.setValue(ActivePortBlock.DOWN, downPort.enable);
            state = state.setValue(ActivePortBlock.UP, upPort.enable);
            level.setBlock(getBlockPos(), state, 11);
        }
    }
}
