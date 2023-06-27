package com.fiercemanul.blackholestorage.network;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NameCachePack {

    private final CompoundTag userCache;

    public NameCachePack(FriendlyByteBuf buf) {
        this.userCache = buf.readNbt();
    }

    public NameCachePack(CompoundTag userCache) {
        this.userCache = userCache;
    }

    public void makePack(FriendlyByteBuf buf) {
        buf.writeNbt(userCache);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientChannelManager.getInstance().setUserCache(userCache);
            });
        });
        context.get().setPacketHandled(true);
    }
}
