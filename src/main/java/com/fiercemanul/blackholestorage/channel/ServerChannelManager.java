package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.Config;
import com.fiercemanul.blackholestorage.network.NameCachePack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BlackHoleStorage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerChannelManager {

    private static volatile ServerChannelManager instance;

    public static ServerChannelManager getInstance() {
        if (instance == null) {
            synchronized (ServerChannelManager.class) {
                if (instance == null) {
                    instance = new ServerChannelManager(ServerLifecycleHooks.getCurrentServer());
                }
            }
        }
        return instance;
    }

    private static void newInstance(MinecraftServer server) {
        if (instance == null) {
            synchronized (ServerChannelManager.class) {
                if (instance == null) instance = new ServerChannelManager(server);
            }
        }
    }

    @SubscribeEvent
    public static void onServerLoad(ServerAboutToStartEvent event) {
        newInstance(event.getServer());
    }





    private CompoundTag userCache;
    private File saveDataPath;
    private final MinecraftServer server;
    private NullChannel nullChannel = new NullChannel();
    private ServerChannel channel = new ServerChannel();

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        this.userCache.getCompound("nameCache").putString(event.getEntity().getUUID().toString(), event.getEntity().getGameProfile().getName());
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new NameCachePack(userCache));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        int tickCount = event.getServer().getTickCount();
        if (channel == null) return;
        if (tickCount % Config.CHANNEL_FULL_UPDATE_RATE.get() == 0) channel.sendFullUpdate();
        else if (tickCount % Config.CHANNEL_FAST_UPDATE_RATE.get() == 0) channel.sendUpdate();
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

    private ServerChannelManager(MinecraftServer server) {
        this.server = server;
        MinecraftForge.EVENT_BUS.register(this);
        this.load();
    }

    private void load() {
        this.saveDataPath = new File(server.getWorldPath(LevelResource.ROOT).toFile(), "data/BlackHoleStorage");
        try {
            if(!saveDataPath.exists()) saveDataPath.mkdirs();

            File userCacheFile = new File(saveDataPath, "UserCache.dat");
            if(userCacheFile.exists() && userCacheFile.isFile()){
                this.userCache = NbtIo.readCompressed(userCacheFile);
                if (!this.userCache.contains("nameCache")) this.initializeNameCache();
            } else {
                this.initializeNameCache();
            }

            File channelFile = new File(saveDataPath, "Channel.dat");
            if (channelFile.exists() && channelFile.isFile()) {
                CompoundTag channelDat = NbtIo.readCompressed(channelFile);
                channel.initialize(channelDat);
            }


        } catch (Exception e) {
            throw new RuntimeException("BlackHoleStorage was unable to read it's data!", e);
        }
    }

    private void save() {
        try {
            File userCache = new File(saveDataPath, "UserCache.dat");
            if (!userCache.exists()) userCache.createNewFile();
            NbtIo.writeCompressed(this.userCache, userCache);

            File channelFile = new File(saveDataPath, "Channel.dat");
            if (!channelFile.exists()) channelFile.createNewFile();
            NbtIo.writeCompressed(this.channel.buildData(), channelFile);

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

    public ServerChannel getChannel(UUID ownerUUID, int channelId) {
        return channel;
    }

}
