package com.fiercemanul.blackholestorage.channel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;

public class FEInputRule extends Rule{

    public FEInputRule(int rate) {
        super(RuleType.FORGE_ENERGY, "", rate);
    }

    @Override
    public void work(ServerChannel channel, BlockEntity blockEntity, Direction targetFace) {
        blockEntity.getCapability(CapabilityEnergy.ENERGY, targetFace).ifPresent(energyStorage -> {
            if (!channel.canStorageFE()) return;
            int extracted = energyStorage.extractEnergy(rate, true);
            energyStorage.extractEnergy(channel.addEnergy(extracted), false);
        });
    }
}
