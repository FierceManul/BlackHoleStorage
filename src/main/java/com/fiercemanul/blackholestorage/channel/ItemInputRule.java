package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public class ItemInputRule extends Rule{

    private int lastSlot = 0;
    private final Item item;

    public ItemInputRule(Item item, String value, int rate) {
        super(RuleType.ITEM, value, rate);
        this.item = item;
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, targetFace).ifPresent(itemHandler -> {
            if (!channel.canStorageItem(value)) return;
            if (worked(itemHandler, lastSlot, channel)) return;
            int maxSlots = itemHandler.getSlots();
            int a = lastSlot;
            for (int i = a; i < maxSlots; i++) {
                if (worked(itemHandler, i, channel)) {
                    lastSlot = i;
                    return;
                }
            }
            for (int i = 0; i < a; i++) {
                if (worked(itemHandler, i, channel)) {
                    lastSlot = i;
                    return;
                }
            }
            lastSlot = 0;
        });
    }

    private boolean worked(IItemHandler itemHandler, int slot, ServerChannel channel) {
        if (!itemHandler.getStackInSlot(slot).getItem().equals(item)) return false;
        ItemStack testStack = itemHandler.extractItem(slot, rate, true);
        if (testStack.isEmpty() || !testStack.getItem().equals(item)) return false;
        int count = channel.addItem(testStack);
        if (count > 0) {
            itemHandler.extractItem(slot, count, false);
            return true;
        }
        return false;
    }
}
