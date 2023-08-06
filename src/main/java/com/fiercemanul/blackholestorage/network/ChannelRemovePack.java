package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChannelRemovePack {

    private final byte type;
    private final int id;

    public ChannelRemovePack(FriendlyByteBuf buf) {
        this.type = buf.readByte();
        this.id = buf.readInt();
    }

    public ChannelRemovePack(byte type, int id) {
        this.type = type;
        this.id = id;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeByte(type);
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientChannelManager.getInstance().removeChannel(type, id)));
        context.get().setPacketHandled(true);
    }
}
