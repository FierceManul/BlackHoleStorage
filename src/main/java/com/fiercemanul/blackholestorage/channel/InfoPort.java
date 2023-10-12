package com.fiercemanul.blackholestorage.channel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;

public final class InfoPort {

    public ArrayList<InfoRule> inputRules = new ArrayList<>();
    public ArrayList<InfoRule> outputRules = new ArrayList<>();
    public boolean enable = false;

    public InfoPort() {}

    public InfoPort(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return;
        ListTag input = tag.getList("input", Tag.TAG_COMPOUND);
        if (!input.isEmpty()) {
            for (Tag value : input) inputRules.add(new InfoRule((CompoundTag) value));
        }
        ListTag output = tag.getList("output", Tag.TAG_COMPOUND);
        if (!output.isEmpty()) {
            for (Tag value : output) outputRules.add(new InfoRule((CompoundTag) value));
        }
        enable = tag.getBoolean("enable");
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();

        ListTag input = new ListTag();
        inputRules.forEach(rule -> input.add(rule.toNbt()));
        nbt.put("input", input);

        ListTag output = new ListTag();
        outputRules.forEach(rule -> output.add(rule.toNbt()));
        nbt.put("output", output);

        nbt.putBoolean("enable", enable);
        return nbt;
    }
}
