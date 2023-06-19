package com.fiercemanul.blackholestorage.block;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.CONTROL_PANEL_BLOCK_ENTITY;

public class ControlPanelBlockEntity extends BlockEntity {

    private boolean craftingMode = false;
    private UUID owner;
    private String ownerNameCache;
    private boolean locked = false;

    public ControlPanelBlockEntity(BlockPos pos, BlockState state) {
        super(CONTROL_PANEL_BLOCK_ENTITY.get(), pos, state);
    }



    @Override
    public void load(CompoundTag pTag) {
        //TODO: 这里要防null
        craftingMode = pTag.getBoolean("craftingMode");
        if (pTag.contains("owner")) {
            owner = pTag.getUUID("owner");
            ownerNameCache = pTag.getString("ownerNameCache");
            locked = pTag.getBoolean("locked");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putBoolean("craftingMode", craftingMode);
        if (owner != null) {
            pTag.putUUID("owner", owner);
            pTag.putString("ownerNameCache", ownerNameCache);
            pTag.putBoolean("locked", locked);
        }
    }

    public void updateOwnerName() {
        if (owner != null) {
            if (level.getServer().getProfileCache().get(owner).isPresent()) {
                ownerNameCache = level.getServer().getProfileCache().get(owner).get().getName();
                setChanged();
            } else if (ownerNameCache == null) {
                ownerNameCache = "UnknownUser";
                setChanged();
            }
        }
    }


    public UUID getOwner() {
        return owner;
    }

    public String getOwnerNameCache() {
        return ownerNameCache;
    }

    public boolean isLocked() {
        return locked;
    }

    public Boolean getCraftingMode() {
        return craftingMode;
    }


    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public void setOwnerNameCache(String ownerNameCache) {
        this.ownerNameCache = ownerNameCache;
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
}
