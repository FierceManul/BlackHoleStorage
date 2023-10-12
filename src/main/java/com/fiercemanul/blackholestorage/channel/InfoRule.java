package com.fiercemanul.blackholestorage.channel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class InfoRule {

    public final RuleType ruleType;
    public final String value;
    public final int rate;

    public InfoRule(RuleType ruleType, String value, int rate) {
        this.ruleType = ruleType;
        this.value = value;
        this.rate = rate;
    }

    public InfoRule(CompoundTag tag) {
        this.ruleType = RuleType.get(tag.getInt("type"));
        this.value = tag.getString("value");
        this.rate = tag.getInt("rate");;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", ruleType.ordinal());
        tag.putString("value", value);
        tag.putInt("rate", rate);
        return tag;
    }

    public MutableComponent getDisplay() {
        return switch (ruleType) {
            case ITEM -> Component.translatable("bhs.GUI.rule.item", value);
            case ITEM_TAG -> Component.translatable("bhs.GUI.rule.item_tag", value);
            case FLUID -> Component.translatable("bhs.GUI.rule.fluid", value);
            case FORGE_ENERGY -> Component.translatable("bhs.GUI.rule.fe");
            case MOD_ITEM -> Component.translatable("bhs.GUI.rule.mod_item", value);
            case MOD_FLUID -> Component.translatable("bhs.GUI.rule.mod_fluid", value);
            case ANY -> Component.translatable("bhs.GUI.rule.any");
        };
    }
}
