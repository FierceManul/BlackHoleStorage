package com.fiercemanul.blackholestorage.compat;

import com.fiercemanul.blackholestorage.gui.ActivePortScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ActivePortGhostItemHandler implements IGhostIngredientHandler<ActivePortScreen> {

    @Override
    public <I> @NotNull List<Target<I>> getTargets(ActivePortScreen gui, I ingredient, boolean doStart) {
        return new LinkedList<>();
    }

    @Override
    public <I> @NotNull List<Target<I>> getTargetsTyped(ActivePortScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new LinkedList<>();
        if (!gui.getMenu().checkedSlotActive()) return targets;
        ingredient.getItemStack().ifPresent(itemStack -> targets.add(new Target<>() {

            @Override
            public @NotNull Rect2i getArea() {
                return new Rect2i(gui.getGuiLeft() + 31, gui.getGuiTop() + 146, 16, 16);
            }

            @Override
            public void accept(I ingredient) {
                gui.jeiGhostItemRule(itemStack);
            }
        }));
        return targets;
    }

    @Override
    public void onComplete() {}
}
