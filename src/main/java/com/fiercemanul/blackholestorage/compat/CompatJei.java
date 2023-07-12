package com.fiercemanul.blackholestorage.compat;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import com.fiercemanul.blackholestorage.gui.ControlPanelScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class CompatJei implements IModPlugin {

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(BlackHoleStorage.MODID, "default");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlackHoleStorage.CONTROL_PANEL_ITEM.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(BlackHoleStorage.PORTABLE_CONTROL_PANEL_ITEM.get()), RecipeTypes.CRAFTING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ControlPanelMenu.class, BlackHoleStorage.CONTROL_PANEL_MENU.get(), RecipeTypes.CRAFTING, 41, 9, 0, 36);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(ControlPanelScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(ControlPanelScreen screen) {
                List<Rect2i> rect2is = new ArrayList<>();
                rect2is.add(new Rect2i(
                        screen.getGuiLeft(),
                        screen.getGuiTop(),
                        screen.imageWidth,
                        screen.imageHeight
                        ));
                return rect2is;
            }
        });
    }
}
