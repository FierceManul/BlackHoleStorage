package com.fiercemanul.blackholestorage.channel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public class Channel {
    public HashMap<String, Integer> storageItems = new HashMap<>();
    private final int maxStorageSize = 32768;
    public Channel() {}

    public Channel(HashMap<String, Integer> storageItems) {
        this.storageItems = storageItems;
    }

    public void addItem(ItemStack itemStack) {
        if (itemStack.hasTag() || itemStack.isEmpty() || storageItems.size() >= maxStorageSize) return;
        String id = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (storageItems.containsKey(id)) {
            int storageCount = storageItems.get(id);
            int remainingSpaces = Integer.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                storageItems.replace(id, storageCount + itemStack.getCount());
                itemStack.setCount(0);
            } else {
                storageItems.replace(id, Integer.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - remainingSpaces);
            }
        } else {
            storageItems.put(id, itemStack.getCount());
            itemStack.setCount(0);
        }
    }

    public int addItem(String itemId, int count) {
        if (itemId.equals("minecraft:air") || count == 0) return 0;
        if (storageItems.containsKey(itemId)) {
            int storageCount = storageItems.get(itemId);
            int remainingSpaces = Integer.MAX_VALUE - storageCount;
            if (remainingSpaces >= count) {
                storageItems.replace(itemId, storageCount + count);
                return 0;
            } else {
                storageItems.replace(itemId, Integer.MAX_VALUE);
                return count - remainingSpaces;
            }
        } else {
            storageItems.put(itemId, count);
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
        String id = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (storageItems.containsKey(id)) {
            int storageCount = storageItems.get(id);
            int remainingSpaces = Integer.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(id);
                itemStack.setCount(itemStack.getCount() + storageCount);
            } else if (remainingSpaces < -count) {
                storageItems.replace(id, Integer.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - remainingSpaces);
            } else {
                storageItems.replace(id, storageCount - count);
                itemStack.setCount(itemStack.getCount() + count);
            }
        } else {
            if (count < 0) {
                storageItems.put(id, -count);
                itemStack.setCount(itemStack.getCount() + count);
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
        if (count >= storageCount) {
            storageItems.remove(itemId);
            count = storageCount;
        } else {
            storageItems.replace(itemId, storageCount - count);
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
        if (count >= storageCount) {
            storageItems.remove(itemId);
            count = storageCount;
        } else {
            storageItems.replace(itemId, storageCount - count);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public void cleanEmpty() {
        storageItems.remove("minecraft:air");
        storageItems.remove(null);
        storageItems.forEach((id, count) -> {
            if (count <= 0) storageItems.remove(id);
        });
    }
}
