package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.network.NameCachePack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.*;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BlackHoleStorage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerChannelManager {

    private static volatile ServerChannelManager instance;
    private CompoundTag userCache;
    private File saveDataPath;

    public static ServerChannelManager getInstance() {
        if (instance == null) {
            synchronized (ServerChannelManager.class) {
                if (instance == null) instance = new ServerChannelManager();
            }
        }
        return instance;
    }

    private static void newInstance() {
        if (instance == null) {
            synchronized (ServerChannelManager.class) {
                if (instance == null) instance = new ServerChannelManager();
            }
        }
    }

    @SubscribeEvent
    public static void onServerLoad(ServerAboutToStartEvent event) {
        newInstance();
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        this.userCache.getCompound("nameCache").putString(event.getEntity().getUUID().toString(), event.getEntity().getGameProfile().getName());
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new NameCachePack(userCache));
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Save event) {
        if (isOverworld(event.getLevel())) save();
    }

    @SubscribeEvent
    public void onServerDown(ServerStoppingEvent event) {
        this.save();
        MinecraftForge.EVENT_BUS.unregister(this);
        instance = null;
    }

    private ServerChannelManager() {
        MinecraftForge.EVENT_BUS.register(this);
        this.load();
    }

    private void load() {
        var server = ServerLifecycleHooks.getCurrentServer();
        this.saveDataPath = new File(server.getWorldPath(LevelResource.ROOT).toFile(), "data/BlackHoleStorage");
        try {
            if(!saveDataPath.exists()) saveDataPath.mkdirs();
            File userCacheFile = new File(saveDataPath, "UserCache.dat");
            if(userCacheFile.exists() && userCacheFile.isFile()){
                DataInputStream inputStream = new DataInputStream(new FileInputStream(userCacheFile));
                this.userCache = NbtIo.readCompressed(inputStream);
                inputStream.close();
                if (!this.userCache.contains("nameCache")) this.initializeNameCache();
            } else {
                this.initializeNameCache();
            }
        } catch (Exception e) {
            throw new RuntimeException("BlackHoleStorage was unable to read it's data!", e);
        }
    }

    private void save() {
        try {
            File userCache = new File(saveDataPath, "UserCache.dat");
            if (!userCache.exists()) userCache.createNewFile();
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(userCache));
            NbtIo.writeCompressed(this.userCache, outputStream);
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("BlackHoleStorage was unable to save it's data!", e);
        }
    }

    private void initializeUserCache() {
        this.userCache = new CompoundTag();
        this.userCache.putInt("dataVersion", 1);
    }

    private void initializeNameCache() {
        CompoundTag nameCache = new CompoundTag();
        nameCache.putString(BlackHoleStorage.FAKE_PLAYER_UUID.toString(), BlackHoleStorage.FAKE_PLAYER_NAME);
        if (userCache == null) this.initializeUserCache();
        this.userCache.put("nameCache", nameCache);
    }
    
    private boolean isOverworld(LevelAccessor level) {
        return !level.isClientSide() && level.equals(level.getServer().getLevel(Level.OVERWORLD));
    }

    public CompoundTag getUserCache() {
        return userCache;
    }

    public String getUserName(UUID uuid) {
        String userName = userCache.getCompound("nameCache").getString(uuid.toString());
        if (userName.equals("")) {
            userCache.getCompound("nameCache").putString(uuid.toString(), "unknownUser");
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new NameCachePack(userCache));
            userName = "unknownUser";
        }
        return userName;
    }
}
