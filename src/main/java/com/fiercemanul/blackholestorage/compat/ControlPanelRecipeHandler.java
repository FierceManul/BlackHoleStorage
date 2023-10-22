package com.fiercemanul.blackholestorage.compat;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.Channel;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import com.fiercemanul.blackholestorage.network.RecipePack;
import com.fiercemanul.blackholestorage.util.Tools;
import com.ibm.icu.impl.CollectionSet;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class ControlPanelRecipeHandler implements IRecipeTransferHandler<ControlPanelMenu, CraftingRecipe> {

    private final IRecipeTransferHandlerHelper helper;

    public ControlPanelRecipeHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public @NotNull Class<ControlPanelMenu> getContainerClass() {
        return ControlPanelMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<ControlPanelMenu>> getMenuType() {
        return Optional.of(BlackHoleStorage.CONTROL_PANEL_MENU.get());
    }

    @Override
    public @NotNull RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @Nullable IRecipeTransferError transferRecipe(ControlPanelMenu container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            String recipeId = recipe.getId().toString();
            List<IRecipeSlotView> list = recipeSlots.getSlotViews();
            Map<String, Integer> itemsMap = new HashMap<>();
            for (int i = 1; i < list.size(); i++) {
                list.get(i).getDisplayedItemStack().ifPresent(itemStack -> {
                    String id = Tools.getItemId(itemStack.getItem());
                    if (itemsMap.containsKey(id)) itemsMap.replace(id, itemsMap.get(id) + 1);
                    else itemsMap.put(id, 1);
                });
            }
            if (!container.craftingMode) container.setCraftMode.run();
            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RecipePack(container.containerId, recipeId, itemsMap, maxTransfer));
        } else {
            List<IRecipeSlotView> list = recipeSlots.getSlotViews();
            List<IRecipeSlotView> missingSlots = new ArrayList<>();
            ArrayList<ItemStack> stacksTempP = new ArrayList<>();
            //玩家背包物品数量缓存。比真数量多一避免变成空气。
            ArrayList<ItemStack> stacksTempC = new ArrayList<>();
            Channel channel = ClientChannelManager.getInstance().getChannel();
            Inventory inventory = player.getInventory();
            for (int i = 1; i < 10; i++) {
                if (list.get(i).isEmpty() || list.get(i).getDisplayedItemStack().isEmpty()) continue;
                ItemStack craftingStack = list.get(i).getDisplayedItemStack().get();
                //p是物品份数量
                int p = 0;
                boolean flag = true;
                for (ItemStack itemStack : stacksTempP) {
                    if (ItemStack.isSameItemSameTags(craftingStack, itemStack)) {
                        p = itemStack.getCount() + 1;
                        itemStack.setCount(p);
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    ItemStack itemStack = craftingStack.copy();
                    itemStack.setCount(1);
                    stacksTempP.add(itemStack);
                    p = 1;
                }
                //如果频道内物品不足，检查背包。
                if (p > channel.getRealItemAmount(Tools.getItemId(craftingStack.getItem()))) {
                    int count = 0;
                    boolean flag1 = true;
                    for (ItemStack itemStack : stacksTempC) {
                        if (ItemStack.isSameItemSameTags(craftingStack, itemStack)) {
                            count = itemStack.getCount() - 1;
                            flag1 = false;
                            break;
                        }
                    }

                    if (flag1) {
                        int countTemp = 1;
                        for (ItemStack itemStack : inventory.items) {
                            if (ItemStack.isSameItemSameTags(craftingStack, itemStack)) {
                                countTemp += itemStack.getCount();
                            }
                        }
                        ItemStack itemStack = craftingStack.copy();
                        itemStack.setCount(countTemp);
                        stacksTempC.add(itemStack);
                        count = countTemp - 1;
                    }

                    if (p > count) missingSlots.add(list.get(i));
                }
            }
            if (missingSlots.size() > 0) return helper.createUserErrorForMissingSlots(
                    Component.translatable("bhs.GUI.craft.missing"),
                    missingSlots
            );
        }
        return null;
    }

}