package com.fiercemanul.blackholestorage.channel;

import net.minecraft.world.item.Item;

public final class ItemChecker {

    private final Item[] items;
    public final int length;
    private int lastIndex = 0;

    public ItemChecker(Item[] items) {
        this.items = items;
        this.length = items.length;
    }


    public Item get(int index) {
        if (index >= length || index < 0) return items[0];
        return items[index];
    }

    public boolean contains(Item otherItem) {
        if (items[lastIndex] == otherItem) return true;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == otherItem) {
                lastIndex = i;
                return true;
            }
        }
        return false;
    }
}
