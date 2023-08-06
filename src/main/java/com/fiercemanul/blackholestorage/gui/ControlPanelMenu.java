package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.Config;
import com.fiercemanul.blackholestorage.block.ControlPanelBlockEntity;
import com.fiercemanul.blackholestorage.channel.Channel;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.channel.ServerChannel;
import com.fiercemanul.blackholestorage.channel.ServerChannelManager;
import com.fiercemanul.blackholestorage.network.ControlPanelMenuActionPack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import com.fiercemanul.blackholestorage.util.Tools;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ControlPanelMenu extends AbstractContainerMenu {

    protected final Player player;
    private final Level level;
    private final BlockPos blockPos;
    public ControlPanelBlockEntity controlPanelBlock;
    /**
     * 便携终端所在物品槽位
     */
    private final int panelItemSlotIndex;
    private final ItemStack panelItem;
    private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    public final Channel channel;
    public DummyContainer dummyContainer;
    public final UUID owner;
    public boolean locked;
    public UUID channelOwner;
    public int channelID;
    public boolean craftingMode;
    public String filter;
    public byte sortType = 0;
    public byte viewType = 0;
    public boolean LShifting = false;


    //客户端调用这个
    public ControlPanelMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(BlackHoleStorage.CONTROL_PANEL_MENU.get(), containerId);
        this.level = playerInv.player.level;
        this.player = playerInv.player;

        this.blockPos = extraData.readBlockPos();
        this.panelItemSlotIndex = extraData.readInt();

        this.owner = extraData.readUUID();
        this.locked = extraData.readBoolean();
        this.craftingMode = extraData.readBoolean();
        this.filter = extraData.readUtf(64);
        this.sortType = extraData.readByte();
        this.viewType = extraData.readByte();
        this.channelOwner = extraData.readUUID();
        this.channelID = extraData.readInt();

        if (panelItemSlotIndex >= 0) this.panelItem = player.getInventory().getItem(panelItemSlotIndex);
        else this.panelItem = ItemStack.EMPTY;

        addSlots(playerInv.player, playerInv);

        this.dummyContainer = new DummyContainer();
        this.channel = ClientChannelManager.getInstance().getChannel(dummyContainer);
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
    public ControlPanelMenu(int containerId, Player player, ControlPanelBlockEntity blockEntity, int panelItemSlotIndex) {
        super(BlackHoleStorage.CONTROL_PANEL_MENU.get(), containerId);
        this.level = player.level;
        this.player = player;
        this.panelItemSlotIndex = panelItemSlotIndex;

        if (panelItemSlotIndex >= 0) {
            this.blockPos = BlockPos.ZERO;
            this.controlPanelBlock = null;
            this.panelItem = player.getInventory().getItem(panelItemSlotIndex);
            CompoundTag nbt = panelItem.getTag();
            this.owner = nbt.contains("owner") ? nbt.getUUID("owner") : player.getUUID();
            this.locked = nbt.getBoolean("locked");
            this.craftingMode = nbt.getBoolean("craftingMode");
            this.filter = nbt.getString("filter");
            this.sortType = nbt.getByte("sortType");
            this.viewType = nbt.getByte("viewType");
            CompoundTag channel = nbt.getCompound("channel");
            if (!channel.isEmpty()) {
                this.channelOwner = channel.getUUID("channelOwner");
                this.channelID = channel.getInt("channelID");
            }
        } else {
            this.blockPos = blockEntity.getBlockPos();
            this.controlPanelBlock = blockEntity;
            this.owner = blockEntity.getOwner() == null ? player.getUUID() : blockEntity.getOwner();
            this.locked = blockEntity.isLocked();
            this.craftingMode = blockEntity.getCraftingMode();
            this.filter = blockEntity.getFilter();
            this.sortType = blockEntity.getSortType();
            this.viewType = blockEntity.getViewType();
            this.channelOwner = blockEntity.getChannelOwner();
            this.channelID = blockEntity.getChannelID();
            this.panelItem = ItemStack.EMPTY;
        }

        this.channel = ServerChannelManager.getInstance().getChannel(channelOwner, channelID);
        if (!channel.isRemoved()) ((ServerChannel) this.channel).addListener((ServerPlayer) player);

        addSlots(player, player.getInventory());
    }

    /*@Override
    public void initializeContents(int pStateId, List<ItemStack> pItems, ItemStack pCarried) {
        super.initializeContents(pStateId, pItems, pCarried);
        if (level.isClientSide) dummyContainer.refreshContainer(true);
    }*/

    //按钮相关
    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> {
                if (owner.equals(player.getUUID()) || owner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
                    locked = !locked;
                    if (panelItemSlotIndex >= 0) {
                        CompoundTag nbt = panelItem.getTag();
                        //locked 的空值检测在物品上，保证到menu的不会空
                        nbt.putBoolean("locked", locked);
                        if (locked) {
                            nbt.putBoolean("craftingMode", craftingMode);
                            nbt.putString("filter", filter);
                            nbt.putInt("sortType", sortType);
                        }
                        panelItem.setTag(nbt);
                    } else {
                        controlPanelBlock.setLocked(locked);
                        if (locked) saveBlock();
                    }
                }
            }
            case 1 -> craftingMode = !craftingMode;
            case 2 -> nextSort();
            case 3 -> reverseSort();
            case 4 -> changeViewType();
            case 5 -> openChannelScreen();
        }
        return pId < 5;
    }


    //本类方法

    public void action(int actionId, String type, String id) {
        switch (actionId) {
            case Action.LEFT_CLICK_DUMMY_SLOT -> onLeftClickDummySlot(type, id);
            case Action.Right_CLICK_DUMMY_SLOT -> onRightClickDummySlot(type, id);
            case Action.LEFT_SHIFT_DUMMY_SLOT -> onLeftShiftDummySlot(type, id);
            case Action.Right_SHIFT_DUMMY_SLOT -> onRightShiftDummySlot(type, id);
            case Action.THROW_ONE -> tryThrowOneFromDummySlot(type, id);
            case Action.THROW_STICK -> tryThrowStickFromDummySlot(type, id);
            case Action.LEFT_DRAG -> onLeftDragDummySlot(type, id);
            case Action.RIGHT_DRAG -> onRightDragDummySlot(type, id);
            case Action.CLONE -> onCloneFormDummySlot(type, id);
            case Action.DRAG_CLONE -> onDragCloneDummySlot(type, id);
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

    public void onClickDummySlot(String itemId, boolean right, boolean shift) {
    }

    public void onLeftClickDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        ResourceLocation location = new ResourceLocation(id);
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (type.equals("item")) setCarried(channel.saveTakeItem(id, false));
            else if (type.equals("fluid")) {
                if (!channel.storageFluids.containsKey(id)) return;
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket")) return;
                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                channel.takeItem("minecraft:bucket", 1);
                setCarried(fluidBucket);
            }
        } else {
            //叠堆大于1不处理特殊操作，防止意外。
            if (carried.getCount() > 1) {
                channel.addItem(carried);
                return;
            }
            //特殊操作，比如取液体.
            if (type.equals("fluid") && carried.getItem().equals(Items.BUCKET)) {
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME) return;
                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                setCarried(fluidBucket);
            } else {
                //原版桶
                if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) return;
                //其他容器
                AtomicBoolean canal = new AtomicBoolean(false);
                if (type.equals("fluid")) carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                    if (!channel.storageFluids.containsKey(id)) return;
                    FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), (int) Math.min(FluidType.BUCKET_VOLUME, channel.storageFluids.get(id)));
                    int tanks = iFluidHandlerItem.getTanks();
                    for (int i = 0; i < tanks; i++) {
                        int filledAmount = iFluidHandlerItem.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        if (filledAmount == 0) continue;
                        channel.takeFluid(id, filledAmount);
                        canal.set(true);
                        break;
                    }
                });
                else if (type.equals("energy") && id.equals("blackholestorage:forge_energy"))
                    carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                        if (!iEnergyStorage.canReceive() || channel.getStorageEnergy() == 0) return;
                        int maxInputAmount = (int) Math.min(1000000, channel.getStorageEnergy());
                        int receiveEnergy = iEnergyStorage.receiveEnergy(maxInputAmount, false);
                        if (receiveEnergy == 0) return;
                        channel.removeEnergy((long) receiveEnergy);
                        canal.set(true);
                    });
                else if (type.equals("item")) carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                    if (!channel.storageItems.containsKey(id)) return;
                    int slots = iItemHandler.getSlots();
                    for (int i = 0; i < slots; i++) {
                        ItemStack tryInsertItem = new ItemStack(ForgeRegistries.ITEMS.getValue(location));
                        if (!ItemStack.isSameItemSameTags(tryInsertItem, iItemHandler.getStackInSlot(i)) && !iItemHandler.getStackInSlot(i).isEmpty()) continue;
                        int remainingSlotSpace = iItemHandler.getSlotLimit(i) - iItemHandler.getStackInSlot(i).getCount();
                        if (remainingSlotSpace <= 0) continue;
                        int transmitAmount = (int) Math.min(Integer.MAX_VALUE, channel.storageItems.get(id) / 2);
                        transmitAmount = Math.max(transmitAmount, 64000);
                        transmitAmount = (int) Math.min(transmitAmount, channel.storageItems.get(id));
                        transmitAmount = Math.min(transmitAmount, remainingSlotSpace);
                        int markAmount = transmitAmount;
                        tryInsertItem.setCount(transmitAmount);
                        for (int j = 0; j < 64; j++) {
                            ItemStack remainingItem = iItemHandler.insertItem(i, tryInsertItem, false);
                            transmitAmount = remainingItem.getCount();
                            if (transmitAmount <= 0) break;
                            tryInsertItem.setCount(transmitAmount);
                        }
                        markAmount -= transmitAmount;
                        if (markAmount > 0) {
                            channel.takeItem(id, markAmount);
                            canal.set(true);
                            return;
                        }
                    }
                });
                if (canal.get()) return;
                channel.addItem(carried);
            }
        }
    }

    public void onRightClickDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (type.equals("item")) setCarried(channel.saveTakeItem(id, true));
            if (type.equals("fluid")) {
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket")) return;
                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id)), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                channel.takeItem("minecraft:bucket", 1);
                setCarried(fluidBucket);
            }
        } else {
            if (carried.getCount() > 1) {
                channel.fillItemStack(carried, -1);
                return;
            }
            if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) return;
            AtomicBoolean canal = new AtomicBoolean(false);
            carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                int tanks = iFluidHandlerItem.getTanks();
                for (int i = 0; i < tanks; i++) {
                    FluidStack fluidStack = iFluidHandlerItem.getFluidInTank(i);
                    if (fluidStack.isEmpty() || fluidStack.hasTag()) continue;
                    long j = channel.canStoredAmount(fluidStack);
                    if (j == 0) continue;
                    FluidStack resultFluidStack = iFluidHandlerItem.drain((int) Math.min(FluidType.BUCKET_VOLUME, j), IFluidHandler.FluidAction.EXECUTE);
                    if (resultFluidStack.isEmpty()) continue;
                    channel.addFluid(resultFluidStack);
                    canal.set(true);
                    if (iFluidHandlerItem instanceof FluidBucketWrapper) setCarried(new ItemStack(Items.BUCKET));
                    return;
                }
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                if (!iEnergyStorage.canExtract() || iEnergyStorage.getEnergyStored() == 0) return;
                int extractEnergy = iEnergyStorage.extractEnergy(Math.min(1000000, channel.canStoredEnergy()), false);
                if (extractEnergy == 0) return;
                channel.addEnergy(extractEnergy);
                canal.set(true);
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                int slots = iItemHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack itemStack = iItemHandler.getStackInSlot(i);
                    if (itemStack.isEmpty() || itemStack.hasTag()) continue;
                    int maxExtractAmount = channel.canStoredAmount(itemStack);
                    itemStack = iItemHandler.extractItem(i, maxExtractAmount, false);
                    if (itemStack.isEmpty()) continue;
                    channel.addItem(itemStack);
                    canal.set(true);
                    break;
                }
            });
            if (canal.get()) return;
            channel.fillItemStack(carried, -1);
        }
    }

    public void onLeftShiftDummySlot(String type, String id) {
        if (id.equals("minecraft:air")) return;
        ItemStack carried = getCarried();
        ResourceLocation location = new ResourceLocation(id);
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (type.equals("item")) {
                if (!channel.storageItems.containsKey(id)) return;
                ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(location));
                itemStack.setCount((int) Math.min(itemStack.getMaxStackSize(), channel.storageItems.get(id)));
                int i = itemStack.getCount();
                if (craftingMode) moveItemStackTo(itemStack, 41, 50, false);
                else moveItemStackTo(itemStack, 0, 36, false);
                i = i - itemStack.getCount();
                if (i > 0) {
                    itemStack.setCount(i);
                    channel.removeItem(itemStack);
                }
            } else if (type.equals("fluid")) {
                if (!channel.storageFluids.containsKey(id)) return;
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket")) return;
                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                if (craftingMode) moveItemStackTo(fluidBucket, 41, 50, false);
                else moveItemStackTo(fluidBucket, 0, 36, false);
                if (fluidBucket.isEmpty()) {
                    channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                    channel.takeItem("minecraft:bucket", 1);
                }
            }
        } else {
            if (carried.getCount() > 1) {
                channel.fillItemStack(carried, -1);
                return;
            }
            if (type.equals("fluid") && carried.getItem().equals(Items.BUCKET)) {
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME) return;
                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                setCarried(fluidBucket);
            } else {
                if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) return;
                AtomicBoolean canal = new AtomicBoolean(false);
                //取一大堆，下限堆大小为64k桶，上限为存量的一半，防止败家行为。
                if (type.equals("fluid")) carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                    if (!channel.storageFluids.containsKey(id)) return;
                    int tanks = iFluidHandlerItem.getTanks();
                    for (int i = 0; i < tanks; i++) {
                        FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1000);
                        if (!fluidStack.isFluidEqual(iFluidHandlerItem.getFluidInTank(i)) && !iFluidHandlerItem.getFluidInTank(i).isEmpty()) continue;
                        int remainingTankSpace = iFluidHandlerItem.getTankCapacity(i) - iFluidHandlerItem.getFluidInTank(i).getAmount();
                        if (remainingTankSpace <= 0) continue;
                        int transmitAmount = (int) Math.min(Integer.MAX_VALUE, channel.storageFluids.get(id) / 2);
                        transmitAmount = Math.max(transmitAmount, 64000000);
                        transmitAmount = (int) Math.min(transmitAmount, channel.storageFluids.get(id));
                        transmitAmount = Math.min(transmitAmount, remainingTankSpace);
                        int markAmount = transmitAmount;
                        fluidStack.setAmount(transmitAmount);
                        for (int j = 0; j < 1024; j++) {
                            int filledAmount = iFluidHandlerItem.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                            if (filledAmount == 0) break;
                            transmitAmount -= filledAmount;
                            if (transmitAmount <= 0) break;
                            fluidStack.setAmount(transmitAmount);
                        }
                        markAmount -= transmitAmount;
                        if (markAmount > 0) {
                            channel.takeFluid(id, markAmount);
                            canal.set(true);
                        }
                        return;
                    }
                });
                    //但电不需要防败家，因为不缺嘿嘿嘿。
                else if (type.equals("energy") && id.equals("blackholestorage:forge_energy"))
                    carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                        if (!iEnergyStorage.canReceive() || channel.getStorageEnergy() == 0) return;
                        int maxInputAmount = (int) Math.min(Integer.MAX_VALUE, channel.getStorageEnergy());
                        int markAmount = maxInputAmount;
                        for (int i = 0; i < 1024; i++) {
                            int receiveEnergy = iEnergyStorage.receiveEnergy(maxInputAmount, false);
                            if (receiveEnergy == 0) break;
                            maxInputAmount -= receiveEnergy;
                            if (maxInputAmount == 0) break;
                        }
                        markAmount -= maxInputAmount;
                        if (markAmount > 0) {
                            channel.removeEnergy((long) markAmount);
                            canal.set(true);
                        }
                    });
                else if (type.equals("item")) carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                    if (!channel.storageItems.containsKey(id)) return;
                    int transmitAmount = (int) Math.min(Integer.MAX_VALUE, channel.storageItems.get(id) / 2);
                    transmitAmount = Math.max(transmitAmount, 64000);
                    transmitAmount = (int) Math.min(transmitAmount, channel.storageItems.get(id));
                    int markAmount = transmitAmount;
                    ItemStack tryInsertItem = new ItemStack(ForgeRegistries.ITEMS.getValue(location), transmitAmount);
                    int slots = iItemHandler.getSlots();
                    for (int i = 0; i < slots; i++) {
                        for (int j = 0; j < 64; j++) {
                            ItemStack remainingItem = iItemHandler.insertItem(i, tryInsertItem, false);
                            if (remainingItem.getCount() == transmitAmount) break;
                            transmitAmount = remainingItem.getCount();
                            if (transmitAmount <= 0) break;
                            tryInsertItem.setCount(transmitAmount);
                        }
                    }
                    markAmount -= transmitAmount;
                    if (markAmount > 0) {
                        channel.takeItem(id, markAmount);
                        canal.set(true);
                    }
                });
                if (canal.get()) return;
                channel.addItem(carried);
            }
        }
    }

    public void onRightShiftDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        ResourceLocation location = new ResourceLocation(id);
        if (carried.isEmpty()) {
            if (id.equals("minecraft:air")) return;
            if (type.equals("item")) {
                if (!channel.storageItems.containsKey(id)) return;
                ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(location));
                if (craftingMode) moveItemStackTo(itemStack, 41, 50, false);
                else moveItemStackTo(itemStack, 0, 36, false);
                if (itemStack.isEmpty()) channel.takeItem(id, 1);
            } else if (type.equals("fluid")) {
                if (!channel.storageFluids.containsKey(id)) return;
                if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket")) return;
                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1);
                ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
                if (fluidBucket.isEmpty()) return;
                if (craftingMode) moveItemStackTo(fluidBucket, 41, 50, false);
                else moveItemStackTo(fluidBucket, 0, 36, false);
                if (fluidBucket.isEmpty()) {
                    channel.takeFluid(id, FluidType.BUCKET_VOLUME);
                    channel.takeItem("minecraft:bucket", 1);
                }
            }
        } else {
            if (carried.getCount() > 1) {
                channel.fillItemStack(carried, -1);
                return;
            }
            if (Config.INCOMPATIBLE_MODID.get().contains(ForgeRegistries.ITEMS.getKey(carried.getItem()).getNamespace())) return;
            AtomicBoolean canal = new AtomicBoolean(false);
            carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(iFluidHandlerItem -> {
                int tanks = iFluidHandlerItem.getTanks();
                for (int i = 0; i < tanks; i++) {
                    FluidStack inTankFluid = iFluidHandlerItem.getFluidInTank(i);
                    if (inTankFluid.isEmpty() || inTankFluid.hasTag()) continue;
                    String fluidId = ForgeRegistries.FLUIDS.getKey(inTankFluid.getFluid()).toString();
                    int maxRemoveAmount = channel.canStoredFluid(fluidId);
                    int markAmount = maxRemoveAmount;
                    //规避限速，最大循环1024次，防止过度循环。
                    for (int j = 0; j < 1024; j++) {
                        FluidStack resultFluidStack = iFluidHandlerItem.drain(Math.min(iFluidHandlerItem.getTankCapacity(i), maxRemoveAmount), IFluidHandler.FluidAction.EXECUTE);
                        if (resultFluidStack.isEmpty()) break;
                        maxRemoveAmount -= resultFluidStack.getAmount();
                        if (iFluidHandlerItem.getFluidInTank(i).getAmount() <= 0) break;
                        if (maxRemoveAmount <= 0) break;
                    }
                    markAmount -= maxRemoveAmount;
                    if (markAmount > 0) {
                        channel.addFluid(fluidId, markAmount);
                        canal.set(true);
                        if (iFluidHandlerItem instanceof FluidBucketWrapper) setCarried(new ItemStack(Items.BUCKET));
                        return;
                    }
                }
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ENERGY).ifPresent(iEnergyStorage -> {
                if (!iEnergyStorage.canExtract() || iEnergyStorage.getEnergyStored() == 0) return;
                int maxRemoveAmount = channel.canStoredEnergy();
                int markAmount = maxRemoveAmount;
                for (int i = 0; i < 1024; i++) {
                    int extractEnergy = iEnergyStorage.extractEnergy(Math.min(iEnergyStorage.getEnergyStored(), maxRemoveAmount), false);
                    if (extractEnergy == 0) break;
                    maxRemoveAmount -= extractEnergy;
                    if (iEnergyStorage.getEnergyStored() <= 0) break;
                    if (maxRemoveAmount <= 0) break;
                }
                markAmount -= maxRemoveAmount;
                if (markAmount > 0) {
                    channel.addEnergy(markAmount);
                    canal.set(true);
                }
            });
            if (canal.get()) return;
            carried.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                int slots = iItemHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack itemStack = iItemHandler.getStackInSlot(i);
                    if (itemStack.isEmpty() || itemStack.hasTag()) continue;
                    int maxExtractAmount = channel.canStoredAmount(itemStack);
                    itemStack = iItemHandler.extractItem(i, maxExtractAmount, false);
                    if (itemStack.isEmpty()) continue;
                    channel.addItem(itemStack);
                    canal.set(true);
                }
            });
            if (canal.get()) return;
            channel.addItem(carried);
        }
    }

    public void tryThrowOneFromDummySlot(String type, String id) {
        if (id.equals("minecraft:air")) return;
        ResourceLocation location = new ResourceLocation(id);
        if (type.equals("item")) {
            if (!channel.storageItems.containsKey(id)) return;
            ItemStack itemStack = channel.takeItem(id, 1);
            player.drop(itemStack, false);
        } else if (type.equals("fluid")) {
            //笑死，对于流体这种空槽根本不触发扔事件。
            if (!channel.storageFluids.containsKey(id)) return;
            if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket")) return;
            FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1);
            ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
            if (fluidBucket.isEmpty()) return;
            channel.takeFluid(id, FluidType.BUCKET_VOLUME);
            channel.takeItem("minecraft:bucket", 1);
            player.drop(fluidBucket, false);
        }
    }

    public void tryThrowStickFromDummySlot(String type, String id) {
        if (id.equals("minecraft:air")) return;
        ResourceLocation location = new ResourceLocation(id);
        if (type.equals("item")) {
            if (!channel.storageItems.containsKey(id)) return;
            ItemStack itemStack = channel.saveTakeItem(id, false);
            player.drop(itemStack, false);
        } else if (type.equals("fluid")) {
            if (!channel.storageFluids.containsKey(id)) return;
            if (channel.storageFluids.get(id) < FluidType.BUCKET_VOLUME || !channel.storageItems.containsKey("minecraft:bucket")) return;
            FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(location), 1);
            ItemStack fluidBucket = new ItemStack(fluidStack.getFluid().getBucket());
            if (fluidBucket.isEmpty()) return;
            channel.takeFluid(id, FluidType.BUCKET_VOLUME);
            channel.takeItem("minecraft:bucket", 1);
            player.drop(fluidBucket, false);
        }
    }

    public void onLeftDragDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        channel.addItem(carried);
    }

    public void onRightDragDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        channel.fillItemStack(carried, -1);
    }

    public void onCloneFormDummySlot(String type, String id) {
        if (id.equals("minecraft:air") || !player.isCreative()) return;
        switch (type) {
            case "item" -> channel.addItem(id, channel.storageItems.getOrDefault(id, 64L));
            case "fluid" -> channel.addFluid(id, channel.storageFluids.getOrDefault(id, 1000L));
            case "energy" -> channel.addEnergy(id, channel.storageEnergies.getOrDefault(id, 1000L));
        }
    }

    public void onDragCloneDummySlot(String type, String id) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) return;
        ItemStack itemStack = carried.copy();
        itemStack.setCount(itemStack.getMaxStackSize());
        channel.addItem(itemStack);
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
        for (int j = 0; j < this.craftSlots.getContainerSize(); ++j) {
            ItemStack itemStack = this.craftSlots.removeItemNoUpdate(j);
            channel.addItem(itemStack);
            if (!itemStack.isEmpty()) player.drop(itemStack, false);
        }
    }

    public static class Sort {
        public static final byte ID_ASCENDING = 0;
        public static final byte ID_DESCENDING = 1;
        public static final byte NAMESPACE_ID_ASCENDING = 2;
        public static final byte NAMESPACE_ID_DESCENDING = 3;
        public static final byte MIRROR_ID_ASCENDING = 4;
        public static final byte MIRROR_ID_DESCENDING = 5;
        public static final byte COUNT_ASCENDING = 6;
        public static final byte COUNT_DESCENDING = 7;
    }

    public void nextSort() {
        sortType += 2;
        if (sortType > 7) sortType %= 8;
        if (level.isClientSide) dummyContainer.refreshContainer(true);
    }

    public void reverseSort() {
        if (sortType % 2 == 0) sortType++;
        else sortType--;
        if (level.isClientSide) dummyContainer.refreshContainer(true);
    }

    public static class ViewType {
        public static final byte ALL = 0;
        public static final byte Items = 1;
        public static final byte Fluids = 2;
    }

    public void changeViewType() {
        if (viewType == 2) viewType = 0;
        else viewType++;
        if (level.isClientSide) dummyContainer.onChangeViewType();
    }

    private void saveBlock() {
        controlPanelBlock.setCraftingMode(craftingMode);
        controlPanelBlock.setFilter(filter);
        controlPanelBlock.setSortType(sortType);
        controlPanelBlock.setViewType(viewType);
    }

    public class DummyContainer extends SimpleContainer {
        protected ArrayList<String> sortedItems = new ArrayList<>();
        protected ArrayList<String> sortedFluids = new ArrayList<>();
        protected ArrayList<String> sortedEnergies = new ArrayList<>();
        public final ArrayList<String[]> sortedObject = new ArrayList<>();
        public final ArrayList<String[]> viewingObject = new ArrayList<>();
        public final HashMap<Integer, FluidStack> fluidStacks = new HashMap<>();
        public ArrayList<String> formatCount = new ArrayList<>();

        private double scrollTo = 0.0D;

        public DummyContainer() {
            super(99);
        }

        public void onChangeViewType() {
            sortedObject.clear();
            switch (viewType) {
                case ViewType.ALL -> {
                    sortedItems.forEach(s -> sortedObject.add(new String[]{"item", s}));
                    sortedFluids.forEach(s -> sortedObject.add(new String[]{"fluid", s}));
                    sortedEnergies.forEach(s -> sortedObject.add(new String[]{"energy", s}));
                }
                case ViewType.Items -> sortedItems.forEach(s -> sortedObject.add(new String[]{"item", s}));
                case ViewType.Fluids -> {
                    sortedFluids.forEach(s -> sortedObject.add(new String[]{"fluid", s}));
                    sortedEnergies.forEach(s -> sortedObject.add(new String[]{"energy", s}));
                }
            }
            scrollOffset(0);
        }

        public void onScrollTo(double scrollTo) {
            this.scrollTo = scrollTo;
            scrollOffset(0);
        }

        public double getScrollOn() {
            return scrollTo;
        }

        public void scrollOffset(int offset) {
            if (sortedObject.size() <= (craftingMode ? 77 : 99)) {
                viewingObject.clear();
                viewingObject.addAll(sortedObject);
            } else {
                int i = (int) Math.ceil(sortedObject.size() / 11.0D);
                i -= craftingMode ? 7 : 9;
                int j = Math.round(i * (float) scrollTo);
                if (offset != 0) {
                    j += offset;
                    j = Math.max(0, Math.min(i, j));
                    scrollTo = (double) j / (double) i;
                }
                viewingObject.clear();
                viewingObject.addAll(sortedObject.subList(j * 11, Math.min(sortedObject.size(), j * 11 + (craftingMode ? 77 : 99))));
            }
            updateDummySlots();
        }

        public double onMouseScrolled(boolean isUp) {
            if (isUp) scrollOffset(-1);
            else scrollOffset(1);
            return scrollTo;
        }

        public void refreshContainer(boolean fullUpdate) {
            if (!level.isClientSide) return;
            if ((fullUpdate || sortType >= 6) && !LShifting) {
                sortedItems = new ArrayList<>(channel.storageItems.keySet());
                sortedFluids = new ArrayList<>(channel.storageFluids.keySet());
                sortedEnergies = new ArrayList<>(channel.storageEnergies.keySet());
                if (!filter.equals("")) {
                    ArrayList<String> temp = new ArrayList<>();
                    ArrayList<String> temp1 = new ArrayList<>();
                    ArrayList<String> temp2 = new ArrayList<>();
                    char head = filter.charAt(0);
                    if (head == '*') {
                        String s = filter.substring(1);
                        for (String itemName : sortedItems) if (itemName.contains(s)) temp.add(itemName);
                        for (String fluidName : sortedFluids) if (fluidName.contains(s)) temp1.add(fluidName);
                        for (String energyName : sortedEnergies) if (energyName.contains(s)) temp2.add(energyName);
                    } else if (head == '$') {
                        String s = filter.substring(1);
                        for (String itemName : sortedItems) {
                            ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)));
                            ArrayList<String> tags = new ArrayList<>();
                            itemStack.getTags().forEach(itemTagKey -> tags.add(itemTagKey.location().getPath()));
                            for (String tag : tags) {
                                if (tag.contains(s)) {
                                    temp.add(itemName);
                                    break;
                                }
                            }
                        }
                    } else {
                        for (String itemName : sortedItems) {
                            if (itemName.contains(filter)) temp.add(itemName);
                            else {
                                ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)));
                                if (itemStack.getDisplayName().getString().toLowerCase().contains(filter)) temp.add(itemName);
                            }
                        }
                        for (String fluidName : sortedFluids) {
                            if (fluidName.contains(filter)) temp1.add(fluidName);
                            else {
                                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)), 1);
                                if (fluidStack.getDisplayName().getString().toLowerCase().contains(filter)) temp1.add(fluidName);
                            }
                        }
                        for (String energyName : sortedEnergies) {
                            if (energyName.contains(filter)) temp2.add(energyName);
                            else {
                                ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(energyName)));
                                if (itemStack.getDisplayName().getString().toLowerCase().contains(filter)) temp2.add(energyName);
                            }
                        }
                    }
                    sortedItems = temp;
                    sortedFluids = temp1;
                    sortedEnergies = temp2;
                }
                switch (sortType) {
                    case Sort.ID_ASCENDING -> {
                        sortedItems.sort(Tools::sortFromRightID);
                        sortedFluids.sort(Tools::sortFromRightID);
                        sortedEnergies.sort(Tools::sortFromRightID);
                    }
                    case Sort.ID_DESCENDING -> {
                        sortedItems.sort(Collections.reverseOrder(Tools::sortFromRightID));
                        sortedFluids.sort(Collections.reverseOrder(Tools::sortFromRightID));
                        sortedEnergies.sort(Collections.reverseOrder(Tools::sortFromRightID));
                    }
                    case Sort.NAMESPACE_ID_ASCENDING -> {
                        sortedItems.sort(String::compareTo);
                        sortedFluids.sort(String::compareTo);
                        sortedEnergies.sort(String::compareTo);
                    }
                    case Sort.NAMESPACE_ID_DESCENDING -> {
                        sortedItems.sort(Collections.reverseOrder(String::compareTo));
                        sortedFluids.sort(Collections.reverseOrder(String::compareTo));
                        sortedEnergies.sort(Collections.reverseOrder(String::compareTo));
                    }
                    case Sort.MIRROR_ID_ASCENDING -> {
                        sortedItems.sort(Tools::sortFromMirrorID);
                        sortedFluids.sort(Tools::sortFromMirrorID);
                        sortedEnergies.sort(Tools::sortFromMirrorID);
                    }
                    case Sort.MIRROR_ID_DESCENDING -> {
                        sortedItems.sort(Collections.reverseOrder(Tools::sortFromMirrorID));
                        sortedFluids.sort(Collections.reverseOrder(Tools::sortFromMirrorID));
                        sortedEnergies.sort(Collections.reverseOrder(Tools::sortFromMirrorID));
                    }
                    case Sort.COUNT_ASCENDING -> {
                        sortedItems.sort((s1, s2) -> Tools.sortFromCount(s1, s2, channel.storageItems, false));
                        sortedFluids.sort((s1, s2) -> Tools.sortFromCount(s1, s2, channel.storageFluids, false));
                        sortedEnergies.sort((s1, s2) -> Tools.sortFromCount(s1, s2, channel.storageEnergies, false));
                    }
                    case Sort.COUNT_DESCENDING -> {
                        sortedItems.sort((s1, s2) -> Tools.sortFromCount(s1, s2, channel.storageItems, true));
                        sortedFluids.sort((s1, s2) -> Tools.sortFromCount(s1, s2, channel.storageFluids, true));
                        sortedEnergies.sort((s1, s2) -> Tools.sortFromCount(s1, s2, channel.storageEnergies, true));
                    }
                }
                onChangeViewType();
                return;
            }
            updateDummySlots();
        }

        public void updateDummySlots() {
            formatCount.clear();
            fluidStacks.clear();
            for (int j = 0; j < (craftingMode ? 77 : 99); j++) {
                if (j < viewingObject.size() && viewingObject.get(j) != null) {
                    if (viewingObject.get(j)[0].equals("fluid")) {
                        this.setItem(j, ItemStack.EMPTY);
                        String id = viewingObject.get(j)[1];
                        fluidStacks.put(j, new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id)), 1));
                        if (!channel.storageFluids.containsKey(id)) {
                            formatCount.add(j, "§c0");
                            continue;
                        }
                        long count = channel.storageFluids.get(id);
                        if (count < Long.MAX_VALUE) {
                            String stringCount = Tools.DECIMAL_FORMAT.format(count);
                            stringCount = stringCount.substring(0, 4);
                            if (stringCount.endsWith(",")) stringCount = stringCount.substring(0, 3);
                            stringCount = stringCount.replace(",", ".");
                            if (count < 1000L) stringCount += "mB";
                            else if (count < 1000000L) stringCount += "";
                            else if (count < 1000000000L) stringCount += "K";
                            else if (count < 1000000000000L) stringCount += "M";
                            else if (count < 1000000000000000L) stringCount += "G";
                            else if (count < 1000000000000000000L) stringCount += "T";
                            else stringCount += "P";
                            formatCount.add(j, stringCount);
                        } else formatCount.add(j, "MAX");
                    } else {
                        //叠堆数为1避开原版的数字渲染
                        String id = viewingObject.get(j)[1];
                        ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));
                        this.setItem(j, itemStack);
                        long count;
                        if (viewingObject.get(j)[0].equals("item")) {
                            if (channel.storageItems.containsKey(id)) {
                                count = channel.storageItems.get(id);
                            } else {
                                formatCount.add(j, "§c0");
                                continue;
                            }
                        } else {
                            if (channel.storageEnergies.containsKey(id)) {
                                count = channel.storageEnergies.get(id);
                            } else {
                                formatCount.add(j, "§c0");
                                continue;
                            }
                        }
                        if (count < 1000L) formatCount.add(j, String.valueOf(count));
                        else if (count < Long.MAX_VALUE) {
                            String stringCount = Tools.DECIMAL_FORMAT.format(count);
                            stringCount = stringCount.substring(0, 4);
                            if (stringCount.endsWith(",")) stringCount = stringCount.substring(0, 3);
                            stringCount = stringCount.replace(",", ".");
                            if (count < 1000000L) stringCount += "K";
                            else if (count < 1000000000L) stringCount += "M";
                            else if (count < 1000000000000L) stringCount += "G";
                            else if (count < 1000000000000000L) stringCount += "T";
                            else if (count < 1000000000000000000L) stringCount += "P";
                            else stringCount += "E";
                            formatCount.add(j, stringCount);
                            // 9,223,372,036,854,775,807L
                            // e  p   t   g   m   k
                        } else formatCount.add(j, "MAX");
                    }
                } else this.setItem(j, ItemStack.EMPTY);
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

    private class DummySlot extends Slot {
        public DummySlot(int slotId, int x, int y) {
            super(dummyContainer, slotId, x, y);
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


    //覆写

    @Override
    public void clicked(int pMouseX, int pMouseY, ClickType pClickType, Player pPlayer) {
        if (pMouseX >= 51) {
            //仅客户端能触发
            String[] object;
            if (pMouseX - 51 < dummyContainer.viewingObject.size()) object = dummyContainer.viewingObject.get(pMouseX - 51);
            else object = new String[]{"item", "minecraft:air"};

            switch (pMouseY) {
                case 0 -> {
                    switch (pClickType) {
                        case QUICK_MOVE -> {
                            //左键shift
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.LEFT_SHIFT_DUMMY_SLOT, object));
                            onLeftShiftDummySlot(object[0], object[1]);
                        }
                        case PICKUP -> {
                            //左键点击
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.LEFT_CLICK_DUMMY_SLOT, object));
                            onLeftClickDummySlot(object[0], object[1]);
                        }
                        case THROW -> {
                            //丢一个
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.THROW_ONE, object));
                            tryThrowOneFromDummySlot(object[0], object[1]);
                        }
                    }
                }
                case 1 -> {
                    switch (pClickType) {
                        case PICKUP -> {
                            //右键点击
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.Right_CLICK_DUMMY_SLOT, object));
                            onRightClickDummySlot(object[0], object[1]);
                        }
                        case QUICK_MOVE -> {
                            //右键shift 快速拿一个
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.Right_SHIFT_DUMMY_SLOT, object));
                            onRightShiftDummySlot(object[0], object[1]);
                        }
                        case QUICK_CRAFT -> {
                            //左键拖动
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.LEFT_DRAG, object));
                            onLeftDragDummySlot(object[0], object[1]);
                        }
                        case THROW -> {
                            //丢一组
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.THROW_STICK, object));
                            tryThrowStickFromDummySlot(object[0], object[1]);
                        }
                    }
                }
                case 4 -> {
                    if (pClickType == ClickType.CLONE) {
                        //复制
                        if (object[0].equals("item") && object[1].equals("minecraft:air")) return;
                        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.CLONE, object));
                        onCloneFormDummySlot(object[0], object[1]);
                    }
                }
                case 5 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //右键拖动
                        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.RIGHT_DRAG, object));
                        onRightDragDummySlot(object[0], object[1]);
                    }
                }
                case 9 -> {
                    if (pClickType == ClickType.QUICK_CRAFT) {
                        //拖动复制
                        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelMenuActionPack(containerId, Action.DRAG_CLONE, object));
                        onDragCloneDummySlot(object[0], object[1]);
                    }
                }
            }
            //剩下的SWAP无视掉(hot bar的快捷键)
        } else if (pMouseX != panelItemSlotIndex) super.clicked(pMouseX, pMouseY, pClickType, pPlayer);
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
                    return ItemStack.EMPTY;
                }
            } else if (slotId == 50) {
                movingStack.getItem().onCraftedBy(movingStack, level, player);
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
        if (panelItemSlotIndex >= 0) return panelItem == player.getInventory().getItem(panelItemSlotIndex);
        else
            return !controlPanelBlock.isRemoved() && !channel.isRemoved() && player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 16.0D;
    }

    @Override
    public void removed(Player player) {
        if (level.isClientSide) return;
        if (!channel.isRemoved()) ((ServerChannel) channel).removeListener((ServerPlayer) player);
        super.removed(player);
        clearCraftSlots();
        if (panelItemSlotIndex >= 0) {
            CompoundTag nbt = panelItem.getTag();
            //locked 的空值检测在物品上，保证到menu的不会空
            if (!nbt.getBoolean("locked")) {
                nbt.putBoolean("locked", locked);
                nbt.putBoolean("craftingMode", craftingMode);
                nbt.putString("filter", filter);
                nbt.putByte("sortType", sortType);
                nbt.putByte("viewType", viewType);
                if (channel.isRemoved()) nbt.remove("channel");
                panelItem.setTag(nbt);
            }
        } else {
            if (!controlPanelBlock.isLocked()) saveBlock();
        }
    }

    private void openChannelScreen() {
        if (locked) return;
        if (panelItemSlotIndex >= 0)
            NetworkHooks.openScreen((ServerPlayer) player,
                    new ChannelSelectMenuProvider(new ItemChannelTerminal(player.getInventory(), panelItem, panelItemSlotIndex)), buf -> {});
        else NetworkHooks.openScreen((ServerPlayer) player, new ChannelSelectMenuProvider(controlPanelBlock), buf -> {});
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
        slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots);
    }

    @SuppressWarnings("unused")
    public int getResultSlotIndex() {
        return 50;
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
