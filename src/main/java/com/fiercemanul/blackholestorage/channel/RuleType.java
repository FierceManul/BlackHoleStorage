package com.fiercemanul.blackholestorage.channel;

public enum RuleType {
    ITEM,
    ITEM_TAG,
    FLUID,
    FORGE_ENERGY,
    MOD_ITEM,
    MOD_FLUID,
    ANY;

    public static RuleType get(int i) {
        return switch (i) {
            case 1 -> ITEM_TAG;
            case 2 -> FLUID;
            case 3 -> FORGE_ENERGY;
            case 4 -> MOD_ITEM;
            case 5 -> MOD_FLUID;
            case 6 -> ANY;
            default -> ITEM;
        };
    }
}
