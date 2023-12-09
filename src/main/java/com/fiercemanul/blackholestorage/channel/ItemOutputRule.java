package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;

public class ItemOutputRule extends Rule{

    private int lastSlot = 0;
    private final Item item;

    public ItemOutputRule(Item item, String value, int rate) {
        super(RuleType.ITEM, value, rate);
        this.item = item;
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetFace).ifPresent(itemHandler -> {
            if (!channel.hasItem(value)) return;
            ItemStack testStack = new ItemStack(item, Integer.min(channel.getItemAmount(value), rate));
            ItemStack afterStack = itemHandler.insertItem(lastSlot, testStack, true);
            int count = testStack.getCount() - afterStack.getCount();
            if (count <= 0) {
                lastSlot = 0;
                int maxSlots = itemHandler.getSlots();
                for (int i = 0; i < maxSlots; i++) {
                    afterStack = itemHandler.insertItem(i, testStack, true);
                    count = testStack.getCount() - afterStack.getCount();
                    if (count > 0) {
                        lastSlot = i;
                        itemHandler.insertItem(lastSlot, channel.takeItem(value, count), false);
                        break;
                    }
                }
            } else itemHandler.insertItem(lastSlot, channel.takeItem(value, count), false);
        });
    }
}
