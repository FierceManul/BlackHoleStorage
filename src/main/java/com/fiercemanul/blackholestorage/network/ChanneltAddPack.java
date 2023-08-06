package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChanneltAddPack {

    private final byte type;
    private final String name;
    private final int id;

    public ChanneltAddPack(FriendlyByteBuf buf) {
        this.type = buf.readByte();
        this.name = buf.readUtf();
        this.id = buf.readInt();
    }

    public ChanneltAddPack(byte type, String name, int id) {
        this.type = type;
        this.name = name;
        this.id = id;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeByte(type);
        buf.writeUtf(name);
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientChannelManager.getInstance().addChannel(type, name, id)));
        context.get().setPacketHandled(true);
    }
}
