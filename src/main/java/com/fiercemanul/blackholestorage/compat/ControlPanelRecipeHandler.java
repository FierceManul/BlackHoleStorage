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
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

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
            if (recipe instanceof Recipe<?> recipe1) {
                String recipeId = recipe1.getId().toString();
                if (!container.craftingMode) container.setCraftMode.run();
                NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RecipePack(container.containerId, recipeId, maxTransfer));
            } else {
                List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();
                HashMap<Item, Long> itemAmount = new HashMap<>();
                HashMap<Item, Integer> itemP = new HashMap<>();
                InvItemCounter invItemCounter = new InvItemCounter(player.getInventory());
                HashSet<Item> itemChosen = new HashSet<>();
                for (IRecipeSlotView slotView : slotViews) {
                    if (slotView.isEmpty() || slotView.getRole().equals(RecipeIngredientRole.OUTPUT)) continue;
                    long markCount = 0;
                    Item markItem = null;
                    ItemStack[] stacks = slotView.getItemStacks().toArray(ItemStack[]::new);
                    for (ItemStack stack : stacks) {
                        if (stack.hasTag()) continue;
                        Item item = stack.getItem();
                        long count;
                        if (itemAmount.containsKey(item)) count = itemAmount.get(item);
                        else {
                            count = container.channel.getRealItemAmount(Tools.getItemId(item));
                            count += invItemCounter.getCount(item);
                            itemAmount.put(item, count);
                        }
                        if (count > markCount) {
                            markCount = count;
                            markItem = item;
                        }
                        if (itemP.containsKey(item)) itemP.replace(item, itemP.get(item) + 1);
                        else itemP.put(item, 1);
                    }
                    if (markItem != null) itemChosen.add(markItem);
                }
                HashMap<String, Integer> itemNeed = new HashMap<>();
                for (Item item : itemChosen) {
                    int need = itemP.get(item);
                    if (maxTransfer) need *= new ItemStack(item).getMaxStackSize();
                    need -= invItemCounter.getCount(item);
                    if (need > 0) {
                        itemNeed.put(Tools.getItemId(item), need);
                    }
                }
                if (!itemNeed.isEmpty()) NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RecipeItemPack(container.containerId, itemNeed));
            }
        } else {
            List<IRecipeSlotView> list = recipeSlots.getSlotViews();
            List<IRecipeSlotView> missingSlots = new ArrayList<>();
            ArrayList<ItemStack> stacksTempP = new ArrayList<>();

            //玩家背包物品数量缓存，延迟初始化。
            InvItemCounter invItemCounter = null;

            Channel channel = ClientChannelManager.getInstance().getChannel();
            Inventory inventory = player.getInventory();
            for (IRecipeSlotView slot : list) {
                if (!slot.getRole().equals(RecipeIngredientRole.INPUT) || slot.isEmpty() || slot.getDisplayedItemStack().isEmpty()) continue;
                ItemStack viewingStack = slot.getDisplayedItemStack().get();

                //p是物品份数量
                int p = 0;
                boolean flag = true;
                for (ItemStack itemStack : stacksTempP) {
                    if (ItemStack.isSameItemSameTags(viewingStack, itemStack)) {
                        p = itemStack.getCount() + 1;
                        itemStack.setCount(p);
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    ItemStack itemStack = viewingStack.copy();
                    itemStack.setCount(1);
                    stacksTempP.add(itemStack);
                    p = 1;
                }

                long count = channel.getRealItemAmount(Tools.getItemId(viewingStack.getItem()));

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