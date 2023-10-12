package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;

public class Port {

    private final ArrayList<Rule> inputRules = new ArrayList<>();
    private final ArrayList<Rule> outputRules = new ArrayList<>();
    public boolean enable = false;
    private final Direction targetFace;


    public Port(Direction targetFace) {
        this.targetFace = targetFace;
    }

    public void onTick(ServerChannel channel, BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.isRemoved() || !enable) return;
        inputRules.forEach(rule -> rule.work(channel, blockEntity, targetFace));
        outputRules.forEach(rule -> rule.work(channel, blockEntity, targetFace));
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

    public void fromNbt(CompoundTag tag) {
        inputRules.clear();
        outputRules.clear();
        enable = false;
        if (tag == null || tag.isEmpty()) return;
        ListTag input = tag.getList("input", Tag.TAG_COMPOUND);
        if (!input.isEmpty()) {
            int max = Integer.min(16, input.size());
            for (int i = 0; i < max; i++) {
                Rule rule = Rule.getValid((CompoundTag) input.get(i), true);
                if (rule != null) inputRules.add(rule);
            }
        }
        ListTag output = tag.getList("output", Tag.TAG_COMPOUND);
        if (!output.isEmpty()) {
            int max = Integer.min(16, output.size());
            for (int i = 0; i < max; i++) {
                Rule rule = Rule.getValid((CompoundTag) output.get(i), false);
                if (rule != null) outputRules.add(rule);
            }
        }
        enable = tag.getBoolean("enable");
    }
}
