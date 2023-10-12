package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class AnyInputRule extends Rule {

    private int lastSlot = 0;

    public AnyInputRule(int rate) {
        super(RuleType.ANY, "", rate);
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, targetFace).ifPresent(itemHandler -> {
            if (!channel.canStorageItem(value)) return;
            if (worked(itemHandler, lastSlot, channel)) return;
            lastSlot = 0;
            int maxSlots = itemHandler.getSlots();
            for (int i = 0; i < maxSlots; i++) {
                if (worked(itemHandler, i, channel)) {
                    lastSlot = i;
                    return;
                }
            }
        });
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
        blockEntity.getCapability(ForgeCapabilities.ENERGY, targetFace).ifPresent(energyStorage -> {
            int extracted = energyStorage.extractEnergy(rate, true);
            if (extracted == 0) return;
            int count = channel.addEnergy(extracted);
            if (count > 0) energyStorage.extractEnergy(count, false);
        });
    }

    private boolean worked(IItemHandler itemHandler, int slot, ServerChannel channel) {
        ItemStack testStack = itemHandler.extractItem(slot, rate, true);
        int count = channel.addItem(testStack);
        if (count > 0) {
            itemHandler.extractItem(slot, count, false);
            return true;
        }
        return false;
    }
}
