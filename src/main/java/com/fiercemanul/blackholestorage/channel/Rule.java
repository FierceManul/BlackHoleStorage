package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public abstract class Rule {

    public final RuleType ruleType;
    public final String value;
    public final int rate;

    public Rule(RuleType ruleType, String value, int rate) {
        this.ruleType = ruleType;
        this.value = value;
        this.rate = rate;
    }

    public abstract void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace);

    public static @Nullable Rule getValid(CompoundTag ruleTag, boolean isInput) {
        RuleType ruleType = RuleType.get(ruleTag.getInt("type"));
        String value = ruleTag.getString("value");
        int rate = ruleTag.getInt("rate");
        if (rate <= 0) rate = 1;
        switch (ruleType) {
            case ITEM -> {
                if (value.equals("") || value.equals("minecraft:air")) return null;
                ResourceLocation location = new ResourceLocation(value);
                Item item = ForgeRegistries.ITEMS.getValue(location);
                if (item == null || item.equals(Items.AIR)) return null;
                return isInput ? new ItemInputRule(item, location.toString(), rate) : new ItemOutputRule(item, location.toString(), rate);
            }
            case ITEM_TAG -> {
                if (value.indexOf(':') < 1) return null;
                ResourceLocation location = new ResourceLocation(value);
                ItemChecker checker = ItemCheckers.getCheckersFromTag(location);
                if (checker.length == 0) return null;
                return isInput ? new MultiItemInputRule(RuleType.ITEM_TAG, checker, location.toString(), rate) : new MultiItemOutputRule(RuleType.ITEM_TAG, checker, location.toString(), rate);
            }
            case FLUID -> {
                ResourceLocation location = new ResourceLocation(value);
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(location);
                if (fluid == null) return null;
                return isInput ? new FluidInputRule(fluid, location.toString(), rate) : new FluidOutputRule(fluid, location.toString(), rate);
            }
            case FORGE_ENERGY -> {
                return isInput ? new FEInputRule(rate) : new FEOutputRule(rate);
            }
            case MOD_ITEM -> {
                int i = value.indexOf(':');
                if (i >= 1) value = value.substring(0, i);
                else if (i == 0) value = value.substring(1);
                ItemChecker checker = ItemCheckers.getCheckersFromMod(value);
                if (checker.length == 0) return null;
                return isInput ? new MultiItemInputRule(RuleType.MOD_ITEM, checker, value, rate) : new MultiItemOutputRule(RuleType.MOD_ITEM, checker, value, rate);
            }
            case MOD_FLUID -> {
                int i = value.indexOf(':');
                if (i >= 1) value = value.substring(0, i);
                else if (i == 0) value = value.substring(1);
                return isInput ? new ModFluidInputRule(value, rate) : new ModFluidOutputRule(value, rate);
            }
            case ANY_ITEM -> {
                return isInput ? new AnyItemtRule(rate) : null;
            }
            case ANY_FLUID -> {
                return isInput ? new AnyFluidRule(rate) : null;
            }
        }
        return null;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", ruleType.ordinal());
        tag.putString("value", value);
        tag.putInt("rate", rate);
        return tag;
    }

}
