package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MultiItemInputRule extends Rule{

    private final ItemChecker items;
    private int lastSlot = 0;

    public MultiItemInputRule(RuleType ruleType, ItemChecker items, String value, int rate) {
        super(ruleType, value, rate);
        this.items = items;
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, targetFace).ifPresent(itemHandler -> {
            if (worked(itemHandler, lastSlot, channel)) return;
            lastSlot = 0;
            int maxSlots = itemHandler.getSlots();
            for (int i = 0; i < maxSlots; i++) {
                if (worked(itemHandler, i, channel)) {
                    lastSlot = i;
                    return;
                }
            }
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
