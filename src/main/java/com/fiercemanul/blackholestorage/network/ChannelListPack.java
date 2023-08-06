package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChannelListPack {

    private final CompoundTag myChannels;
    private final CompoundTag otherChannels;
    private final CompoundTag publicChannels;

    public ChannelListPack(FriendlyByteBuf buf) {
        this.myChannels = buf.readNbt();
        this.otherChannels = buf.readNbt();
        this.publicChannels = buf.readNbt();
    }

    public ChannelListPack(CompoundTag my, CompoundTag other, CompoundTag pub) {
        this.myChannels = my;
        this.otherChannels = other;
        this.publicChannels = pub;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeNbt(myChannels);
        buf.writeNbt(otherChannels);
        buf.writeNbt(publicChannels);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientChannelManager.getInstance().setChannelList(myChannels, otherChannels, publicChannels);
            });
        });
        context.get().setPacketHandled(true);
    }
}
