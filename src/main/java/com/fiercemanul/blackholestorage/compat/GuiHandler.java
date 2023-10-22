package com.fiercemanul.blackholestorage.compat;

import com.fiercemanul.blackholestorage.gui.BaseScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;


@ParametersAreNonnullByDefault
public class GuiHandler<T extends BaseScreen<?>> implements IGuiContainerHandler<T> {

    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(T screen) {
        List<Rect2i> rect2is = new ArrayList<>();
        rect2is.add(new Rect2i(
                screen.getGuiLeft(),
                screen.getGuiTop(),
                screen.getGuiWidth(),
                screen.getGuiHeight()
        ));
        return rect2is;
    }
}
