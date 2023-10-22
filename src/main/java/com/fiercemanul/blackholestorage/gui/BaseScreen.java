package com.fiercemanul.blackholestorage.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.ParametersAreNonnullByDefault;

public abstract class BaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {


    public BaseScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 202;
        this.imageHeight = 249;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void renderLabels(PoseStack stack, int i, int j) {}

    public int getGuiWidth() {
        return imageWidth;
    }

    public int getGuiHeight() {
        return imageHeight;
    }
}
