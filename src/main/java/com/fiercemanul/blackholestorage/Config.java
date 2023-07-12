package com.fiercemanul.blackholestorage;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec.IntValue MAX_SIZE_PRE_CHANNEL;
    public static ForgeConfigSpec.IntValue MAX_CHANNELS_PRE_PLAYER;
    public static ForgeConfigSpec.IntValue MAX_PUBLIC_CHANNELS;

    public static void register() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment(
                "                                             #",
                "===---   Config for BlackHoleStorage   ---===#",
                "                                             #")
                .push("BlackHoleStorage");

        MAX_SIZE_PRE_CHANNEL = builder.comment(
                        "",
                        "定义单个频道可储存的最大物品 .种类. 数。",
                        "Define the maximum number of item .types. that can be stored in a single channel.",
                        "Определение максимального количества .типов. элементов, которые могут храниться в одном канале."
                )
                .defineInRange("channelSize", 32768, 2048, Integer.MAX_VALUE);

        MAX_CHANNELS_PRE_PLAYER = builder.comment(
                        "",
                        "定义单个玩家可以拥有的最大频道数。",
                        "Defines the maximum number of channels a single player can have.",
                        "Определяет максимальное количество каналов, которое может иметь один проигрыватель."
                )
                .defineInRange("maxPlayerChannels", 16, 4, 64);

        MAX_PUBLIC_CHANNELS = builder.comment(
                        "",
                        "定义公有频道的最大频道数。",
                        "Defines the maximum number of channels for a public channel.",
                        "Определяет максимальное количество каналов для общедоступного канала."
                )
                .defineInRange("maxPublicChannels", 128, 32, 1024);

        builder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }
}
