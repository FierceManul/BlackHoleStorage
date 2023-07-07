package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.block.ControlPanelBlockEntity;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.channel.Channel;
import com.fiercemanul.blackholestorage.channel.ServerChannelManager;
import com.fiercemanul.blackholestorage.network.ControlPanelMenuActionPack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.CONTROL_PANEL_MENU;

public class ControlPanelMenu extends AbstractContainerMenu {

    private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    public final Channel channel = new Channel();
    public final DummyContainer dummyContainer;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");
    private ContainerLevelAccess access;
    protected Player player;
    private Level level;
    @Nullable
    public ControlPanelBlockEntity controlPanelBlock;
    public UUID owner;
    public String ownerName;
    public boolean locked = false;
    public UUID channelOwner;
    public String channelOwnerName;
    public boolean craftingMode = false;


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
        super(CONTROL_PANEL_MENU.get(), containerId);
        this.level = playerInv.player.level;
        this.access = ContainerLevelAccess.create(playerInv.player.level, extraData.readBlockPos());
        this.player = playerInv.player;
        //客户端getBlockEntity会导致gui呼不出来,但为什么get不get都是null，get了就出问题了呢？
        //this.controlPanelBlock = (ControlPanelBlockEntity) playerInv.player.level.getBlockEntity(extraData.readBlockPos());
        this.owner = extraData.readUUID();
        this.locked = extraData.readBoolean();
        this.craftingMode = extraData.readNbt().getBoolean("craftingMode");

        this.ownerName = ClientChannelManager.getInstance().getUserName(this.owner);

        addSlots(playerInv.player, playerInv);

        this.dummyContainer = new DummyContainer();

