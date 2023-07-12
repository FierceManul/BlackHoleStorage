package com.fiercemanul.blackholestorage.gui;


public abstract class ScrollBar {

    public final int x;
    public final int y;
    public final int weight;
    public final int height;
    public boolean scrolling = false;
    protected double scrollTagSize;
    protected double scrolledOn = 0.0D;

    public ScrollBar(int x, int y, int weight, int height) {
        this.x = x;
        this.y = y;
        this.weight = weight;
        this.height = height;
        this.scrollTagSize = height;
    }

    public boolean canScroll() {
        return scrollTagSize < height;
    }

    public double getScrollTagSize() {
        return scrollTagSize;
    }

    public void setScrollTagSize(double scrollTagSize) {
        this.scrollTagSize = Math.max(weight, Math.min(height, scrollTagSize));
    }

    abstract public void onmMouseDraggedOn(double y);

    public double getScrolledOn() {
        return scrolledOn;
    }

    public void setScrolledOn(double scrolledOn) {
        this.scrolledOn = Math.max(0.0D, Math.min(1.0D, scrolledOn));
    }

}
