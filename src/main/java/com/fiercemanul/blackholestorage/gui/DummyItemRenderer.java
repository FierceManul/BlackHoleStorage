package com.fiercemanul.blackholestorage.gui;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.text.DecimalFormat;

@OnlyIn(Dist.CLIENT)
public class DummyItemRenderer{
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");
}
