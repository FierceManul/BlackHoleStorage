package com.fiercemanul.blackholestorage.channel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IChannelTerminal {

    UUID getTerminalOwner();
    @Nullable
    ChannelInfo getChannelInfo();
    void setChannel(UUID channelOwner, int channelID);
    void removeChannel(ServerPlayer actor);
    void renameChannel(ServerPlayer actor, String name);
    void addChannelSelector(ServerPlayer player);
    void removeChannelSelector(ServerPlayer player);
    boolean stillValid();
    void tryReOpenMenu(ServerPlayer player);
}
