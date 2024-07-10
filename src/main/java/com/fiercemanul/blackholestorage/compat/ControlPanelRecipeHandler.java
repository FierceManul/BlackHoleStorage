package com.fiercemanul.blackholestorage.compat;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.Channel;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import com.fiercemanul.blackholestorage.network.RecipeItemPack;
import com.fiercemanul.blackholestorage.network.RecipePack;
import com.fiercemanul.blackholestorage.util.InvItemCounter;
import com.fiercemanul.blackholestorage.util.Tools;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ControlPanelRecipeHandler<R> implements IRecipeTransferHandler<ControlPanelMenu, R> {


    private final IRecipeTransferHandlerHelper helper;

    public ControlPanelRecipeHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public @NotNull Class<ControlPanelMenu> getContainerClass() {
        return ControlPanelMenu.class;
    }

    @Override
    public @NotNull Class<R> getRecipeClass() {
        return (Class<R>) CraftingRecipe.class;
    }

    @Override
    public @NotNull Optional<MenuType<ControlPanelMenu>> getMenuType() {
        return Optional.of(BlackHoleStorage.CONTROL_PANEL_MENU.get());
    }

    @SuppressWarnings("all")
    @Override
    public RecipeType<R> getRecipeType() {
        return null;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @Nullable IRecipeTransferError transferRecipe(ControlPanelMenu container, R recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            if (recipe instanceof CraftingRecipe craftingRecipe) {
                String recipeId = craftingRecipe.getId().toString();
                if (!container.craftingMode) container.craftModeSetter.run();
                NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RecipePack(container.containerId, recipeId, maxTransfer));
            } else {
                List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();
                HashMap<Item, Integer> itemP = new HashMap<>();
                InvItemCounter invItemCounter = new InvItemCounter(player.getInventory());
                for (IRecipeSlotView slotView : slotViews) {
                    if (slotView.isEmpty() || slotView.getRole().equals(RecipeIngredientRole.OUTPUT)) continue;
                    long markCount = 0;
                    ItemStack markStack = null;
                    ItemStack[] stacks = slotView.getItemStacks().toArray(ItemStack[]::new);
                    for (ItemStack stack : stacks) {
                        if (stack.hasTag()) continue;
                        Item item = stack.getItem();
                        long count = container.channel.getRealItemAmount(Tools.getItemId(item));
                        count += invItemCounter.getCount(item);
                        if (count > markCount) {
                            markCount = count;
                            markStack = stack;
                        }
                    }
                    if (markStack != null) {
                        Item item = markStack.getItem();
                        if (itemP.containsKey(item)) itemP.replace(item, itemP.get(item) + markStack.getCount());
                        else itemP.put(item, markStack.getCount());
                    }
                }
                HashMap<String, Integer> itemNeed = new HashMap<>();
                itemP.forEach((item, integer) -> {
                    int need = itemP.get(item);
                    if (maxTransfer) need *= 64;
                    need -= invItemCounter.getCount(item);
                    if (need > 0) {
                        itemNeed.put(Tools.getItemId(item), need);
                    }
                });
                if (!itemNeed.isEmpty()) NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RecipeItemPack(container.containerId, itemNeed));
            }
        }
        else {
            List<IRecipeSlotView> list = recipeSlots.getSlotViews();
            List<IRecipeSlotView> missingSlots = new ArrayList<>();
            ArrayList<ItemStack> stacksTempP = new ArrayList<>();

            //玩家背包物品数量缓存，延迟初始化。
            InvItemCounter invItemCounter = null;

            Channel channel = ClientChannelManager.getInstance().getChannel();
            Inventory inventory = player.getInventory();
            for (IRecipeSlotView slot : list) {
                if (slot.getRole().equals(RecipeIngredientRole.OUTPUT) || slot.isEmpty() || slot.getDisplayedItemStack().isEmpty()) continue;
                ItemStack viewingStack = slot.getDisplayedItemStack().get();

                //p是物品份数量
                int p = 0;
                boolean flag = true;
                for (ItemStack itemStack : stacksTempP) {
                    if (ItemStack.isSameItemSameTags(viewingStack, itemStack)) {
                        p = itemStack.getCount() + viewingStack.getCount();
                        itemStack.setCount(p);
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    ItemStack itemStack = viewingStack.copy();
                    stacksTempP.add(itemStack);
                    p = itemStack.getCount();
                }

                long count = 0;
                if (!viewingStack.hasTag()) count = channel.getRealItemAmount(Tools.getItemId(viewingStack.getItem()));

                //如果频道内物品不足，检查背包。
                if (p > count) {
                    if (invItemCounter == null) invItemCounter = new InvItemCounter(inventory);
                    count += invItemCounter.getCount(viewingStack);
                    if (p > count) missingSlots.add(slot);
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