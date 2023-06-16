package com.fiercemanul.blackholestorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.PASSIVE_PORT_BLOCK_ENTITY;

public class PassivePortBlockEntity extends BlockEntity {

    public PassivePortBlockEntity(BlockPos pos, BlockState state) {
        super(PASSIVE_PORT_BLOCK_ENTITY.get(), pos, state);
    }

}
