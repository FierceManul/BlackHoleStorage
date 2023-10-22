package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChannelUpdatePack {

    private final CompoundTag tag;

    public ChannelUpdatePack(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public ChannelUpdatePack(CompoundTag tag) {
        this.tag = tag;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientChannelManager.getInstance().updateChannel(tag)));
        context.get().setPacketHandled(true);
    }
}
