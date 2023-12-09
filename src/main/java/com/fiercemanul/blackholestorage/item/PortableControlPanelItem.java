package com.fiercemanul.blackholestorage.item;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenuProvider;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenuProvider;
import com.fiercemanul.blackholestorage.gui.ItemChannelTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PortableControlPanelItem extends Item {
    public PortableControlPanelItem(Properties properties) {
        super(properties.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (Minecraft.getInstance().player == null) return;
        if (!pStack.hasTag()) return;
        if (pStack.getTag().contains("owner")) {
            CompoundTag nbt = pStack.getTag();
            UUID selfUUID = Minecraft.getInstance().player.getUUID();
            UUID ownerUUID = nbt.getUUID("owner");
            String ownerName = ClientChannelManager.getInstance().getUserName(nbt.getUUID("owner"));
            boolean lock = nbt.getBoolean("locked");
            if (selfUUID.equals(ownerUUID)) pTooltipComponents.add(new TranslatableComponent("bhs.GUI.owner", "§a" + ownerName));
            else if (lock) pTooltipComponents.add(new TranslatableComponent("bhs.GUI.owner", "§c" + ownerName));
            else pTooltipComponents.add(new TranslatableComponent("bhs.GUI.owner", ownerName));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ItemStack panel = pPlayer.getItemInHand(pUsedHand);
            int slotIndex = pUsedHand.equals(InteractionHand.MAIN_HAND) ? pPlayer.getInventory().selected : 40;
            CompoundTag nbt = panel.getOrCreateTag();
            if (!nbt.contains("owner")) {
                nbt.putUUID("owner", pPlayer.getUUID());
                nbt.putBoolean("locked", false);
                if (!nbt.contains("craftingMode")) nbt.putBoolean("craftingMode", false);
                if (!nbt.contains("filter")) nbt.putString("filter", "");
                if (!nbt.contains("sortType")) nbt.putByte("sortType", (byte) 4);
                if (!nbt.contains("viewType")) nbt.putByte("viewType", (byte) 0);
            }
            if (nbt.contains("channel")) {
                NetworkHooks.openGui((ServerPlayer) pPlayer, new ControlPanelMenuProvider(slotIndex), buf -> {
                    buf.writeBlockPos(BlockPos.ZERO);
                    buf.writeInt(slotIndex);
                    buf.writeUUID(nbt.getUUID("owner"));
                    buf.writeBoolean(nbt.getBoolean("locked"));
                    buf.writeBoolean(nbt.getBoolean("craftingMode"));
                    buf.writeUtf(nbt.getString("filter"));
                    buf.writeByte(nbt.getByte("sortType"));
                    buf.writeByte(nbt.getByte("viewType"));
                    buf.writeUUID(nbt.getCompound("channel").getUUID("channelOwner"));
                    buf.writeInt(nbt.getCompound("channel").getInt("channelID"));
                });
            } else {
                NetworkHooks.openGui((ServerPlayer) pPlayer, new ChannelSelectMenuProvider(new ItemChannelTerminal(pPlayer.getInventory(), panel, slotIndex)), buf -> {});
            }
        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
    }
}
