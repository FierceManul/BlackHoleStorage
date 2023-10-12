package com.fiercemanul.blackholestorage.channel;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;

public final class FluidCheckers {

    private static final HashMap<String, FluidChecker> MOD_FLUID_MAP = new HashMap<>();

    public static FluidChecker getCheckersFromMod(String modId) {
        if (MOD_FLUID_MAP.containsKey(modId)) return MOD_FLUID_MAP.get(modId);
        ArrayList<Fluid> fluidArrayList = new ArrayList<>();
        ForgeRegistries.FLUIDS.getKeys().forEach(location -> {
            if (location.getNamespace().equals(modId)) fluidArrayList.add(ForgeRegistries.FLUIDS.getValue(location));
        });
        FluidChecker checker = new FluidChecker(fluidArrayList.toArray(new Fluid[]{}));
        MOD_FLUID_MAP.put(modId, checker);
        return checker;
    }
}
