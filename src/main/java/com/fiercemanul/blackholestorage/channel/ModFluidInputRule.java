package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ModFluidInputRule extends Rule{

    private final FluidChecker fluids;
    private int lastFluidIndex = 0;

    public ModFluidInputRule(String value, int rate) {
        super(RuleType.MOD_FLUID, value, rate);
        this.fluids = FluidCheckers.getCheckersFromMod(value);
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetFace).ifPresent(fluidHandler -> {
            int tanks = fluidHandler.getTanks();
            if (tanks <= 1) {
                FluidStack fluidStack = fluidHandler.drain(rate, IFluidHandler.FluidAction.SIMULATE);
                if (fluidStack.isEmpty()) return;
                if (fluids.contains(fluidStack.getFluid())) {
                    int i = channel.addFluid(fluidStack);
                    if (i > 0) fluidHandler.drain(i, IFluidHandler.FluidAction.EXECUTE);
                }
            } else if(tanks > fluids.length) {
                if (inputFluid(fluids.get(lastFluidIndex), fluidHandler, channel)) return;
                for (int i = 0; i < fluids.length; i++) {
                    if (inputFluid(fluids.get(i), fluidHandler, channel)) {
                        lastFluidIndex = i;
                        return;
                    }
                }
            } else {
                for (int i = 0; i < tanks; i++) {
                    Fluid fluid = fluidHandler.getFluidInTank(i).getFluid();
                    if (!fluids.contains(fluid)) continue;
                    if (inputFluid(fluid, fluidHandler, channel)) return;
                }
            }
        });
    }

    private boolean inputFluid(Fluid fluid, IFluidHandler fluidHandler, ServerChannel channel) {
        FluidStack fluidStack = fluidHandler.drain(new FluidStack(fluid, rate), IFluidHandler.FluidAction.SIMULATE);
        if (fluidStack.isEmpty()) return false;
        int count = channel.addFluid(fluidStack);
        if (count > 0) {
            fluidHandler.drain(new FluidStack(fluid, count), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }
}
