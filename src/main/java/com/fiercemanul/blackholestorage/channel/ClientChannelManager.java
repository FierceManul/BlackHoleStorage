package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = BlackHoleStorage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientChannelManager {

    private static volatile ClientChannelManager instance;
    private CompoundTag userCache;
    private ClientChannel channel = new ClientChannel();

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

    @SubscribeEvent
    public static void onLoggingInServer(ClientPlayerNetworkEvent.LoggingIn event) {
        newInstance();
    }

    @SubscribeEvent
    public void onLoggingOutServer(ClientPlayerNetworkEvent.LoggingOut event) {
        MinecraftForge.EVENT_BUS.unregister(this);
        instance = null;
    }




    public ClientChannelManager() {
        MinecraftForge.EVENT_BUS.register(this);
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

    public ClientChannel getChannel() {
        return channel;
    }

    public ClientChannel getChannel(ControlPanelMenu.DummyContainer container) {
        channel.addListener(container);
        return channel;
    }

    public void updateChannel(CompoundTag data) {
        channel.update(data);
    }

    public void fullUpdateChannel(CompoundTag data) {
        channel.fullUpdate(data);
    }
}
