package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.ClientChannel;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.network.ControlPanelFilterPack;
import com.fiercemanul.blackholestorage.network.ControlPanelMenuActionPack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import com.fiercemanul.blackholestorage.util.Tools;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@OnlyIn(Dist.CLIENT)
public class ControlPanelScreen extends AbstractContainerScreen<ControlPanelMenu> {

    public static final ResourceLocation GUI_IMG = new ResourceLocation(BlackHoleStorage.MODID, "textures/gui/control_panel.png");
    public int imageWidth = 202;
    public int imageHeight = 249;
    public final String ownerName;
    private Item lastHoveredItem = Items.AIR;
    private int lastCount = 0;
    private String lastFormatCountTemp = "";
    private final ItemScrollBar scrollBar = new ItemScrollBar(193, 6, 4, 152);
    private EditBox shortSearchBox;
    private EditBox longSearchBox;

    public ControlPanelScreen(ControlPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.ownerName = ClientChannelManager.getInstance().getUserName(this.getMenu().owner);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - imageWidth + 4) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        this.addRenderableWidget(new ToggleCraftingButton(this.leftPos + 142, this.topPos + 163));
        this.addRenderableWidget(new ToggleLockButton(this.leftPos + 177, this.topPos + 210));
        this.addRenderableWidget(new SortButton(this.leftPos + 177, this.topPos + 176));
        this.shortSearchBox = new EditBox(this.font, leftPos + 75, topPos + 126, 59, 9, Component.translatable("bhs.GUI.search"));
        this.shortSearchBox.setMaxLength(64);
        this.shortSearchBox.setBordered(false);
        this.shortSearchBox.setVisible(menu.craftingMode);
        this.shortSearchBox.setValue(menu.filter);
        this.longSearchBox = new EditBox(this.font, leftPos + 41, topPos + 163, 77, 9, Component.translatable("bhs.GUI.search"));
        this.longSearchBox.setMaxLength(64);
        this.longSearchBox.setBordered(false);
        this.longSearchBox.setVisible(!menu.craftingMode);
        this.longSearchBox.setValue(menu.filter);
        this.addRenderableWidget(shortSearchBox);
        this.addRenderableWidget(longSearchBox);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.scrollBar.render(poseStack);
        this.renderDummyCount();
        this.renderTooltip(poseStack, mouseX, mouseY);

    }

    @Override
    protected void renderBg(PoseStack stack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_IMG);
        this.blit(stack, this.leftPos, this.topPos, 0, 0, imageWidth, 6);
        if (this.menu.craftingMode) {
            for (int i = 0; i < 6; i++) {
                this.blit(stack, this.leftPos, this.topPos + 6 + i * 17, 0, 6, imageWidth, 17);
            }
            this.blit(stack, this.leftPos, this.topPos + 108, 0, 40, imageWidth, 141);

            Slot helmetSlot = this.menu.slots.get(36);
            if (helmetSlot.isActive() && helmetSlot.getItem().isEmpty()) {
                this.blit(stack, this.leftPos + helmetSlot.x, this.topPos + helmetSlot.y, 0, 199, 16, 16);
            }
            Slot chestplateSlot = this.menu.slots.get(37);
            if (chestplateSlot.isActive() && chestplateSlot.getItem().isEmpty()) {
                this.blit(stack, this.leftPos + chestplateSlot.x, this.topPos + chestplateSlot.y, 16, 199, 16, 16);
            }
            Slot leggingsSlot = this.menu.slots.get(38);
            if (leggingsSlot.isActive() && leggingsSlot.getItem().isEmpty()) {
                this.blit(stack, this.leftPos + leggingsSlot.x, this.topPos + leggingsSlot.y, 32, 199, 16, 16);
            }
            Slot bootsSlot = this.menu.slots.get(39);
            if (bootsSlot.isActive() && bootsSlot.getItem().isEmpty()) {
                this.blit(stack, this.leftPos + bootsSlot.x, this.topPos + bootsSlot.y, 48, 199, 16, 16);
            }
            Slot lhandSlot = this.menu.slots.get(40);
            if (lhandSlot.isActive() && lhandSlot.getItem().isEmpty()) {
                this.blit(stack, this.leftPos + lhandSlot.x, this.topPos + lhandSlot.y, 64, 199, 16, 16);
            }

        } else {
            for (int i = 0; i < 8; i++) {
                this.blit(stack, this.leftPos, this.topPos + 6 + i * 17, 0, 6, imageWidth, 17);
            }
            this.blit(stack, this.leftPos, this.topPos + 142, 0, 40, imageWidth, 17);
            this.blit(stack, this.leftPos, this.topPos + 159, 0, 182, imageWidth, 16);
            this.blit(stack, this.leftPos, this.topPos + 175, 0, 107, imageWidth, 74);

        }
    }

    public void renderDummyCount() {
        for (int i = 0; i < this.menu.dummyContainer.formatCount.size(); i++) {
            Slot slot = this.menu.slots.get(i + 51);
            ItemStack itemStack = slot.getItem();
            String count = this.menu.dummyContainer.formatCount.get(i);

            this.setBlitOffset(100);
            RenderSystem.enableDepthTest();

            float fontSize = 0.5F;
            PoseStack poseStack = new PoseStack();
            poseStack.translate(leftPos + slot.x, topPos + slot.y, 300.0D);
            poseStack.scale(fontSize, fontSize, 1.0F);
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            this.font.drawInBatch(
                    count,
                    (16 - this.font.width(count) * fontSize) / fontSize,
                    (16 - this.font.lineHeight * fontSize) / fontSize,
                    16777215,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    false,
                    0,
                    15728880);
            bufferSource.endBatch();

            this.setBlitOffset(0);
        }
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        if (this.menu.getCarried().isEmpty()) {
            if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                if (hoveredSlot.index >= 51) renderDummyItemTooltip(pPoseStack, this.hoveredSlot.getItem(), pX, pY);
                else this.renderTooltip(pPoseStack, this.hoveredSlot.getItem(), pX, pY);
            } else {
                if (isInsideEditBox(pX, pY)) {
                    List<FormattedCharSequence> list = new ArrayList<>();
                    list.add(Component.translatable("bhs.GUI.search.tip1").getVisualOrderText());
                    list.add(Component.translatable("bhs.GUI.search.tip2").getVisualOrderText());
                    list.add(Component.translatable("bhs.GUI.search.tip3").getVisualOrderText());
                    ControlPanelScreen.this.renderToolTip(pPoseStack, list, pX, pY);
                }
            }
        }
    }

    private void renderDummyItemTooltip(PoseStack pPoseStack, ItemStack pItemStack, int pMouseX, int pMouseY) {
        if (hoveredSlot == null) return;
        ItemStack itemStack = pItemStack.copy();
        List<Component> components = this.getTooltipFromItem(itemStack);
        int count = menu.channel.storageItems.getOrDefault(menu.dummyContainer.viewingItemNames.get(hoveredSlot.index - 51), 0);
        if (!itemStack.getItem().equals(lastHoveredItem)) {
            String formatCount = Tools.DECIMAL_FORMAT.format(count);
            components.add(Component.literal(formatCount));
            this.lastHoveredItem = itemStack.getItem();
            this.lastCount = count;
            this.lastFormatCountTemp = formatCount;
        } else if (count == lastCount) {
            components.add(Component.literal(lastFormatCountTemp));
        } else {
            String formatCount = Tools.DECIMAL_FORMAT.format(count);
            int count2 = count - lastCount;
            String formatCount2 = Tools.DECIMAL_FORMAT.format(count2);
            if (count2 >= 0) formatCount += "  |  +§a" + formatCount2;
            else formatCount += "  |  §c" + formatCount2;
            components.add(Component.literal(formatCount));
            lastCount = count;
            lastFormatCountTemp = formatCount;
        }
        this.renderTooltip(pPoseStack, components, pItemStack.getTooltipImage(), pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(PoseStack stack, int i, int j) {}

    /**
     * 加这一步按钮ToolTip才能渲染多行本文，我不知道为什么，反正他就是可以。
     */
    private void renderToolTip(PoseStack pPoseStack, List<? extends FormattedCharSequence> pTooltips, int pMouseX, int pMouseY) {
        super.renderTooltip(pPoseStack, pTooltips, pMouseX, pMouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (shortSearchBox.isFocused()) shortSearchBox.tick();
        if (longSearchBox.isFocused()) shortSearchBox.tick();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            if (scrollBar.isInside(pMouseX, pMouseY) && scrollBar.canScroll()) {
                scrollBar.scrolling = true;
                scrollBar.onmMouseDraggedOn(pMouseY);
            }
        }
        if (pButton == 1) {
            //短搜索框
            if (pMouseX >= leftPos + 74 && pMouseX <= leftPos + 141 && pMouseY >= topPos + 125 && pMouseY <= topPos + 135) {
                menu.filter = ""; shortSearchBox.setValue(""); longSearchBox.setValue("");
                menu.dummyContainer.refreshContainer(true);
            }
            //长搜索框
            else if (pMouseX >= leftPos + 40 && pMouseX <= leftPos + 124 && pMouseY >= topPos + 162 && pMouseY <= topPos + 172) {
                menu.filter = ""; shortSearchBox.setValue(""); longSearchBox.setValue("");
                menu.dummyContainer.refreshContainer(true);
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (scrollBar.scrolling) {
            scrollBar.onmMouseDraggedOn(pMouseY);
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        scrollBar.scrolling = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (shortSearchBox.isFocused() || longSearchBox.isFocused()){
            if (pKeyCode >= InputConstants.KEY_0 && pKeyCode <= InputConstants.KEY_Z) return true;
        }
        if (pKeyCode == InputConstants.KEY_LSHIFT) menu.LShifting = true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (shortSearchBox.isFocused()) {
            String s = shortSearchBox.getValue().toLowerCase();
            if (!s.equals(menu.filter)) {
                menu.filter = s;
                longSearchBox.setValue(s);
                menu.dummyContainer.refreshContainer(true);
            }
        } else if (longSearchBox.isFocused()) {
            String s = longSearchBox.getValue().toLowerCase();
            if (!s.equals(menu.filter)) {
                menu.filter = s;
                shortSearchBox.setValue(s);
                menu.dummyContainer.refreshContainer(true);
            }
        }
        if (pKeyCode == InputConstants.KEY_LSHIFT) {
            menu.LShifting = false;
            menu.dummyContainer.refreshContainer(true);
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pMouseX >= leftPos + 5 && pMouseX <= leftPos + 197 && pMouseY >= topPos + 5 && pMouseY <= topPos + 6 + (menu.craftingMode ? 119 : 153) && scrollBar.canScroll()) {
            if (pDelta <= 0) scrollBar.setScrolledOn(menu.dummyContainer.onMouseScrolled(false));
            else scrollBar.setScrolledOn(menu.dummyContainer.onMouseScrolled(true));
            return true;
        } else return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double) pX && pMouseX < (double) (pX + pWidth) && pMouseY >= (double) pY && pMouseY < (double) (pY + pHeight);
    }

    private boolean isInsideEditBox(double pMouseX, double pMouseY) {
        if (menu.craftingMode && pMouseX >= leftPos + 74 && pMouseX <= leftPos + 141 && pMouseY >= topPos + 125 && pMouseY <= topPos + 135) return true;
        return !menu.craftingMode && pMouseX >= leftPos + 40 && pMouseX <= leftPos + 124 && pMouseY >= topPos + 162 && pMouseY <= topPos + 172;
    }

    private void toggleLock() {
        if (menu.owner.equals(menu.player.getUUID()) || menu.owner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
            this.menu.locked = !this.menu.locked;
            this.shortSearchBox.setFocus(false);
            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelFilterPack(menu.containerId, menu.filter));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    private void toggleCraftingMode() {
        this.menu.craftingMode = !this.menu.craftingMode;
        this.menu.dummyContainer.refreshContainer(true);
        this.shortSearchBox.setFocus(false);
        this.shortSearchBox.setVisible(menu.craftingMode);
        this.longSearchBox.setFocus(false);
        this.longSearchBox.setVisible(!menu.craftingMode);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
    }

    private void cycleSort() {
        if (InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), InputConstants.KEY_LSHIFT)) {
            menu.reverseSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3);
        } else {
            menu.nextSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
        }
    }

    private String getSortKey(int sortType) {
        return switch (sortType) {
            case ControlPanelMenu.Sort.ID_ASCENDING, ControlPanelMenu.Sort.ID_DESCENDING -> "bhs.GUI.sort.id";
            case ControlPanelMenu.Sort.NAMESPACE_ID_ASCENDING, ControlPanelMenu.Sort.NAMESPACE_ID_DESCENDING -> "bhs.GUI.sort.nid";
            case ControlPanelMenu.Sort.MIRROR_ID_ASCENDING, ControlPanelMenu.Sort.MIRROR_ID_DESCENDING -> "bhs.GUI.sort.mirror_id";
            case ControlPanelMenu.Sort.COUNT_ASCENDING, ControlPanelMenu.Sort.COUNT_DESCENDING -> "bhs.GUI.sort.count";
            default -> "";
        };
    }

    private class ItemScrollBar extends ScrollBar {

        private final int scrollBarTagColor = FastColor.ARGB32.color(255, 77, 73, 77);
        private int lastFilteredItemsSize;

        public ItemScrollBar(int x, int y, int weight, int height) {
            super(x, y, weight, height);
            this.setScrollTagSize();
            this.lastFilteredItemsSize = menu.dummyContainer.sortedItemNames.size();
        }

        public int getHeight() {
            return menu.craftingMode ? 118 : 152;
        }

        public void setScrollTagSize() {
            double v = (double) getHeight() * ((menu.craftingMode ? 7.0D : 9.0D) / Math.ceil(menu.dummyContainer.sortedItemNames.size() / 11.0D));
            this.setScrollTagSize(v);
        }

        @Override
        public void setScrollTagSize(double scrollTagSize) {
            this.scrollTagSize = Math.max(weight, Math.min(getHeight(), scrollTagSize));
        }

        @Override
        public boolean canScroll() {
            return scrollTagSize < getHeight();
        }

        public boolean isInside(double mouseX, double mouseY) {
            int xStar = leftPos + x;
            int yStar = topPos + y;
            int xEnd = xStar + weight;
            int yEnd = yStar + getHeight();
            return mouseX >= xStar && mouseY >= yStar && mouseX <= xEnd && mouseY <= yEnd;
        }

        @Override
        public void onmMouseDraggedOn(double mouseY) {
            if (mouseY <= topPos + y) {
                scrolledOn = 0.0D;
            } else if (mouseY >= topPos + y + getHeight()) {
                scrolledOn = 1.0D;
            } else {
                double v = (mouseY - topPos - y - (scrollTagSize / 2)) / (getHeight() - scrollTagSize);
                setScrolledOn(v);
            }
            menu.dummyContainer.onScrollTo(scrolledOn);
        }

        public void render(PoseStack poseStack) {
            if (menu.dummyContainer.sortedItemNames.size() != lastFilteredItemsSize) {
                setScrollTagSize();
                this.lastFilteredItemsSize = menu.dummyContainer.sortedItemNames.size();
            }
            double v = topPos + y + ((getHeight() - scrollTagSize) * scrolledOn);
            fill(poseStack,
                    leftPos + x,
                    (int) Math.floor(v),
                    leftPos + x + weight,
                    (int) Math.ceil(v + scrollTagSize),
                    scrollBarTagColor);
        }
    }

    private class ToggleCraftingButton extends ImageButton {

        public ToggleCraftingButton(int pX, int pY) {
            super(pX, pY, 16, 8, 80, 199, GUI_IMG, pButton -> {
                toggleCraftingMode();
                scrollBar.setScrollTagSize();
            });
        }

        @Override
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            int uOffset = 80;
            int vOffset = 199;
            if (this.isHoveredOrFocused()) vOffset += 8;
            if (menu.craftingMode) uOffset += 16;
            RenderSystem.enableDepthTest();
            blit(pPoseStack, this.x, this.y, (float) uOffset, (float) vOffset, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }

    private class ToggleLockButton extends ImageButton {

        public ToggleLockButton(int pX, int pY) {
            super(pX, pY, 19, 16, 67, 215, GUI_IMG, pButton -> toggleLock());
        }

        @Override
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            int uOffset = 67;
            int vOffset = 215;
            if (this.isHoveredOrFocused()) vOffset += 16;
            if (menu.locked) uOffset += 19;
            RenderSystem.enableDepthTest();
            blit(pPoseStack, this.x, this.y, (float) uOffset, (float) vOffset, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @Override
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();
            UUID owner = ControlPanelScreen.this.menu.owner;
            UUID user = ControlPanelScreen.this.menu.player.getUUID();
            if (owner.equals(user)) {
                list.add(Component.translatable("bhs.GUI.owner", "§a" + menu.player.getGameProfile().getName()).getVisualOrderText());
            } else if (ControlPanelScreen.this.menu.locked) {
                list.add(Component.translatable("bhs.GUI.owner", "§c" + ownerName).getVisualOrderText());
            } else {
                list.add(Component.translatable("bhs.GUI.owner", ownerName).getVisualOrderText());
            }
            ControlPanelScreen.this.renderToolTip(pPoseStack, list, pMouseX, pMouseY);
        }
    }

    private class SortButton extends ImageButton {

        public SortButton(int pX, int pY) {
            super(pX, pY, 19, 16, 202, 0, GUI_IMG, pButton -> cycleSort());
        }

        @Override
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            int uOffset = 202;
            int vOffset = menu.sortType * 16;
            if (this.isHoveredOrFocused()) uOffset += 19;
            RenderSystem.enableDepthTest();
            blit(pPoseStack, this.x, this.y, (float) uOffset, (float) vOffset, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @Override
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();
            list.add(Component.translatable(getSortKey(menu.sortType)).getVisualOrderText());
            if (menu.sortType % 2 == 0) list.add(Component.translatable("bhs.GUI.sort.ascending").getVisualOrderText());
            else list.add(Component.translatable("bhs.GUI.sort.descending").getVisualOrderText());
            list.add(Component.translatable("bhs.GUI.line").getVisualOrderText());
            list.add(Component.translatable("bhs.GUI.sort.tip1").getVisualOrderText());
            list.add(Component.translatable("bhs.GUI.sort.tip2").getVisualOrderText());
            ControlPanelScreen.this.renderToolTip(pPoseStack, list, pMouseX, pMouseY);
        }
    }

    @Override
    public void onClose() {
        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ControlPanelFilterPack(menu.containerId, menu.filter));
        ((ClientChannel)menu.channel).removeListener();
        super.onClose();
    }
}
