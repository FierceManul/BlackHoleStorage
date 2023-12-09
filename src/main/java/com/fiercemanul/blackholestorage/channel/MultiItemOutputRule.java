package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.util.Tools;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MultiItemOutputRule extends Rule {

    private final ItemChecker items;
    private int lastSlot = 0;
    private int lastIndex = 0;

    public MultiItemOutputRule(RuleType ruleType, ItemChecker items, String value, int rate) {
        super(ruleType, value, rate);
        this.items = items;
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetFace).ifPresent(itemHandler -> {
            for (int i = 0; i < items.length; i++) {
                if (output(itemHandler, channel)) return;
                else nextItem();
            }
        });
    }

    private void nextItem() {
        lastIndex++;
        if (lastIndex >= items.length) lastIndex = 0;
    }

    private boolean output(IItemHandler itemHandler, ServerChannel channel) {
        Item item = items.get(lastIndex);
        String itemId = Tools.getItemId(item);
        if (!channel.hasItem(itemId)) return false;
        ItemStack testStack = new ItemStack(item, Integer.min(channel.getItemAmount(itemId), rate));
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
                    itemHandler.insertItem(lastSlot, channel.takeItem(itemId, count), false);
                    return true;
                }
            }
        } else {
            itemHandler.insertItem(lastSlot, channel.takeItem(itemId, count), false);
            return true;
        }
        return false;
    }
}
