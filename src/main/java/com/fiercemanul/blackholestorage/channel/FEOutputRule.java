package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class FEOutputRule extends Rule{

    public FEOutputRule(int rate) {
        super(RuleType.FORGE_ENERGY, "", rate);
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(ForgeCapabilities.ENERGY, targetFace).ifPresent(energyStorage -> {
            if (channel.getRealFEAmount() <= 0L) return;
            int maxReceive = Integer.min(rate, channel.getFEAmount());
            int received = energyStorage.receiveEnergy(maxReceive, true);
            channel.removeEnergy((long) received);
            energyStorage.receiveEnergy(received, false);
        });
    }
}
