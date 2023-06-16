package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.block.ControlPanelBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.CONTROL_PANEL_MENU;
import static com.fiercemanul.blackholestorage.BlackHoleStorage.PORTABLE_CONTROL_PANEL_ITEM;

public class ControlPanelMenu extends AbstractContainerMenu {

    private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final SimpleContainer fakeContainer = new SimpleContainer(100);
    private ContainerLevelAccess access;
    private Player player;
    private Level level;
    public ControlPanelBlockEntity controlPanelBlock;
    public boolean craftingMode = true;
    public UUID owner;
    public String ownerName;
    public boolean locked = false;


    public static final String[] SORT_TYPE = new String[]{
            "namespace_id_ascending",
            "namespace_id_descending",
            "id_ascending",
            "id_descending",
            "mirror_id_ascending",
            "mirror_id_descending",
            "count_ascending",
            "count_descending"
    };

    //客户端调用这个
    public ControlPanelMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, playerInv.player, playerInv.player.level, extraData.readBlockPos());
    }

    //服务端用这个
    public ControlPanelMenu(int containerId, Inventory playerInv, Player player, Level level, BlockPos pos) {
        super(CONTROL_PANEL_MENU.get(), containerId);
        this.level = level;
        this.access = ContainerLevelAccess.create(level, pos);
        this.player = player;
        this.controlPanelBlock = (ControlPanelBlockEntity) level.getBlockEntity(pos);

        //快捷栏0~8
        for(int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInv, l, 23 + l * 17, 227));
        }

        //背包9~35
        for(int k = 0; k < 3; ++k) {
            for(int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInv, i1 + k * 9 + 9, 23 + i1 * 17, 176 + k * 17));
            }
        }

        //护甲36~40
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.HEAD, 39, 142, 125));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.CHEST, 38, 159, 125));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.LEGS, 37, 159, 142));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.FEET, 36, 142, 142));
        this.addSlot(new Slot(playerInv, 40, 159, 159){
            @Override
            public boolean isActive() {
                return craftingMode;
            }
        });

        //合成格41~50
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 3, 23 + j * 17, 125 + i * 17){
                    @Override
                    public boolean isActive() {
                        return craftingMode;
                    }
                });
            }
        }
        this.addSlot(new ResultSlot(player, this.craftSlots, this.resultSlots, 0, 74, 142){
            @Override
            public boolean isActive() {
                return craftingMode;
            }
        });

        //虚拟储存物品格51 ~ 149
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 11; j++) {
                this.addSlot(new Slot(fakeContainer, i * 11 + j, 6 + j * 17, 6 + i * 17));
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 11; j++) {
                this.addSlot(new Slot(fakeContainer, 77 + i * 11 + j, 6 + j * 17, 125 + i * 17){
                    @Override
                    public boolean isActive() {
                        return !craftingMode;
                    }
                });
            }
        }

        //数据同步专用150
        this.addSlot(new Slot(fakeContainer, 99, 6, 159));

        //服务端
        if (!level.isClientSide) {
            //TODO: 这里也要防null
            this.craftingMode = controlPanelBlock.getCraftingMode();
            if (controlPanelBlock.getOwner() == null) {
                this.owner = player.getUUID();
                controlPanelBlock.setOwner(owner);
            } else {
                this.owner = controlPanelBlock.getOwner();
            }
            //this.ownerName = Minecraft.;
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("craftingMode", this.craftingMode);
            tag.putUUID("owner", this.owner);
            ItemStack itemStack = new ItemStack(PORTABLE_CONTROL_PANEL_ITEM.get(), 1, tag);
            itemStack.setTag(tag);
            slots.get(150).set(itemStack);
        }

    }

    @Override
    public void initializeContents(int pStateId, List<ItemStack> pItems, ItemStack pCarried) {
        super.initializeContents(pStateId, pItems, pCarried);
        if (level.isClientSide) {
            //TODO: 空值预防
            CompoundTag tag = slots.get(150).getItem().getTag();
            this.craftingMode = tag.getBoolean("craftingMode");
            this.owner = tag.getUUID("owner");
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotId >= 0 && slotId <= 35) {

                if (craftingMode) {
                    if (!this.moveItemStackTo(itemstack1, 41, 50, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(itemstack1, 51, 62, false)) {
                        return ItemStack.EMPTY;
                    }
                }

            } else if (slotId == 50) {

                this.access.execute((level, pos) -> {
                    itemstack1.getItem().onCraftedBy(itemstack1, level, player);
                });
                if (!this.moveItemStackTo(itemstack1, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);

            } else if(slotId >= 41 && slotId <= 49) {

                if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }

            } else {

                if (craftingMode) {
                    if (!this.moveItemStackTo(itemstack1, 41, 50, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                }

            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
            if (slotId == 50) {
                player.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BlackHoleStorage.CONTROL_PANEL.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            clearCraftSlots();
        });
        controlPanelBlock.setCraftingMode(craftingMode);
    }




    //按钮相关

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0 : LogUtils.getLogger().warn("aaw");
            case 1 : craftingMode = !craftingMode;
        }
        return pId < 2;
    }







    //合成相关
    protected static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer) {
        if (!level.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<CraftingRecipe> optional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingContainer, level);
            if (optional.isPresent()) {
                CraftingRecipe craftingrecipe = optional.get();
                if (resultContainer.setRecipeUsed(level, serverplayer, craftingrecipe)) {
                    itemstack = craftingrecipe.assemble(craftingContainer);
                }
            }

            resultContainer.setItem(50, itemstack);
            menu.setRemoteSlot(50, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 50, itemstack));
        }
    }

    public void slotsChanged(Container container) {
        this.access.execute((level, pos) -> {
            slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots);
        });
    }

    public void fillCraftSlotsStackedContents(StackedContents contents) {
        this.craftSlots.fillStackedContents(contents);
    }

    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }

    public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
        return recipe.matches(this.craftSlots, this.player.level);
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    public int getResultSlotIndex() {
        return 50;
    }

    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    //未知用途
    public int getSize() {
        return 10;
    }

    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public boolean shouldMoveToInventory(int i) {
        return i != this.getResultSlotIndex();
    }


    //本类方法

    private Slot getArmorSlot(Player player, Inventory inventory, EquipmentSlot equipmentslot, int slotId, int x, int y) {
        return new Slot(inventory, slotId, x, y) {
            public void set(ItemStack itemStack) {
                ItemStack itemstack = this.getItem();
                super.set(itemStack);
                player.onEquipItem(equipmentslot, itemstack, itemStack);
            }

            public int getMaxStackSize() {
                return 1;
            }

            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.canEquip(equipmentslot, player);
            }

            public boolean mayPickup(Player player1) {
                ItemStack itemstack = this.getItem();
                return (itemstack.isEmpty() || player1.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(player1);
            }

            @Override
            public boolean isActive() {
                return craftingMode;
            }
        };
    }

    protected void clearCraftSlots() {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for(int j = 0; j < this.craftSlots.getContainerSize(); ++j) {
                player.drop(this.craftSlots.removeItemNoUpdate(j), false);
            }

        } else {
            for(int i = 0; i < this.craftSlots.getContainerSize(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(this.craftSlots.removeItemNoUpdate(i));
                }
            }

        }
    }


}
