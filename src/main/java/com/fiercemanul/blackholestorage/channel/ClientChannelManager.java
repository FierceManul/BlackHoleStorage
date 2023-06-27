package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = BlackHoleStorage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientChannelManager {

    private static volatile ClientChannelManager instance;
    private CompoundTag userCache;

    public static ClientChannelManager getInstance() {
        if (instance == null) {
            synchronized (ClientChannelManager.class) {
                if (instance == null) instance = new ClientChannelManager();
            }
        }
        return instance;
    }

    private static void newInstance() {
        if (instance == null) {
            synchronized (ClientChannelManager.class) {
                if (instance == null) instance = new ClientChannelManager();
            }
        }
    }

    public ClientChannelManager() {
    }

    public void setUserCache(CompoundTag userCache) {
        this.userCache = userCache;
    }

    public CompoundTag getUserCache() {
        return userCache;
    }

    public String getUserName(UUID uuid) {
        String userName = userCache.getCompound("nameCache").getString(uuid.toString());
        if (userName.equals("")) return "unknownUser";
        return userName;
    }
}
