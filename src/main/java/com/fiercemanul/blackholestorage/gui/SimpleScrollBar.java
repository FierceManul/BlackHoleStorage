package com.fiercemanul.blackholestorage.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SimpleScrollBar extends AbstractWidget {

    private boolean scrolling = false;
    private double scrollTagSize = 4;
    private double scrolledOn = 0.0D;
    private int scrollBarTagColor = FastColor.ARGB32.color(255, 77, 73, 77);
    private int scrollBarBackgroundColor = FastColor.ARGB32.color(255, 36, 30, 31);


    public SimpleScrollBar(int x, int y, int weight, int height, Component message) {
        super(x, y, weight, height, message);
        if (height < weight * 2) setSize(weight, weight * 2);
    }

    public SimpleScrollBar(int x, int y, int weight, int height) {
        this(x, y, weight, height, CommonComponents.EMPTY);
    }

    public void setScrollBarTagColor(int scrollBarTagColor) {
        this.scrollBarTagColor = scrollBarTagColor;
    }

    public void setScrollBarBackgroundColor(int scrollBarBackgroundColor) {
        this.scrollBarBackgroundColor = scrollBarBackgroundColor;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int weight, int height) {
        this.width = weight;
        this.height = height;
    }

    public void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
    }

    public void setScrolledOn(double scrolledOn) {
        this.scrolledOn = Math.max(0.0D, Math.min(1.0D, scrolledOn));
    }

    public void setScrollTagSize(double scrollTagSize) {
        this.scrollTagSize = Math.max(width, Math.min(height, scrollTagSize));
    }

    /*public void setScrollTagSizePercentage(double percentage) {
        this.scrollTagSize = Math.max(width, Math.min(height, scrollTagSize * getHeight()));
    }*/

    public double getScrollTagSize() {
        return scrollTagSize;
    }

    public double getScrollOn() {
        return this.scrolledOn;
    }

    public boolean isScrolling() {
        return scrolling;
    }

    public boolean canScroll() {
        return scrollTagSize < height;
    }

    public double getScrolledOn() {
        return scrolledOn;
    }


    @Override
    public void onClick(double pMouseX, double pMouseY) {
        this.scrolling = true;
        this.onDragTo(pMouseY);
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        this.scrolling = false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double pDragX, double pDragY) {
        this.onDragTo(mouseY);
    }

    public void onDragTo(double mouseY) {
        if (mouseY <= y) {
            scrolledOn = 0.0D;
        } else if (mouseY >= y + getHeight()) {
            scrolledOn = 1.0D;
        } else {
            double v = (mouseY - y - (scrollTagSize / 2)) / (getHeight() - scrollTagSize);
            setScrolledOn(v);
        }
        this.draggedTo(scrolledOn);
    }

    abstract public void draggedTo(double scrolledOn);

    abstract public void beforeRender();

    @Override
    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.beforeRender();
        if (!this.visible) return;
        fill(poseStack, x, y, x + width, y + height, scrollBarBackgroundColor);
        double v = y + ((getHeight() - scrollTagSize) * scrolledOn);
        fill(poseStack, x, (int) Math.floor(v), x + width, (int) Math.ceil(v + scrollTagSize), scrollBarTagColor);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }
}
