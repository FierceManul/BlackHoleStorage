package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@OnlyIn(Dist.CLIENT)
public class ControlPanelScreen extends AbstractContainerScreen<ControlPanelMenu> {

    public static final ResourceLocation GUI_IMG = new ResourceLocation(BlackHoleStorage.MODID,"textures/gui/control_panel.png");
    public int imageWidth = 202;
    public int imageHeight = 249;
    private boolean craftingMode = false;
    private UUID owner;

    public ControlPanelScreen(ControlPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    protected void init() {
        super.init();
        this.leftPos = (this.width - imageWidth + 4) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        this.addRenderableWidget(new ToggleCraftingButton(this.leftPos + 142, this.topPos + 163));
        this.addRenderableWidget(new ToggleLockButton(this.leftPos + 177, this.topPos + 210));
    }

    public void containerTick() {
        super.containerTick();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTick, int mouseX, int mouseY) {

        this.craftingMode = this.menu.craftingMode;
        this.owner = this.menu.owner;

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

        fill(stack, 20, 40, 50, 50, FastColor.ARGB32.color(255, 255, 255, 255));

    }

    @Override
    protected void renderLabels(PoseStack stack, int i, int j) {
    }

    private void renderToolTip(PoseStack pPoseStack, List<? extends FormattedCharSequence> pTooltips, int pMouseX, int pMouseY) {
        super.renderTooltip(pPoseStack, pTooltips, pMouseX, pMouseY);
    }

    private void toggleCraftingMode() {
        this.menu.craftingMode = !this.menu.craftingMode;
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
    }

    private class ToggleCraftingButton extends ImageButton {

        public ToggleCraftingButton(int pX, int pY) {
            super(pX, pY, 16, 8, 80, 199, GUI_IMG, pButton -> {
                toggleCraftingMode();
            });
        }

        @Override
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            int uOffset = 80;
            int vOffset = 199;
            if (this.isHoveredOrFocused()) {
                vOffset += 8;
            }
            if (craftingMode) {
                uOffset += 16;
            }
            RenderSystem.enableDepthTest();
            blit(pPoseStack, this.x, this.y, (float)uOffset, (float)vOffset, this.width, this.height, 256, 256);
            if (this.isHovered) {
                this.renderToolTip(pPoseStack, pMouseX, pMouseY);
            }
        }
    }

    private class ToggleLockButton extends ImageButton {

        public ToggleLockButton(int pX, int pY) {
            super(pX, pY, 19, 16, 67, 215, GUI_IMG, pButton -> {
            });
        }

        @Override
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();
            list.add(Component.translatable("bhs.GUI.owner", "test").getVisualOrderText());
            list.add(Component.literal(owner.toString()).getVisualOrderText());
            //TODO: 用UUID获取玩家名字
            ControlPanelScreen.this.renderToolTip(pPoseStack, list, pMouseX, pMouseY);
        }
    }

}
