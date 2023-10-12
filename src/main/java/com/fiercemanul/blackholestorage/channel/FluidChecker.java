package com.fiercemanul.blackholestorage.channel;

import net.minecraft.world.level.material.Fluid;

public final class FluidChecker {

    private final Fluid[] fluids;
    public final int length;
    private int lastIndex = 0;

    public FluidChecker(Fluid[] fluids) {
        this.fluids = fluids;
        this.length = fluids.length;
    }


    public Fluid get(int index) {
        if (index >= length || index < 0) return fluids[0];
        return fluids[index];
    }

    public boolean contains(Fluid otherFluid) {
        if (fluids[lastIndex] == otherFluid) return true;
        for (int i = 0; i < fluids.length; i++) {
            if (fluids[i] == otherFluid) {
                lastIndex = i;
                return true;
            }
        }
        return false;
    }
}
