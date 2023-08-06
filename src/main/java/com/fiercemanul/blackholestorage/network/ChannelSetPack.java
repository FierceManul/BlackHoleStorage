package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChannelSetPack {

    private final byte type;
    private final int id;
    private final String name;

    public ChannelSetPack(FriendlyByteBuf buf) {
        this.type = buf.readByte();
        this.id = buf.readInt();
        this.name = buf.readUtf();
    }

    public ChannelSetPack(byte type, int id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeByte(type);
        buf.writeInt(id);
        buf.writeUtf(name);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientChannelManager.getInstance().setSelectedChannel(type, id, name)));
        context.get().setPacketHandled(true);
    }
}
