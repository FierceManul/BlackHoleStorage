package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.block.ActivePortBlockEntity;
import com.fiercemanul.blackholestorage.channel.InfoPort;
import com.fiercemanul.blackholestorage.channel.InfoRule;
import com.fiercemanul.blackholestorage.channel.RuleType;
import com.fiercemanul.blackholestorage.util.Tools;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.UUID;

public class ActivePortMenu extends AbstractContainerMenu {

    protected final Player player;
    protected final UUID owner;
    protected boolean locked;
    protected final ActivePortBlockEntity activePort;
    protected final BlockPos blockPos;
    protected final UUID channelOwner;
    protected final String channelName;
    protected final CheckerContainer checkerContainer = new CheckerContainer();
    protected InfoPort editingNorthPort;
    protected InfoPort editingSouthPort;
    protected InfoPort editingWestPort;
    protected InfoPort editingEastPort;
    protected InfoPort editingDownPort;
    protected InfoPort editingUpPort;
    private InfoPort selectedPort;
    private boolean portInput = false;
    private ArrayList<InfoRule> selectedRules;
    protected ChoosingRules choosingRules;
    protected int editingRate;
    protected DummyContainer dummyContainer;
    protected int scrollAt = 0;



    public ActivePortMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(BlackHoleStorage.ACTIVE_PORT_MENU.get(), containerId);
        this.player = playerInv.player;
        this.owner = extraData.readUUID();
        this.locked = extraData.readBoolean();
        this.activePort = null;
        this.blockPos = extraData.readBlockPos();
        this.channelOwner = extraData.readUUID();
        this.channelName = extraData.readUtf();
        this.editingNorthPort = new InfoPort(extraData.readNbt());
        this.editingSouthPort = new InfoPort(extraData.readNbt());
        this.editingWestPort = new InfoPort(extraData.readNbt());
        this.editingEastPort = new InfoPort(extraData.readNbt());
        this.editingDownPort = new InfoPort(extraData.readNbt());
        this.editingUpPort = new InfoPort(extraData.readNbt());
        this.editingRate = extraData.readInt();
        this.selectedPort = switch (Direction.from3DDataValue(extraData.readInt())) {
            case DOWN -> editingDownPort;
            case UP -> editingUpPort;
            case NORTH -> editingNorthPort;
            case SOUTH -> editingSouthPort;
            case WEST -> editingWestPort;
            case EAST -> editingEastPort;
        };
        this.selectedRules = selectedPort.outputRules;
        this.choosingRules = new ChoosingRules();

