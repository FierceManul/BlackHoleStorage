package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.block.ActivePortBlockEntity;
import com.fiercemanul.blackholestorage.channel.InfoPort;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPortResultPack {

    private final BlockPos pos;
    private final CompoundTag northPort;
    private final CompoundTag southPort;
    private final CompoundTag westPort;
    private final CompoundTag eastPort;
    private final CompoundTag downPort;
    private final CompoundTag upPort;
    private final int rate;

    public ClientPortResultPack(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.northPort = buf.readNbt();
        this.southPort = buf.readNbt();
        this.westPort = buf.readNbt();
        this.eastPort = buf.readNbt();
        this.downPort = buf.readNbt();
        this.upPort = buf.readNbt();
        this.rate = buf.readInt();
    }

    public ClientPortResultPack(BlockPos pos, InfoPort northPort, InfoPort southPort, InfoPort westPort, InfoPort eastPort, InfoPort downPort, InfoPort upPort, int rate) {
        this.pos = pos;
        this.northPort = northPort.toNbt();
        this.southPort = southPort.toNbt();
        this.westPort = westPort.toNbt();
        this.eastPort = eastPort.toNbt();
        this.downPort = downPort.toNbt();
        this.upPort = upPort.toNbt();
        this.rate = rate;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(northPort);
        buf.writeNbt(southPort);
        buf.writeNbt(westPort);
        buf.writeNbt(eastPort);
        buf.writeNbt(downPort);
        buf.writeNbt(upPort);
        buf.writeInt(rate);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            Level level = player.level;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && !blockEntity.isRemoved() && blockEntity instanceof ActivePortBlockEntity activePort) {
                activePort.setNorthPort(northPort);
                activePort.setSouthPort(southPort);
                activePort.setWestPort(westPort);
                activePort.setEastPort(eastPort);
                activePort.setDownPort(downPort);
                activePort.setUpPort(upPort);
                activePort.setRate(rate);
                activePort.setChanged();
                activePort.updateBlockState();
            }
        });
        context.get().setPacketHandled(true);
    }
}
