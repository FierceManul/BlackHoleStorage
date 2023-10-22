package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChannelPack {

    private final CompoundTag tag;

    public ChannelPack(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public ChannelPack(CompoundTag tag) {
        this.tag = tag;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientChannelManager.getInstance().fullUpdateChannel(tag)));
        context.get().setPacketHandled(true);
    }
}
