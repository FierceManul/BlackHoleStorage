package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ServerChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AddChannelPack {

    private final String name;
    private final boolean pub;

    public AddChannelPack(FriendlyByteBuf buf) {
        this.name = buf.readUtf(64);
        this.pub = buf.readBoolean();
    }

    public AddChannelPack(String name, boolean pub) {
        this.name = name;
        this.pub = pub;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeUtf(name, 64);
        buf.writeBoolean(pub);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (name.isEmpty()) return;
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            ServerChannelManager.getInstance().tryAddChannel(player, name, pub);
        });
        context.get().setPacketHandled(true);
    }
}
