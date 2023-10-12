package com.fiercemanul.blackholestorage.channel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;

public final class ItemCheckers {

    private static final HashMap<String, ItemChecker> TAG_ITEM_MAP = new HashMap<>();
    private static final HashMap<String, ItemChecker> MOD_ITEM_MAP = new HashMap<>();

    public static ItemChecker getCheckersFromTag(ResourceLocation tag) {
        String s = tag.toString();
        if (TAG_ITEM_MAP.containsKey(s)) return TAG_ITEM_MAP.get(s);
        ArrayList<Item> itemArrayList = new ArrayList<>();
        String namespace = tag.getNamespace();
        String path = tag.getPath();
        ForgeRegistries.ITEMS.forEach(item -> {
            if (item.builtInRegistryHolder().tags().anyMatch(
                    tagKey -> tagKey.location().getPath().equals(path) && tagKey.location().getNamespace().equals(namespace)
            )) itemArrayList.add(item);
            else if (item instanceof BlockItem blockItem
                    && blockItem.getBlock().builtInRegistryHolder().tags().anyMatch(
                    tagKey -> tagKey.location().getPath().equals(path) && tagKey.location().getNamespace().equals(namespace)
            )) itemArrayList.add(item);
        });
        ItemChecker checker = new ItemChecker(itemArrayList.toArray(new Item[]{}));
        TAG_ITEM_MAP.put(s, checker);
        return checker;
    }

    public static ItemChecker getCheckersFromMod(String modId) {
        if (MOD_ITEM_MAP.containsKey(modId)) return MOD_ITEM_MAP.get(modId);
        ArrayList<Item> itemArrayList = new ArrayList<>();
        ForgeRegistries.ITEMS.getKeys().forEach(location -> {
            if (location.getNamespace().equals(modId)) itemArrayList.add(ForgeRegistries.ITEMS.getValue(location));
        });
        ItemChecker checker = new ItemChecker(itemArrayList.toArray(new Item[]{}));
        MOD_ITEM_MAP.put(modId, checker);
        return checker;
    }
}
