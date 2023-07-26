package com.fiercemanul.blackholestorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.CONTROL_PANEL_BLOCK_ENTITY;

public class ControlPanelBlockEntity extends BlockEntity {

    private UUID owner;
    private boolean locked = false;
    private boolean craftingMode = false;
    private String filter = "";
    private byte sortType = 4;
    private byte viewType = 0;


    public ControlPanelBlockEntity(BlockPos pos, BlockState state) {
        super(CONTROL_PANEL_BLOCK_ENTITY.get(), pos, state);
    }



    @Override
    public void load(CompoundTag pTag) {
        //这里要防null
        if (pTag.contains("owner")) {
            owner = pTag.getUUID("owner");
            locked = pTag.getBoolean("locked");
        }
        if (pTag.contains("craftingMode")) craftingMode = pTag.getBoolean("craftingMode");
        if (pTag.contains("filter")) filter = pTag.getString("filter");
        if (pTag.contains("sortType")) sortType = pTag.getByte("sortType");
        if (pTag.contains("viewType")) viewType = pTag.getByte("viewType");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (owner != null) {
            pTag.putUUID("owner", owner);
            pTag.putBoolean("locked", locked);
        }
        pTag.putBoolean("craftingMode", craftingMode);
        pTag.putString("filter", filter);
        pTag.putByte("sortType", sortType);
        pTag.putByte("viewType", viewType);
    }


    public UUID getOwner() {
        return owner;
    }

    public boolean isLocked() {
        return locked;
    }

    public Boolean getCraftingMode() {
        return craftingMode;
    }

    public String getFilter() {
        return filter;
    }

    public byte getSortType() {
        return sortType;
    }

    public byte getViewType() {
        return viewType;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.setChanged();
    }

    public void setCraftingMode(Boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.setChanged();
    }

    public void setFilter(String filter) {
        this.filter = filter;
        this.setChanged();
    }

    public void setSortType(byte sortType) {
        this.sortType = sortType;
        this.setChanged();
    }

    public void setViewType(byte viewType) {
        this.viewType = viewType;
        this.setChanged();
    }
}
