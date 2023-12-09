package com.fiercemanul.blackholestorage.channel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
        this.rate = tag.getInt("rate");
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
            case ITEM -> new TranslatableComponent("bhs.GUI.rule.item", value);
            case ITEM_TAG -> new TranslatableComponent("bhs.GUI.rule.item_tag", value);
            case FLUID -> new TranslatableComponent("bhs.GUI.rule.fluid", value);
            case FORGE_ENERGY -> new TranslatableComponent("bhs.GUI.rule.fe");
            case MOD_ITEM -> new TranslatableComponent("bhs.GUI.rule.mod_item", value);
            case MOD_FLUID -> new TranslatableComponent("bhs.GUI.rule.mod_fluid", value);
            case ANY_ITEM -> new TranslatableComponent("bhs.GUI.rule.any_item");
            case ANY_FLUID -> new TranslatableComponent("bhs.GUI.rule.any_fluid");
        };
    }
}
