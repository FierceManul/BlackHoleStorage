package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.Config;
import com.fiercemanul.blackholestorage.network.*;
import com.fiercemanul.blackholestorage.util.Tools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
import java.io.IOException;
import java.util.HashMap;
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
    private boolean loadSuccess = true;
    private final MinecraftServer server;
    private final NullChannel nullChannel = NullChannel.INSTANCE;
    private final HashMap<UUID, HashMap<Integer, ServerChannel>> channelList = new HashMap<>();
    /**
     * <玩家，终端主人>
     */
    private final HashMap<ServerPlayer, UUID> channelSelector = new HashMap<>();

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        this.userCache.getCompound("nameCache").putString(event.getEntity().getUUID().toString(), event.getEntity().getGameProfile().getName());
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new NameCachePack(userCache));
        if (!loadSuccess) event.getEntity().getServer().getPlayerList().broadcastSystemMessage(Component.translatable("blackholestorage.load_error"), false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        int tickCount = event.getServer().getTickCount();
        if (tickCount % Config.CHANNEL_FULL_UPDATE_RATE.get() == 0) channelList.forEach((uuid, map) -> map.forEach((id, channel) -> channel.sendFullUpdate()));
        else if (tickCount % Config.CHANNEL_FAST_UPDATE_RATE.get() == 0) channelList.forEach((uuid, map) -> map.forEach((id, channel) -> channel.sendUpdate()));
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Save event) {
        if (isOverworld(event.getLevel())) save(event.getLevel().getServer());
    }

    @SubscribeEvent
    public void onServerDown(ServerStoppingEvent event) {
        this.save(event.getServer());
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
            if (!saveDataPath.exists()) saveDataPath.mkdirs();

            File userCacheFile = new File(saveDataPath, "UserCache.dat");
            if (userCacheFile.exists() && userCacheFile.isFile()) {
                this.userCache = NbtIo.readCompressed(userCacheFile);
                if (!this.userCache.contains("nameCache")) this.initializeNameCache();
            } else {
                this.initializeNameCache();
            }
            BlackHoleStorage.LOGGER.info("用户名缓存加载成功");

            File[] channelDirs = saveDataPath.listFiles(pathname -> pathname.isDirectory() && pathname.getName()
                    .matches(Tools.UUID_REGEX));
            if (channelDirs != null) {
                for (File dir : channelDirs) {
                    UUID player = UUID.fromString(dir.getName());
                    File[] channels = dir.listFiles(pathname -> pathname.isFile() && pathname.getName().matches("^(0|[1-9][0-9]{0,3})\\.dat$"));
                    if (channels == null) continue;
                    HashMap<Integer, ServerChannel> playerChannels = new HashMap<>();
                    for (File channelFile : channels) {
                        CompoundTag channelDat = NbtIo.readCompressed(channelFile);
                        int channelID = Integer.parseInt(channelFile.getName().substring(0, channelFile.getName().length() - 4));
                        ServerChannel channel = new ServerChannel(channelDat);
                        playerChannels.put(channelID, channel);
                        BlackHoleStorage.LOGGER.info("成功加载频道： " + dir.getName() + "——" + channelID + "——" + channel.getName());
                    }
                    channelList.put(player, playerChannels);
                }
            }
            BlackHoleStorage.LOGGER.info("黑洞储存数据加载完毕");

        } catch (Exception e) {
            loadSuccess = false;
            throw new RuntimeException("黑洞储存在加载数据的时候出错了！ 本次游戏将不会保存数据！", e);
        }
    }

    private void save(MinecraftServer server) {
        if (!loadSuccess) return;
        try {
            File userCache = new File(saveDataPath, "UserCache.dat");
            if (!userCache.exists()) userCache.createNewFile();
            NbtIo.writeCompressed(this.userCache, userCache);
            BlackHoleStorage.LOGGER.info("成功保存用户名缓存");

            channelList.forEach((uuid, channels) -> {
                File user = new File(saveDataPath, uuid.toString());
                if (!user.exists()) user.mkdir();
                channels.forEach((id, channel) -> {
                    File channelDat = new File(user, id + ".dat");
                    try {
                        if (!channelDat.exists()) channelDat.createNewFile();
                        NbtIo.writeCompressed(channel.buildData(), channelDat);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    BlackHoleStorage.LOGGER.info("成功保存频道： " + uuid + "——" + id + "——" + channel.getName());
                });
            });

        } catch (Exception e) {
            server.getPlayerList().broadcastSystemMessage(Component.translatable("blackholestorage.save_error"), false);
            throw new RuntimeException("黑洞储存在保存数据的时候出错了！ 什么情况呢？", e);
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
        if (channelList.containsKey(ownerUUID)) {
            HashMap<Integer, ServerChannel> list = channelList.get(ownerUUID);
            if (list.containsKey(channelId)) return list.get(channelId);
        }
        return nullChannel;
    }

    public String getChannelName(UUID ownerUUID, int id) {
        if (!channelList.containsKey(ownerUUID)) return "RemovedChannel";
        if (!channelList.get(ownerUUID).containsKey(id)) return "RemovedChannel";
        return channelList.get(ownerUUID).get(id).getName();
    }

    public void addChannelSelector(ServerPlayer player, UUID otherUUID) {
        channelSelector.put(player, otherUUID);

        CompoundTag myChannels = new CompoundTag();
        CompoundTag otherChannels = new CompoundTag();
        CompoundTag publicChannels = new CompoundTag();

        if (channelList.containsKey(player.getUUID()))
            channelList.get(player.getUUID()).forEach((id, channel) -> myChannels.putString(String.valueOf(id), channel.getName()));

        if (!player.getUUID().equals(otherUUID) && channelList.containsKey(otherUUID))
            channelList.get(otherUUID).forEach((id, channel) -> otherChannels.putString(String.valueOf(id), channel.getName()));

        if (channelList.containsKey(BlackHoleStorage.FAKE_PLAYER_UUID))
            channelList.get(BlackHoleStorage.FAKE_PLAYER_UUID).forEach((id, channel) -> publicChannels.putString(String.valueOf(id), channel.getName()));

        if (!myChannels.isEmpty() || !otherChannels.isEmpty() || !publicChannels.isEmpty())
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelListPack(myChannels, otherChannels, publicChannels));
    }

    public void tryAddChannel(ServerPlayer player, String name, boolean pub) {
        int slotId = player.getInventory().findSlotMatchingItem(new ItemStack(BlackHoleStorage.STORAGE_CORE.get()));
        if (slotId <= -1) return;
        UUID uuid = pub ? BlackHoleStorage.FAKE_PLAYER_UUID : player.getUUID();
        int max = pub ? Config.MAX_PUBLIC_CHANNELS.get() : Config.MAX_CHANNELS_PRE_PLAYER.get();
        HashMap<Integer, ServerChannel> playerChannels;
        if (channelList.containsKey(uuid)) playerChannels = channelList.get(uuid);
        else {
            playerChannels = new HashMap<>();
            channelList.put(uuid, playerChannels);
        }
        if (playerChannels.size() >= max) return;
        for (int i = 0; i < playerChannels.size() + 1; i++) {
            if (playerChannels.containsKey(i)) continue;
            playerChannels.put(i, new ServerChannel(name));
            player.getInventory().getItem(slotId).shrink(1);
            sandChannelAdd(uuid, name, i);
            BlackHoleStorage.LOGGER.info("添加了频道： " + uuid + "——" + i + "——" + name);
            break;
        }
    }

    private void sandChannelAdd(UUID channelOwner, String name, int id) {
        if (channelOwner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
            channelSelector.forEach((player, other) ->
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelAddPack((byte) 2, name, id)));
        } else {
            channelSelector.forEach((player, other) -> {
                if (player.getUUID().equals(channelOwner)) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelAddPack((byte) 0, name, id));
                } else if (channelOwner.equals(other)) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelAddPack((byte) 1, name, id));
                }
            });
        }
    }

    public boolean tryRemoveChannel(UUID channelOwner, int id) {
        if (!channelList.containsKey(channelOwner)) return false;
        HashMap<Integer, ServerChannel> list = channelList.get(channelOwner);
        if (!list.containsKey(id)) return false;
        ServerChannel channel = list.get(id);
        if (!channel.isEmpty()) return false;
        if (list.remove(id) != null) {
            channel.setRemoved();
            sandChannelRemove(channelOwner, id);
            File file = new File(saveDataPath, channelOwner + "/" + id + ".dat");
            file.delete();
            return true;
        }
        return false;
    }

    private void sandChannelRemove(UUID channelOwner, int id) {
        if (channelOwner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
            channelSelector.forEach((player, other) ->
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelRemovePack((byte) 2, id)));
        } else {
            channelSelector.forEach((player, other) -> {
                if (player.getUUID().equals(channelOwner)) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelRemovePack((byte) 0, id));
                } else if (channelOwner.equals(other)) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelRemovePack((byte) 1, id));
                }
            });
        }
    }

    public void renameChannel(ChannelInfo info, String name) {
        if (!channelList.containsKey(info.owner())) return;
        var list = channelList.get(info.owner());
        if (!list.containsKey(info.id())) return;
        ServerChannel channel = list.get(info.id());
        channel.setName(name);
        sandChannelAdd(info.owner(), name, info.id());
    }

    public void removeChannelSelector(ServerPlayer player) {
        channelSelector.remove(player);
    }


    public static void sendChannelSet(ServerPlayer player, UUID terminalOwner, UUID channelOwner, int channelId) {
        String name = ServerChannelManager.getInstance().getChannelName(channelOwner, channelId);
        if (player.getUUID().equals(channelOwner)) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelSetPack((byte) 0, channelId, name));
        } else if (terminalOwner.equals(channelOwner)) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelSetPack((byte) 1, channelId, name));
        } else if (BlackHoleStorage.FAKE_PLAYER_UUID.equals(channelOwner)) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelSetPack((byte) 2, channelId, name));
        } else if (!name.isEmpty()) {
            //频道名不为空，代表选择的频道是非用户非设备所有人非公有的其他人
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ChannelSetPack((byte) -1, -1, name));
        }
    }

}
