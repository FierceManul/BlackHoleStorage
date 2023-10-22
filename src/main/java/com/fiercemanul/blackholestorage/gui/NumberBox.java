package com.fiercemanul.blackholestorage.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

class NumberBox extends EditBox {

    private final int maxValue;

    public NumberBox(Font font, int pX, int pY, int pWidth, int pHeight, int maxValue, int maxLength) {
        super(font, pX, pY, pWidth, pHeight, Component.literal("114514"));
        this.maxValue = maxValue;
        this.setFilter(s -> s.matches("^[1-9][0-9]*$") || s.equals(""));
        this.setMaxLength(maxLength);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setValue(String pText) {
        super.setValue(pText);
        checkValue();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void insertText(String pTextToWrite) {
        super.insertText(pTextToWrite);
        checkValue();
    }

    private void checkValue() {
        if (getValue().isEmpty()) return;
        long l = Long.parseLong(getValue());
        if (l > maxValue) setValue(String.valueOf(maxValue));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (visible && isHovered && pButton == 1) {
            setValue("");
            setFocus(true);
            setEditable(true);
            return true;
        } else return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
