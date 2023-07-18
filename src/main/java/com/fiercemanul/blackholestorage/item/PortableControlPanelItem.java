package com.fiercemanul.blackholestorage.item;

import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class PortableControlPanelItem extends Item {
    public PortableControlPanelItem(Properties properties) {
        super(properties.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ItemStack panel = pPlayer.getItemInHand(pUsedHand);
            int slotIndex = pUsedHand.equals(InteractionHand.MAIN_HAND) ? pPlayer.getInventory().selected : 40;
            CompoundTag nbt = panel.getOrCreateTag();
            if (!nbt.contains("owner")) nbt.putUUID("owner", pPlayer.getUUID());
            if (!nbt.contains("locked")) nbt.putBoolean("locked", false);
            if (!nbt.contains("craftingMode")) nbt.putBoolean("craftingMode", false);
            if (!nbt.contains("filter")) nbt.putString("filter", "");
            if (!nbt.contains("sortType")) nbt.putInt("sortType", 4);

            MenuProvider containerProvider = new MenuProvider() {

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new ControlPanelMenu(containerId, player, null, slotIndex);
                }

                @Override
                public Component getDisplayName() {
                    return Component.translatable("screen.bhs.control_panel");
                }
            };

            NetworkHooks.openScreen((ServerPlayer) pPlayer, containerProvider, buf -> {
                buf.writeBlockPos(BlockPos.ZERO);
                buf.writeInt(slotIndex);
                buf.writeUUID(nbt.getUUID("owner"));
                buf.writeBoolean(nbt.getBoolean("locked"));
                buf.writeBoolean(nbt.getBoolean("craftingMode"));
                buf.writeUtf(nbt.getString("filter"));
                buf.writeInt(nbt.getInt("sortType"));
            });

        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
    }
}
