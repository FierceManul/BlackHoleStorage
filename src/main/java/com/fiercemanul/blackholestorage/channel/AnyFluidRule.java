package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class AnyFluidRule extends Rule {

    public AnyFluidRule(int rate) {
        super(RuleType.ANY_FLUID, "", rate);
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, targetFace).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.drain(rate, IFluidHandler.FluidAction.SIMULATE);
            if (fluidStack.isEmpty()) return;
            int count = channel.addFluid(fluidStack);
            if (count > 0) fluidHandler.drain(count, IFluidHandler.FluidAction.EXECUTE);
            else if (fluidHandler.getTanks() > 1) for (int i = 0; i < fluidHandler.getTanks(); i++) {
                FluidStack fluidStack1 = fluidHandler.getFluidInTank(i);
                if (fluidStack.isEmpty()) continue;
                FluidStack fluidStack2 = fluidHandler.drain(new FluidStack(fluidStack1.getFluid(), rate), IFluidHandler.FluidAction.SIMULATE);
                int count1 = channel.addFluid(fluidStack2);
                if (count1 > 0) {
                    fluidHandler.drain(new FluidStack(fluidStack1.getFluid(), count1), IFluidHandler.FluidAction.EXECUTE);
                    return;
                }
            }
        });
    }
}
