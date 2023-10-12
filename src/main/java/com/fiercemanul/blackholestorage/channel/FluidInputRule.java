package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidInputRule extends Rule{

    private final Fluid fluid;

    public FluidInputRule(Fluid fluid, String value, int rate) {
        super(RuleType.FLUID, value, rate);
        this.fluid = fluid;
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, targetFace).ifPresent(fluidHandler -> {
            FluidStack testStack = fluidHandler.drain(new FluidStack(fluid, rate), IFluidHandler.FluidAction.SIMULATE);
            if (testStack.isEmpty()) return;
            int i = channel.addFluid(testStack);
            if (i > 0) fluidHandler.drain(new FluidStack(fluid, i), IFluidHandler.FluidAction.EXECUTE);
        });
    }
}
