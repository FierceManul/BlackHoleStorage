package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class AnyItemtRule extends Rule {

    private int lastSlot = 0;

    public AnyItemtRule(int rate) {
        super(RuleType.ANY_ITEM, "", rate);
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetFace).ifPresent(itemHandler -> {
            if (!channel.canStorageItem(value)) return;
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
        ItemStack testStack = itemHandler.extractItem(slot, rate, true);
        int count = channel.addItem(testStack);
        if (count > 0) {
            itemHandler.extractItem(slot, count, false);
            return true;
        }
        return false;
    }
}