        //虚拟储存物品格51 ~ 149
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 11; j++) {
                this.addSlot(new DummySlot(i * 11 + j, 6 + j * 17, 6 + i * 17));
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 11; j++) {
                this.addSlot(new DummySlot(77 + i * 11 + j, 6 + j * 17, 125 + i * 17) {
                    @Override
                    public boolean isActive() {
                        return !craftingMode;
                    }
                });
            }
        }
    }

    //服务端用这个
    public ControlPanelMenu(int containerId, Inventory playerInv, Player player, Level level, BlockPos pos, CompoundTag nbt) {
        super(CONTROL_PANEL_MENU.get(), containerId);
        this.level = level;
        this.access = ContainerLevelAccess.create(level, pos);
        this.player = player;
        this.controlPanelBlock = (ControlPanelBlockEntity) level.getBlockEntity(pos);
        this.owner = nbt.getUUID("owner");
        this.locked = nbt.getBoolean("locked");
        this.craftingMode = nbt.getBoolean("craftingMode");

        this.ownerName = ServerChannelManager.getInstance().getUserName(this.owner);

        addSlots(player, playerInv);

        this.dummyContainer = null;
    }

    private void addSlots(Player player, Inventory playerInv) {
        //快捷栏0~8
        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInv, l, 23 + l * 17, 227));
        }

        //背包9~35
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInv, i1 + k * 9 + 9, 23 + i1 * 17, 176 + k * 17));
            }
        }

        //护甲36~40
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.HEAD, 39, 142, 125));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.CHEST, 38, 159, 125));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.LEGS, 37, 159, 142));
        this.addSlot(getArmorSlot(player, playerInv, EquipmentSlot.FEET, 36, 142, 142));
        this.addSlot(new Slot(playerInv, 40, 159, 159) {
            @Override
            public boolean isActive() {
                return craftingMode;
            }
        });

        //合成格41~50
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 3, 23 + j * 17, 125 + i * 17) {
                    @Override
                    public boolean isActive() {
                        return craftingMode;
                    }
                });
            }
        }
        this.addSlot(new ResultSlot(player, this.craftSlots, this.resultSlots, 0, 74, 142) {
            @Override
            public boolean isActive() {
                return craftingMode;
            }
        });
    }


    //按钮相关
    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            //TODO: lock需要检查uuid，测试暂时不做
            case 0 -> locked = !locked;
            case 1 -> {
                craftingMode = !craftingMode;
                refreshDummyContainer();
            }
        }
        return pId < 2;
    }


    //合成相关
    protected static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer) {
        if (!level.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer) player;
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

    @SuppressWarnings("unused")
    public int getResultSlotIndex() {
        return 50;
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
        //TODO:物品送回频道
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer) player).hasDisconnected()) {
            for (int j = 0; j < this.craftSlots.getContainerSize(); ++j) {
                player.drop(this.craftSlots.removeItemNoUpdate(j), false);
            }

        } else {
            for (int i = 0; i < this.craftSlots.getContainerSize(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(this.craftSlots.removeItemNoUpdate(i));
                }
            }

        }
    }

    public void action(int actionId, String itemId, int count) {
        switch (actionId) {
            case Action.LEFT_CLICK_DUMMY_SLOT -> onLeftClickDummySlot(itemId, count);
            case Action.Right_CLICK_DUMMY_SLOT -> onRightClickDummySlot(itemId, count);
            case Action.LEFT_SHIFT_DUMMY_SLOT -> onLeftShiftDummySlot(itemId);
            case Action.Right_SHIFT_DUMMY_SLOT -> onRightShiftDummySlot(itemId);
            case Action.THROW_ONE -> tryThrowOneFromDummySlot(itemId);
            case Action.THROW_STICK -> tryThrowStickFromDummySlot(itemId, count);
            case Action.LEFT_DRAG -> onLeftDragDummySlot(itemId);
            case Action.RIGHT_DRAG -> onRightDragDummySlot(itemId);
            case Action.CLONE -> onCloneFormDummySlot(itemId);
            case Action.DRAG_CLONE -> onDragCloneDummySlot(itemId);
        }
    }

    private static final class Action {
        public static final int LEFT_CLICK_DUMMY_SLOT = 0;
        public static final int Right_CLICK_DUMMY_SLOT = 1;
        public static final int LEFT_SHIFT_DUMMY_SLOT = 2;
        public static final int Right_SHIFT_DUMMY_SLOT = 3;
        public static final int THROW_ONE = 4;
        public static final int THROW_STICK = 5;
        public static final int LEFT_DRAG = 6;
        public static final int RIGHT_DRAG = 7;
        public static final int CLONE = 8;
        public static final int DRAG_CLONE = 9;
    }

    public void onLeftClickDummySlot(String itemId, int count) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (itemId.equals("minecraft:air")) return;
            setCarried(channel.takeItem(itemId, count));
        } else {
            channel.addItem(carried);
        }
        this.refreshDummyContainer();
    }

    public void onRightClickDummySlot(String itemId, int count) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (itemId.equals("minecraft:air")) return;
            setCarried(channel.takeItem(itemId, (count + 1) / 2));
        } else {
            channel.fillItemStack(carried, -1);
        }
        this.refreshDummyContainer();
    }

    public void onLeftShiftDummySlot(String itemId) {
        if (itemId.equals("minecraft:air")) return;
        ItemStack itemStack = channel.saveTakeItem(itemId, 64);
        if (itemStack.isEmpty()) {
            this.refreshDummyContainer();
            return;
        }
        if (craftingMode) moveItemStackTo(itemStack, 41, 50, false);
        else moveItemStackTo(itemStack, 0, 36, false);
        //不要合并这两个if
        if (!itemStack.isEmpty()) channel.addItem(itemStack);
        if (!itemStack.isEmpty()) player.drop(itemStack, false);
        this.refreshDummyContainer();
    }

    public void onRightShiftDummySlot(String itemId) {
        if (itemId.equals("minecraft:air")) return;
        ItemStack itemStack = channel.takeItem(itemId, 1);
        if (itemStack.isEmpty()) {
            this.refreshDummyContainer();
            return;
        }
        if (craftingMode) moveItemStackTo(itemStack, 41, 50, false);
        else moveItemStackTo(itemStack, 0, 36, false);
        //不要合并这两个if
        if (!itemStack.isEmpty()) channel.addItem(itemStack);
        if (!itemStack.isEmpty()) player.drop(itemStack, false);
        this.refreshDummyContainer();
    }

    public void tryThrowOneFromDummySlot(String itemId) {
        ItemStack itemStack = channel.takeItem(itemId, 1);
        if (itemStack.isEmpty()) return;
        player.drop(itemStack, false);
        this.refreshDummyContainer();
    }

    public void tryThrowStickFromDummySlot(String itemId, int count) {
        ItemStack itemStack = channel.takeItem(itemId, count);
        if (itemStack.isEmpty()) return;
        player.drop(itemStack, false);
        this.refreshDummyContainer();
    }

    public void onLeftDragDummySlot(String itemId) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        channel.addItem(carried);
        this.refreshDummyContainer();
    }

    public void onRightDragDummySlot(String itemId) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        channel.fillItemStack(carried, -1);
        this.refreshDummyContainer();
    }

    public void onCloneFormDummySlot(String itemId) {
        if (player.isCreative() && channel.storageItems.containsKey(itemId)) {
            channel.addItem(itemId, channel.storageItems.get(itemId));
        }
        this.refreshDummyContainer();
    }

    public void onDragCloneDummySlot(String itemId) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        ItemStack itemStack = carried.copy();
        itemStack.setCount(itemStack.getMaxStackSize());
        channel.addItem(itemStack);
        this.refreshDummyContainer();
    }

    public void refreshDummyContainer() {
        if (level.isClientSide) {
            this.dummyContainer.refreshContainer();
        }
    }

    public class DummySlot extends Slot {
        public DummySlot(int slotId, int x, int y) {
            super(dummyContainer, slotId, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxStackSize(ItemStack pStack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public void initialize(ItemStack itemStack) {
            super.initialize(itemStack);
        }

        @Override
        public void set(ItemStack pStack) {
        }

        @Override
        public void onTake(Player pPlayer, ItemStack pStack) {
        }

        @Override
        public ItemStack remove(int pAmount) {
            return ItemStack.EMPTY;
        }

        @Override
        public Optional<ItemStack> tryRemove(int pCount, int pDecrement, Player pPlayer) {
            return Optional.of(ItemStack.EMPTY);
        }

        @Override
        public ItemStack safeInsert(ItemStack pStack, int pIncrement) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack safeTake(int pCount, int pDecrement, Player pPlayer) {
            return ItemStack.EMPTY;
        }

        @Override
        public void onQuickCraft(ItemStack pOldStack, ItemStack pNewStack) {
        }

        @Override
        public void setChanged() {
        }
    }

    public class DummyContainer extends SimpleContainer {
        public ArrayList<String> stringCountTemp = new ArrayList<>();

        public DummyContainer() {
            super(99);
        }

        public void refreshContainer() {
            if (level.isClientSide) {
                int i;
                if (craftingMode) i = Integer.min(77, channel.storageItems.size());
                else i = Integer.min(99, channel.storageItems.size());
                //
                String[] itemNames = channel.storageItems.keySet().toArray(new String[0]);
                stringCountTemp.clear();
                for (int j = 0; j < 99; j++) {
                    if (j < i) {
                        //叠堆数为1避开原版的数字渲染
                        ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemNames[j])));
                        this.setItem(j, itemStack);
                        int count = channel.storageItems.get(itemNames[j]);
                        if (count < 1000 ) this.stringCountTemp.add(j, String.valueOf(count));
                        else if (count < Integer.MAX_VALUE) {
                            String stringCount = DECIMAL_FORMAT.format(count);
                            stringCount = stringCount.substring(0, 4);
                            if (stringCount.endsWith(",")) stringCount = stringCount.substring(0, 3);
                            stringCount = stringCount.replace(",", ".");
                            if (count < 1000000) stringCount += "K";
                            else if (count < 1000000000) stringCount += "M";
                            else stringCount += "G";
                            this.stringCountTemp.add(j, stringCount);
                        } else this.stringCountTemp.add(j, "MAX");
                    } else this.setItem(j, ItemStack.EMPTY);
                }
            }
        }

        @Override
        public void setChanged() {
        }

        @Override
        public int getMaxStackSize() {
            return Integer.MAX_VALUE;
        }

    }


    //覆写

    @Override
    public void clicked(int pMouseX, int pMouseY, ClickType pClickType, Player pPlayer) {
        if (pMouseX >= 51) {
            //仅客户端能触发
            int index = pMouseX - 51;
            ItemStack itemStack = dummyContainer.getItem(index);
            String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
            int maxStackSize = itemStack.getMaxStackSize();
            switch (pMouseY) {
                case 0 -> {
                    switch (pClickType) {
                        case QUICK_MOVE -> {
                            //左键shift
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                    new ControlPanelMenuActionPack(containerId, Action.LEFT_SHIFT_DUMMY_SLOT, itemId, 0));
                            onLeftShiftDummySlot(itemId);
                        }
                        case PICKUP -> {
                            //左键点击
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                    new ControlPanelMenuActionPack(containerId, Action.LEFT_CLICK_DUMMY_SLOT, itemId, maxStackSize));
                            onLeftClickDummySlot(itemId, maxStackSize);
                        }
                        case THROW -> {
                            //丢一个
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                    new ControlPanelMenuActionPack(containerId, Action.THROW_ONE, itemId, 0));
                            tryThrowOneFromDummySlot(itemId);
                        }
                    }
                }
                case 1 -> {
                    switch (pClickType) {
                        case PICKUP -> {
                            //右键点击
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                    new ControlPanelMenuActionPack(containerId, Action.Right_CLICK_DUMMY_SLOT, itemId, maxStackSize));
                            onRightClickDummySlot(itemId, maxStackSize);
                        }
                        case QUICK_MOVE -> {
                            //右键shift 快速拿一个
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                    new ControlPanelMenuActionPack(containerId, Action.Right_SHIFT_DUMMY_SLOT, itemId, 0));
                            onRightShiftDummySlot(itemId);
                        }
                        case QUICK_CRAFT -> {
                            //左键拖动
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                    new ControlPanelMenuActionPack(containerId, Action.LEFT_DRAG, itemId, 0));
                            onLeftDragDummySlot(itemId);
                        }
                        case THROW -> {
                            //丢一组
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                    new ControlPanelMenuActionPack(containerId, Action.THROW_STICK, itemId, maxStackSize));
                            tryThrowStickFromDummySlot(itemId, maxStackSize);
                        }
                    }
                }
                case 4 -> {
                    if (pClickType == ClickType.CLONE) {
                        //复制
                        if (itemStack.isEmpty()) return;
                        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                new ControlPanelMenuActionPack(containerId, Action.CLONE, itemId, maxStackSize));
                        onCloneFormDummySlot(itemId);
                    }
                }
                case 5 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //右键拖动
                        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                new ControlPanelMenuActionPack(containerId, Action.RIGHT_DRAG, itemId, 0));
                        onRightDragDummySlot(itemId);
                    }
                }
                case 9 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //拖动复制
                        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                new ControlPanelMenuActionPack(containerId, Action.DRAG_CLONE, itemId, 0));
                        onDragCloneDummySlot(itemId);
                    }
                }
            }
            //剩下的SWAP无视掉(hot bar的快捷键)
        } else super.clicked(pMouseX, pMouseY, pClickType, pPlayer);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        //empty由于退出调用的奇怪循环
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            ItemStack movingStack = slot.getItem();
            itemStack = movingStack.copy();
            if (slotId >= 0 && slotId <= 35) {
                if (craftingMode) {
                    if (!this.moveItemStackTo(movingStack, 41, 50, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    channel.addItem(movingStack);
                    this.refreshDummyContainer();
                    return ItemStack.EMPTY;
                }
            } else if (slotId == 50) {
                this.access.execute((level, pos) -> {
                    movingStack.getItem().onCraftedBy(movingStack, level, player);
                });
                if (!this.moveItemStackTo(movingStack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(movingStack, itemStack);
            } else if (slotId >= 41 && slotId <= 49) {
                if (!this.moveItemStackTo(movingStack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                BlackHoleStorage.LOGGER.warn("Ohh! Who trigger the quickMoveStack() when slotId >= 51 ?");
            }
            if (movingStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (movingStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, movingStack);
            if (slotId == 50) {
                player.drop(movingStack, false);
            }
        }
        return itemStack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.index <= 49;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BlackHoleStorage.CONTROL_PANEL.get());
    }

    @Override
    public void removed(Player player) {
        if (level.isClientSide) return;
        super.removed(player);
        this.access.execute((level, pos) -> {
            clearCraftSlots();
        });
        controlPanelBlock.setLocked(locked);
        if (!locked) {
            controlPanelBlock.setCraftingMode(craftingMode);
        }
    }


    //未知用途
    /*
    public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
        return recipe.matches(this.craftSlots, this.player.level);
    }

    public void fillCraftSlotsStackedContents(StackedContents contents) {
        this.craftSlots.fillStackedContents(contents);
    }

    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }
    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }
    public int getSize() {
        return 10;
    }

    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    public boolean shouldMoveToInventory(int i) {
        return i != this.getResultSlotIndex();
    }*/
}
