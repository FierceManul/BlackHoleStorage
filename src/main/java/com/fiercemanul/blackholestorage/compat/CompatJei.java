package com.fiercemanul.blackholestorage.compat;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ActivePortScreen;
import com.fiercemanul.blackholestorage.gui.BaseScreen;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public final class CompatJei implements IModPlugin {

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
        registration.addRecipeTransferHandler(new ControlPanelRecipeHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
    }



    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(BaseScreen.class, new GuiHandler<>());
        registration.addGhostIngredientHandler(ActivePortScreen.class, new ActivePortGhostItemHandler());
    }
}
