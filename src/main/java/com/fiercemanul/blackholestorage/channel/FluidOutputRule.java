package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidOutputRule extends Rule{

    private final Fluid fluid;

    public FluidOutputRule(Fluid fluid, String value, int rate) {
        super(RuleType.FLUID, value, rate);
        this.fluid = fluid;
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetFace).ifPresent(fluidHandler -> {
            int filledAmount = fluidHandler.fill(new FluidStack(fluid, Integer.min(rate, channel.getFluidAmount(value))), IFluidHandler.FluidAction.SIMULATE);
            if (filledAmount <= 0) return;
            fluidHandler.fill(new FluidStack(fluid, filledAmount), IFluidHandler.FluidAction.EXECUTE);
            channel.takeFluid(value, filledAmount);
        });
    }
}