        dummyContainer = new DummyContainer();
        addSlots(playerInv);
        for (int i = 0; i < 5; ++i) {
            this.addSlot(new FakeSlot(dummyContainer, i, 31, 34 + i * 28));
        }
    }

    public ActivePortMenu(int containerId, Player player, ActivePortBlockEntity activePort) {
        super(BlackHoleStorage.ACTIVE_PORT_MENU.get(), containerId);
        this.player = player;
        this.owner = activePort.getOwner();
        this.locked = activePort.isLocked();
        this.activePort = activePort;
        this.blockPos = activePort.getBlockPos();
        this.channelOwner = activePort.getChannelInfo().owner();
        this.channelName = activePort.getChannelName();

        addSlots(player.getInventory());
    }

    private void addSlots(Inventory playerInv) {
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

        addSlot(new Slot(checkerContainer, 0, 31, 146) {
            @Override
            public boolean isActive() {
                return checkedSlotActive();
            }
        });
    }

    @Override
    @ParametersAreNonnullByDefault
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        if (pSlotId >= 37) return;
        super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        if (pIndex > 36) return ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            if (pIndex == 36) {
                if (!moveItemStackTo(slot.getItem(), 0, 36, true)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(slot.getItem(), 36, 37, true)) return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }



    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return !activePort.isRemoved() &&
                player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 32.0D;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void removed(Player pPlayer) {
        if (!player.level.isClientSide) activePort.setUser(null);
        ItemStack itemStack = checkerContainer.removeItemNoUpdate(0);
        moveItemStackTo(itemStack, 0, 36, true);
        if (!itemStack.isEmpty()) pPlayer.drop(itemStack, false);
        super.removed(pPlayer);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        switch (pId) {
            case 0 -> toggleLock();
            case 1 -> openChannelScreen();
        }
        return true;
    }

    protected void toggleLock() {
        if (owner.equals(player.getUUID()) || owner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
            this.locked = !locked;
            activePort.setLocked(locked);
        }
    }

    private void openChannelScreen() {
        if (locked) return;
        NetworkHooks.openGui((ServerPlayer) player, new ChannelSelectMenuProvider(activePort), buf -> {});
    }

    public InfoPort getSelectedPort() {
        return selectedPort;
    }

    public void setSelectedPort(InfoPort port) {
        this.selectedPort = port;
        selectedRules = portInput ? selectedPort.inputRules : selectedPort.outputRules;
        dummyContainer.updateItem();
    }

    public boolean isPortInput() {
        return portInput;
    }

    public void setPortInput(boolean portInput) {
        this.portInput = portInput;
        selectedRules = portInput ? selectedPort.inputRules : selectedPort.outputRules;
        dummyContainer.updateItem();
    }

    public ArrayList<InfoRule> getSelectedRules() {
        return selectedRules;
    }

    public @Nullable InfoRule getRule(int buttonId) {
        if (scrollAt + buttonId < selectedRules.size()) return selectedRules.get(scrollAt + buttonId);
        return null;
    }

    public boolean hasRule(int buttonId) {
        return scrollAt + buttonId < selectedRules.size();
    }

    public boolean checkedSlotActive() {
        return scrollAt + 4 >= selectedRules.size();
    }

    public void ruleUp(int buttonId) {
        if (locked) return;
        int pos = scrollAt + buttonId;
        if (pos <= 0) return;
        InfoRule rule = selectedRules.get(pos -1);
        selectedRules.set(pos -1, selectedRules.get(pos));
        selectedRules.set(pos, rule);
        dummyContainer.updateItem();
    }

    public void ruleDown(int buttonId) {
        if (locked) return;
        int pos = scrollAt + buttonId;
        if (pos >= selectedRules.size() - 1) return;
        InfoRule rule = selectedRules.get(pos +1);
        selectedRules.set(pos +1, selectedRules.get(pos));
        selectedRules.set(pos, rule);
        dummyContainer.updateItem();
    }

    public void deleteRule(int buttonId) {
        if (locked) return;
        selectedRules.remove(scrollAt + buttonId);
        if (selectedRules.size() > 4) {
            if (scrollAt > selectedRules.size() - 4) scrollAt = selectedRules.size() - 4;
        } else scrollAt = 0;
        if (selectedPort.inputRules.size() + selectedPort.outputRules.size() == 0) selectedPort.enable = false;
        dummyContainer.updateItem();
    }

    public class CheckerContainer extends SimpleContainer {

        private ItemStack lastStack = ItemStack.EMPTY;

        public CheckerContainer() {
            super(1);
        }

        @Override
        public void setChanged() {
            if (!player.isLocalPlayer()) return;
            ItemStack itemStack = getItem(0);
            if (itemStack == lastStack) return;
            lastStack = itemStack;
            makeRules(itemStack);
        }

        public void forceMakeRules() {
            makeRules(getItem(0));
        }

        public void makeRules(ItemStack itemStack) {
            choosingRules.rules.clear();
            if (!itemStack.isEmpty()) {
                choosingRules.rules.add(new InfoRule(RuleType.ITEM, Tools.getItemId(itemStack.getItem()), 1));
                ArrayList<String> tagArrayList = new ArrayList<>();
                itemStack.getTags().forEach(tagKey -> tagArrayList.add(tagKey.location().toString()));
                if (itemStack.getItem() instanceof BlockItem blockItem) blockItem.getBlock().builtInRegistryHolder()
                        .tags().forEach(blockTagKey -> {
                            String tag = blockTagKey.location().toString();
                            if (!tagArrayList.contains(tag)) tagArrayList.add(tag);
                        });
                tagArrayList.forEach(s -> choosingRules.rules.add(new InfoRule(RuleType.ITEM_TAG, s, 1)));
                itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(fluidHandler -> {
                    int tanks = fluidHandler.getTanks();
                    for (int i = 0; i < tanks; i++) {
                        Fluid fluid = fluidHandler.getFluidInTank(i).getFluid();
                        if (fluid.equals(Fluids.EMPTY)) continue;
                        choosingRules.rules.add(new InfoRule(RuleType.FLUID, Tools.getFluidId(fluid), 1));
                    }
                });
                choosingRules.rules.add(new InfoRule(RuleType.MOD_ITEM, ForgeRegistries.ITEMS.getKey(itemStack.getItem()).getNamespace(), 1));
                choosingRules.rules.add(new InfoRule(RuleType.MOD_FLUID, ForgeRegistries.ITEMS.getKey(itemStack.getItem()).getNamespace(), 1));
            }
            if (isPortInput()) {
                choosingRules.rules.add(new InfoRule(RuleType.ANY_ITEM, "", 1));
                choosingRules.rules.add(new InfoRule(RuleType.ANY_FLUID, "", 1));
            }
            choosingRules.rules.add(new InfoRule(RuleType.FORGE_ENERGY, "", 1));
            choosingRules.choosingIndex = 0;
            choosingRules.updateDisplay();
        }
    }

    public class ChoosingRules {

        public final ArrayList<InfoRule> rules = new ArrayList<>();
        public final ArrayList<FormattedCharSequence> rulesTooltip = new ArrayList<>();
        private int choosingIndex = 0;

        public ChoosingRules() {
            rules.add(new InfoRule(RuleType.FORGE_ENERGY, "", 1));
            updateDisplay();
        }

        public void updateDisplay() {
            rulesTooltip.clear();
            if (rules.size() <= 16) {
                for (int i = 0; i < rules.size(); i++) {
                    MutableComponent display = rules.get(i).getDisplay();
                    if (i == choosingIndex) display.withStyle(ChatFormatting.GREEN);
                    else display.withStyle(ChatFormatting.DARK_GRAY);
                    rulesTooltip.add(display.getVisualOrderText());
                }
            } else {
                int start;
                int end;
                if (choosingIndex - 8 < 0) {
                    start = 0;
                    end = 15;
                } else if (choosingIndex + 7 >= rules.size()) {
                    start = rules.size() - 16;
                    end = rules.size() - 1;
                } else {
                    start = choosingIndex - 8;
                    end = choosingIndex + 7;
                }
                if (start == 0) {
                    if (choosingIndex == 0) rulesTooltip.add(rules.get(0).getDisplay().withStyle(ChatFormatting.GREEN).getVisualOrderText());
                    else rulesTooltip.add(rules.get(0).getDisplay().withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                } else rulesTooltip.add(new TextComponent("...").withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                for (int i = start + 1; i < end; i++) {
                    if (i == choosingIndex) rulesTooltip.add(rules.get(i).getDisplay().withStyle(ChatFormatting.GREEN).getVisualOrderText());
                    else rulesTooltip.add(rules.get(i).getDisplay().withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                }
                if (end == rules.size() - 1) {
                    if (choosingIndex == end) rulesTooltip.add(rules.get(end).getDisplay().withStyle(ChatFormatting.GREEN).getVisualOrderText());
                    else rulesTooltip.add(rules.get(end).getDisplay().withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                } else rulesTooltip.add(new TextComponent("...").withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
            }
            rulesTooltip.add(new TranslatableComponent("bhs.GUI.rule.tip").getVisualOrderText());
        }

        public InfoRule getChoosingRule() {
            return rules.get(choosingIndex);
        }

        public void applyRule(int ruleRate) {
            if (locked) return;
            if (selectedRules.size() >= 16) return;
            InfoRule rule = rules.get(choosingIndex);
            selectedRules.add(new InfoRule(rule.ruleType, rule.value, ruleRate));
            selectedPort.enable = true;
            if (selectedRules.size() >= 5) scrollAt++;
            dummyContainer.updateItem();
        }

        public void next() {
            if (choosingIndex + 1 >= rules.size()) choosingIndex = 0;
            else choosingIndex++;
            updateDisplay();
        }

        public void last() {
            if (choosingIndex <= 0) choosingIndex = rules.size() - 1;
            else choosingIndex--;
            updateDisplay();
        }
    }

    public class DummyContainer extends SimpleContainer {

        public DummyContainer() {
            super(5);
        }

        public void updateItem() {
            for (int i = 0; i < 5; i++) {
                if (i + scrollAt >= selectedRules.size()) break;
                InfoRule rule = selectedRules.get(i + scrollAt);
                if (rule.ruleType.equals(RuleType.ITEM)) {
                    Item item = Tools.getItem(rule.value);
                    setItem(i, new ItemStack(item));
                } else if (rule.ruleType.equals(RuleType.FORGE_ENERGY)) {
                    setItem(i, new ItemStack(BlackHoleStorage.FORGE_ENERGY.get()));
                } else setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private class FakeSlot extends DummySlot {

        public FakeSlot(Container container, int slotId, int x, int y) {
            super(container, slotId, x, y);
        }

        @Override
        public boolean isActive() {
            return scrollAt + getSlotIndex() < selectedRules.size();
        }


    }
}
