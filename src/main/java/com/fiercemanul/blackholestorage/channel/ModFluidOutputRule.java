package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.util.Tools;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class ModFluidOutputRule extends Rule {

    private final FluidChecker fluids;
    private int lastFluidIndex = 0;

    public ModFluidOutputRule(String value, int rate) {
        super(RuleType.MOD_FLUID, value, rate);
        this.fluids = FluidCheckers.getCheckersFromMod(value);
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, targetFace).ifPresent(fluidHandler -> {
            for (int i = 0; i < fluids.length; i++) {
                Fluid fluid = fluids.get(lastFluidIndex);
                int filled = fluidHandler.fill(new FluidStack(fluid, Integer.min(rate, channel.getStorageAmount(fluid))), IFluidHandler.FluidAction.SIMULATE);
                if (filled > 0) {
                    fluidHandler.fill(channel.takeFluid(Tools.getFluidId(fluid), rate), IFluidHandler.FluidAction.EXECUTE);
                    return;
                }
                else nextFluid();
            }
        });
    }

    private void nextFluid() {
        lastFluidIndex++;
        if (lastFluidIndex >= fluids.length) lastFluidIndex = 0;
    }

}
