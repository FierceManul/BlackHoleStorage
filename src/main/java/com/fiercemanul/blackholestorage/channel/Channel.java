package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public abstract class Channel {

    public final HashMap<String, Integer> storageItems = new HashMap<>();
    public int maxStorageSize = Config.MAX_SIZE_PRE_CHANNEL.get();

    public Channel() {}

    public abstract void onItemChanged(String itemId, boolean listChanged);

    public void addItem(ItemStack itemStack) {
        if (itemStack.hasTag() || itemStack.isEmpty()) return;
        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (storageItems.containsKey(itemId)) {
            int storageCount = storageItems.get(itemId);
            int remainingSpaces = Integer.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                storageItems.replace(itemId, storageCount + itemStack.getCount());
                itemStack.setCount(0);
            } else {
                storageItems.replace(itemId, Integer.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - remainingSpaces);
            }
            onItemChanged(itemId, false);
        } else {
            if (storageItems.size() >= maxStorageSize) return;
            storageItems.put(itemId, itemStack.getCount());
            itemStack.setCount(0);
            onItemChanged(itemId, true);
        }
    }

    public int addItem(String itemId, int count) {
        if (itemId.equals("minecraft:air") || count == 0) return 0;
        if (storageItems.containsKey(itemId)) {
            int storageCount = storageItems.get(itemId);
            int remainingSpaces = Integer.MAX_VALUE - storageCount;
            if (remainingSpaces >= count) {
                storageItems.replace(itemId, storageCount + count);
                onItemChanged(itemId, false);
                return 0;
            } else {
                storageItems.replace(itemId, Integer.MAX_VALUE);
                onItemChanged(itemId, false);
                return count - remainingSpaces;
            }
        } else {
            storageItems.put(itemId, count);
            onItemChanged(itemId, true);
            return 0;
        }
    }

    /**
     * 填充物品叠堆，不限制数量。
     * @param itemStack 要填充的物品
     * @param count 要填充的数量，负数为扣除。
     */
    public void fillItemStack(ItemStack itemStack, int count) {
        if (itemStack.isEmpty() || count == 0) return;
        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (storageItems.containsKey(itemId)) {
            int storageCount = storageItems.get(itemId);
            int remainingSpaces = Integer.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(itemId);
                itemStack.setCount(itemStack.getCount() + storageCount);
                onItemChanged(itemId, true);
            } else if (remainingSpaces < -count) {
                storageItems.replace(itemId, Integer.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - remainingSpaces);
                onItemChanged(itemId, false);
            } else {
                storageItems.replace(itemId, storageCount - count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, false);
            }
        } else {
            if (count < 0) {
                if (storageItems.size() >= maxStorageSize) return;
                storageItems.put(itemId, -count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, true);
            }
        }
    }

    public void takeItem(ItemStack itemStack) {
        fillItemStack(itemStack, itemStack.getMaxStackSize());
    }

    /**
     * 从频道获取物品，但不限制数量。
     */
    public ItemStack takeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        int storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = storageCount;
            onItemChanged(itemId, true);
        }
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), count);
    }

    /**
     * 从频道获取物品，数量限制在叠堆最大值。
     */
    public ItemStack saveTakeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), 1);
        count = Integer.min(count, itemStack.getMaxStackSize());
        int storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = storageCount;
            onItemChanged(itemId, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public abstract boolean isRemoved();

}
