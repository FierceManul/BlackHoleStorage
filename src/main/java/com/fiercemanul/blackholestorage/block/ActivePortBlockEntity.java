package com.fiercemanul.blackholestorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.ACTIVE_PORT_BLOCK_ENTITY;

public class ActivePortBlockEntity extends BlockEntity {

    public ActivePortBlockEntity(BlockPos pos, BlockState state) {
        super(ACTIVE_PORT_BLOCK_ENTITY.get(), pos, state);
    }
}
