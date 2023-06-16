package com.fiercemanul.blackholestorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.CONTROL_PANEL_BLOCK_ENTITY;

public class ControlPanelBlockEntity extends BlockEntity {

    private boolean craftingMode = false;
    private UUID owner;

    public ControlPanelBlockEntity(BlockPos pos, BlockState state) {
        super(CONTROL_PANEL_BLOCK_ENTITY.get(), pos, state);
    }



    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        //TODO: 这里要防null
        craftingMode = pTag.getBoolean("craftingMode");
        owner = pTag.getUUID("owner");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putBoolean("craftingMode", craftingMode);
        if (owner != null) {
            pTag.putUUID("owner", owner);
        }
    }

    public Boolean getCraftingMode() {
        return craftingMode;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setCraftingMode(Boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.setChanged();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }
}
