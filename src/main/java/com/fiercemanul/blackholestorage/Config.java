package com.fiercemanul.blackholestorage;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.LinkedList;
import java.util.List;

public class Config {

    public static ForgeConfigSpec.IntValue MAX_SIZE_PRE_CHANNEL;
    public static ForgeConfigSpec.IntValue MAX_CHANNELS_PRE_PLAYER;
    public static ForgeConfigSpec.IntValue MAX_PUBLIC_CHANNELS;
    public static ForgeConfigSpec.IntValue CHANNEL_FAST_UPDATE_RATE;
    public static ForgeConfigSpec.IntValue CHANNEL_FULL_UPDATE_RATE;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> INCOMPATIBLE_MODID;

    public static void register() {
        ForgeConfigSpec.Builder serverConfig = new ForgeConfigSpec.Builder();
        serverConfig.comment(
                        "                                             #",
                        "===---   Config for BlackHoleStorage   ---===#",
                        "                                             #"
        ).push("BlackHoleStorage");
        MAX_SIZE_PRE_CHANNEL = serverConfig.comment(
                "",
                "定义单个频道可储存的最大物品 .种类. 数。",
                "Define the maximum number of item .types. that can be stored in a single channel.",
                "Определение максимального количества .типов. элементов, которые могут храниться в одном канале."
        ).defineInRange("channelSize", 32768, 2048, Integer.MAX_VALUE);
        MAX_CHANNELS_PRE_PLAYER = serverConfig.comment(
                "",
                "定义单个玩家可以拥有的最大频道数。",
                "Defines the maximum number of channels a single player can have.",
                "Определяет максимальное количество каналов, которое может иметь один проигрыватель."
        ).defineInRange("maxPlayerChannels", 16, 4, 64);
        MAX_PUBLIC_CHANNELS = serverConfig.comment(
                "",
                "定义公有频道的最大频道数。",
                "Defines the maximum number of channels for a public channel.",
                "Определяет максимальное количество каналов для общедоступного канала."
        ).defineInRange("maxPublicChannels", 128, 32, 1024);
        serverConfig.pop();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverConfig.build());


        List<String> list = new LinkedList<>();
        list.add("pneumaticcraft");
        ForgeConfigSpec.Builder commonConfig = new ForgeConfigSpec.Builder();
        commonConfig.comment(
                        "                                             #",
                        "===---   Config for BlackHoleStorage   ---===#",
                        "                                             #"
        ).push("BlackHoleStorage");
        INCOMPATIBLE_MODID = commonConfig.comment(
                "",
                "定义不兼容的mod的id列表，防止一些的危险互动。",
                "Define a list of IDs for incompatible mods to prevent some dangerous interactions.",
                "Определите список идентификаторов для несовместимых модов, чтобы предотвратить некоторые опасные взаимодействия."
        ).defineList("incompatible_modid", list, o -> true);
        CHANNEL_FAST_UPDATE_RATE = commonConfig.comment(
                "",
                "定义频道增量更新包的发包速率，越小越快，1代表每秒更新20次，20代表每秒更新一次，注意这不代表实际发包速率，频道如无变化，不会进行发包。",
                "Define the packet sending rate of the channel incremental update package, the smaller the faster, 1 means 20 updates per second, 20 represents one update per second, note that this does not represent the actual packet sending rate, if the channel does not change, the packet will not be issued.",
                "Определите скорость отправки пакетов пакета инкрементального обновления канала, чем меньше, тем быстрее, 1 означает 20 обновлений в секунду, 20 представляет одно обновление в секунду, обратите внимание, что это не представляет фактическую скорость отправки пакета, если канал не изменится, пакет не будет выдан."
        ).defineInRange("fast_update_rate", 1, 1, 40);
        CHANNEL_FULL_UPDATE_RATE = commonConfig.comment(
                "",
                "类似上一个，这个包是完整更新包，发送整个频道信息。你通常不需要调整他们，除非你的网络环境非常苛刻。",
                "Similar to the previous one, this package is a complete update package that sends the entire channel information. You usually don't need to adjust them unless your network environment is very harsh.",
                "Как и предыдущий, этот пакет представляет собой полный пакет обновлений, который отправляет всю информацию о канале. Обычно вам не нужно настраивать их, если только ваша сетевая среда не очень сурова."
        ).defineInRange("full_update_rate", 40, 20, 1200);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonConfig.build());
    }
}
