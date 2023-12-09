package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MultiItemInputRule extends Rule{

    private final ItemChecker items;
    private int lastSlot = 0;

    public MultiItemInputRule(RuleType ruleType, ItemChecker items, String value, int rate) {
        super(ruleType, value, rate);
        this.items = items;
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetFace).ifPresent(itemHandler -> {
            if (worked(itemHandler, lastSlot, channel)) return;
            int a = lastSlot;
            int maxSlots = itemHandler.getSlots();
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
        if (!items.contains(itemHandler.getStackInSlot(slot).getItem())) return false;
        ItemStack testStack = itemHandler.extractItem(slot, rate, true);
        if (testStack.isEmpty() || !items.contains(testStack.getItem())) return false;
        int count = channel.addItem(testStack);
        if (count > 0) {
            itemHandler.extractItem(slot, count, false);
            return true;
        }
        return false;
    }
}
