package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.channel.InfoPort;
import com.fiercemanul.blackholestorage.channel.InfoRule;
import com.fiercemanul.blackholestorage.channel.RuleType;
import com.fiercemanul.blackholestorage.network.ClientPortResultPack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import com.fiercemanul.blackholestorage.render.FluidItemRender;
import com.fiercemanul.blackholestorage.util.Tools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ActivePortScreen extends BaseScreen<ActivePortMenu> {


    private static final ResourceLocation GUI_IMG = new ResourceLocation(BlackHoleStorage.MODID, "textures/gui/active_port.png");
    private final String ownerName = ClientChannelManager.getInstance().getUserName(menu.owner);
    private ChoosingRuleDisplayLabel choosingRuleDisplayLabel;
    private RuleRateBox ruleRateBox;
    private RateBox rateBox;
    private RuleScrollBar scrollBar;
    private int itemRate = 64;
    private int fluidRate = 1000;
    private int feRate = 100000;


    public ActivePortScreen(ActivePortMenu menu, Inventory pPlayerInventory, Component pTitle) {
        super(menu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - imageWidth + 4) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        addRenderableWidget(new PortButton(menu.editingNorthPort, "north", leftPos + 13, topPos + 6, 202, 17));
        addRenderableWidget(new PortButton(menu.editingSouthPort, "south", leftPos + 31, topPos + 6, 219, 17));
        addRenderableWidget(new PortButton(menu.editingWestPort, "west", leftPos + 49, topPos + 6, 202, 34));
        addRenderableWidget(new PortButton(menu.editingEastPort, "east", leftPos + 67, topPos + 6, 219, 34));
        addRenderableWidget(new PortButton(menu.editingDownPort, "down", leftPos + 85, topPos + 6, 202, 51));
        addRenderableWidget(new PortButton(menu.editingUpPort, "up", leftPos + 103, topPos + 6, 219, 51));
        addRenderableWidget(new InOutButton(true, leftPos + 154, topPos + 6, 202, 68));
        addRenderableWidget(new InOutButton(false, leftPos + 172, topPos + 6, 219, 68));
        rateBox = new RateBox(leftPos + 125, topPos + 11, 26, 10, 1200, 4);
        rateBox.setValue(String.valueOf(menu.editingRate));
        rateBox.setBordered(false);
        addRenderableWidget(rateBox);
        addRenderableWidget(new ChannelButton(leftPos + 177, topPos + 176));
        addRenderableWidget(new ToggleLockButton(leftPos + 177, topPos + 193));
        for (int i = 0; i < 5; i++) {
            addRenderableWidget(new RuleDisplayLabel(i, leftPos + 50, topPos + 31 + i * 28, 114, 10));
            addRenderableWidget(new MoveUpButton(i, leftPos + 10, topPos + 33 + i * 28));
            addRenderableWidget(new MoveDownButton(i, leftPos + 10, topPos + 43 + i * 28));
            addRenderableWidget(new DeleteRuleButton(i, leftPos + 168, topPos + 34 + i * 28));
        }
        choosingRuleDisplayLabel = new ChoosingRuleDisplayLabel(leftPos + 50, topPos + 143, 114, 10);
        addRenderableWidget(choosingRuleDisplayLabel);
        ruleRateBox = new RuleRateBox(leftPos + 52, topPos + 157, 110, 10, Integer.MAX_VALUE, 10);
        ruleRateBox.setValue(String.valueOf(feRate));
        ruleRateBox.setBordered(false);
        addRenderableWidget(ruleRateBox);
        addRenderableWidget(new AddRuleButton(leftPos + 168, topPos + 146));
        scrollBar = new RuleScrollBar(leftPos + 188, topPos + 27, 4, 142);
        updateScrollTagSize();
        addRenderableWidget(scrollBar);
        menu.dummyContainer.updateItem();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_IMG);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, 99);
        blit(poseStack, leftPos, topPos + 98, 0, 23, imageWidth, 151);
        for (int i = 0; i < 5; i++) {
            InfoRule rule = menu.getRule(i);
            if (rule != null) {
                RenderSystem.setShaderTexture(0, GUI_IMG);
                blit(poseStack, leftPos + 27, topPos + 29 + i * 28, 0, 174, 140, 26);
                font.draw(poseStack, String.valueOf(rule.rate), leftPos + 51, topPos + 45 + i * 28, 14737632);
            } else break;
        }
        if (menu.checkedSlotActive()) {
            RenderSystem.setShaderTexture(0, GUI_IMG);
            blit(poseStack, leftPos + 27, topPos + 141, 64, 200, 140, 26);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        if (hoveredSlot != null && menu.getCarried().isEmpty() && hoveredSlot.index > 36) {
            if (hoveredSlot.hasItem() && hoveredSlot.getItem().getItem().equals(BlackHoleStorage.FORGE_ENERGY.get())) {
                renderTooltip(pPoseStack, Component.translatable("item.blackholestorage.forge_energy"), pX, pY);
                return;
            } else if (menu.hasRule(hoveredSlot.getContainerSlot()) && menu.getRule(hoveredSlot.getContainerSlot()).ruleType.equals(RuleType.FLUID)) {
                renderTooltip(pPoseStack, Component.translatable("block." + menu.getRule(hoveredSlot.getContainerSlot()).value.replace(':', '.')), pX, pY);
            }
        } else if (choosingRuleDisplayLabel.isHovered()) choosingRuleDisplayLabel.renderToolTip(pPoseStack, pX, pY);
        super.renderTooltip(pPoseStack, pX, pY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        scrollBar.mouseReleased(pMouseX, pMouseY, pButton);
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (scrollBar.isScrolling()) scrollBar.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (choosingRuleDisplayLabel.isHovered()) {
            if (pDelta <= 0) menu.choosingRules.next();
            else menu.choosingRules.last();
        } else if (pMouseX > leftPos + 6 && pMouseX < leftPos + 196
                && pMouseY > topPos + 32 && pMouseY < topPos + 173) {
            if (menu.getSelectedRules().size() < 4) {
                menu.scrollAt = 0;
                scrollBar.setScrolledOn(0.0D);
            } else {
                int a;
                if (pDelta <= 0) a = menu.scrollAt + 1;
                else a = menu.scrollAt - 1;
                menu.scrollAt = Math.max(0, Math.min(menu.getSelectedRules().size() - 4, a));
                scrollBar.setScrolledOn((double) menu.scrollAt / (menu.getSelectedRules().size() - 4));
            }
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = leftPos;
        int j = topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double) pX && pMouseX < (double) (pX + pWidth) && pMouseY >= (double) pY && pMouseY < (double) (pY + pHeight);
    }

    private void updateScrollTagSize() {
        scrollBar.setScrollTagSize(5.0D / (menu.getSelectedRules().size() + 1) * 142);
    }

    @Override
    public void onClose() {
        super.onClose();
        int rate = rateBox.getValue().equals("") ? 20 : Integer.parseInt(rateBox.getValue());
        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ClientPortResultPack(
                menu.blockPos,
                menu.editingNorthPort,
                menu.editingSouthPort,
                menu.editingWestPort,
                menu.editingEastPort,
                menu.editingDownPort,
                menu.editingUpPort,
                rate));
    }

    public void jeiGhostItemRule(ItemStack itemStack) {
        menu.checkerContainer.makeRules(itemStack);
    }

    private class PortButton extends ImageButton {

        private final InfoPort port;
        private final float tx;
        private final float ty;
        private final List<FormattedCharSequence> portTip;
        private static final FormattedCharSequence tip = Component.translatable("bhs.GUI.port.tip").getVisualOrderText();

        public PortButton(InfoPort port, String name, int pX, int pY, int pXTexStart, int pYTexStart) {
            super(pX, pY, 17, 17, pXTexStart, pYTexStart, GUI_IMG, pButton -> {
                if (port == menu.getSelectedPort()) {
                    if (!menu.locked) port.enable = !port.enable;
                } else {
                    menu.setSelectedPort(port);
                    menu.scrollAt = 0;
                    updateScrollTagSize();
                    menu.dummyContainer.updateItem();
                }
            });
            this.port = port;
            this.portTip = new ArrayList<>();
            portTip.add(Component.translatable("bhs.GUI.port." + name).getVisualOrderText());
            this.tx = pXTexStart;
            this.ty = pYTexStart;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = menu.getSelectedPort() == port ? 219.0F : 202.0F;
            blit(pPoseStack, this.getX(), this.getY(), uOffset, 0.0F, this.width, this.height, 256, 256);
            blit(pPoseStack, this.getX(), this.getY(), tx, ty, this.width, this.height, 256, 256);
            if (port.enable) blit(pPoseStack, this.getX(), this.getY(), 236, 0, this.width, this.height, 256, 256);
            if (isHovered) renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            if (port == menu.getSelectedPort()) {
                if (portTip.size() < 2) portTip.add(tip);
            }
            else if (portTip.size() >= 2) portTip.remove(1);
            renderTooltip(pPoseStack, portTip, pMouseX, pMouseY);
        }
    }

    private class InOutButton extends ImageButton {

        private final boolean input;
        private final float tx;
        private final float ty;
        private final MutableComponent component;

        public InOutButton(boolean input, int pX, int pY, int pXTexStart, int pYTexStart) {
            super(pX, pY, 17, 17, pXTexStart, pYTexStart, GUI_IMG, pButton -> {
                menu.setPortInput(input);
                menu.scrollAt = 0;
                updateScrollTagSize();
                menu.checkerContainer.forceMakeRules();
                menu.dummyContainer.updateItem();
            });
            this.input = input;
            this.component = input ? Component.translatable("bhs.GUI.port.input") : Component.translatable("bhs.GUI.port.output");
            this.tx = pXTexStart;
            this.ty = pYTexStart;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = menu.isPortInput() == input ? 219.0F : 202.0F;
            blit(pPoseStack, this.getX(), this.getY(), uOffset, 0.0F, this.width, this.height, 256, 256);
            blit(pPoseStack, this.getX(), this.getY(), tx, ty, this.width, this.height, 256, 256);
            if (isHovered) renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            renderTooltip(pPoseStack, component, pMouseX, pMouseY);
        }
    }

    private class RateBox extends NumberBox {

        public RateBox(int pX, int pY, int pWidth, int pHeight, int maxValue, int maxLength) {
            super(font, pX, pY, pWidth, pHeight, maxValue, maxLength);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            super.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
            if (isHovered) renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            renderTooltip(pPoseStack, Component.translatable("bhs.GUI.rate.tip"), pMouseX, pMouseY);
        }
    }

    private class ToggleLockButton extends ImageButton {

        private final MutableComponent componentA;
        private final MutableComponent componentB;
        private final MutableComponent componentC;

        public ToggleLockButton(int pX, int pY) {
            super(pX, pY, 19, 16, 202, 102, GUI_IMG, pButton -> {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                menu.locked = !menu.locked;
            });
            componentA = Component.translatable("bhs.GUI.owner", "§a" + menu.player.getGameProfile().getName());
            componentB = Component.translatable("bhs.GUI.owner", "§c" + ownerName);
            componentC = Component.translatable("bhs.GUI.owner", ownerName);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = this.isHoveredOrFocused() ? 221.0F : 202.0F;
            float vOffset = menu.locked ? 117.0F : 101.0F;
            blit(pPoseStack, this.getX(), this.getY(), uOffset, vOffset, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            if (menu.owner.equals(menu.player.getUUID())) renderTooltip(pPoseStack, componentA, pMouseX, pMouseY);
            else if (menu.locked) renderTooltip(pPoseStack, componentB, pMouseX, pMouseY);
            else renderTooltip(pPoseStack, componentC, pMouseX, pMouseY);
        }
    }

    private class ChannelButton extends ImageButton {

        private final List<FormattedCharSequence> tips = new ArrayList<>();

        public ChannelButton(int pX, int pY) {
            super(pX, pY, 19, 16, 202, 136, GUI_IMG, pButton ->
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1));
            if (menu.channelOwner.equals(menu.player.getUUID())) {
                tips.add(Component.translatable("bhs.GUI.channel.tip1", "§a" + menu.channelName).getVisualOrderText());
                tips.add(Component.translatable("bhs.GUI.channel.tip2", "§a" + ClientChannelManager.getInstance().getUserName(menu.channelOwner)).getVisualOrderText());
            }
            else if (!menu.channelOwner.equals(BlackHoleStorage.FAKE_PLAYER_UUID)) {
                tips.add(Component.translatable("bhs.GUI.channel.tip1", "§c" + menu.channelName).getVisualOrderText());
                tips.add(Component.translatable("bhs.GUI.channel.tip2", "§c" + ClientChannelManager.getInstance().getUserName(menu.channelOwner)).getVisualOrderText());
            }
            else {
                tips.add(Component.translatable("bhs.GUI.channel.tip1", menu.channelName).getVisualOrderText());
                tips.add(Component.translatable("bhs.GUI.channel.tip2", ClientChannelManager.getInstance().getUserName(menu.channelOwner)).getVisualOrderText());
            }
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = this.isHoveredOrFocused() ? 221.0F : 202.0F;
            blit(pPoseStack, this.getX(), this.getY(), uOffset, 85.0F, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            renderTooltip(pPoseStack, tips, pMouseX, pMouseY);
        }
    }

    private class MoveUpButton extends ImageButton {


        private final int id;

        public MoveUpButton(int id, int pX, int pY) {
            super(pX, pY, 16, 8, 0, 200, GUI_IMG, pButton -> menu.ruleUp(id));
            this.id = id;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            visible = menu.hasRule(id);
            active = menu.hasRule(id);
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    private class MoveDownButton extends ImageButton {


        private final int id;

        public MoveDownButton(int id, int pX, int pY) {
            super(pX, pY, 16, 8, 16, 200, GUI_IMG, pButton -> menu.ruleDown(id));
            this.id = id;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            visible = menu.hasRule(id);
            active = menu.hasRule(id);
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    private class DeleteRuleButton extends ImageButton {


        private final int id;

        public DeleteRuleButton(int id, int pX, int pY) {
            super(pX, pY, 16, 16, 32, 200, GUI_IMG, pButton -> {
                menu.deleteRule(id);
                updateScrollTagSize();
            });
            this.id = id;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            visible = menu.hasRule(id);
            active = menu.hasRule(id);
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    private class AddRuleButton extends ImageButton {

        public AddRuleButton(int pX, int pY) {
            super(pX, pY, 16, 16, 48, 200, GUI_IMG, pButton -> {
                if (!ruleRateBox.getValue().isEmpty()) menu.choosingRules.applyRule(Integer.parseInt(ruleRateBox.getValue()));
                updateScrollTagSize();
            });
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            visible = menu.checkedSlotActive();
            active = menu.checkedSlotActive();
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    private class RuleRateBox extends NumberBox {

        public RuleRateBox(int pX, int pY, int pWidth, int pHeight, int maxValue, int maxLength) {
            super(font, pX, pY, pWidth, pHeight, maxValue, maxLength);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            visible = menu.checkedSlotActive();
            active = menu.checkedSlotActive();
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    private class RuleDisplayLabel extends AbstractWidget {

        private final int id;
        private InfoRule rule;
        private MutableComponent tip;
        private String stringTemp;
        private FluidStack fluidStack;

        public RuleDisplayLabel(int id, int pX, int pY, int pWidth, int pHeight) {
            super(pX, pY, pWidth, pHeight, CommonComponents.EMPTY);
            this.id = id;
            this.rule = menu.getRule(id);
            setString();
        }

        private void setString() {
            if (rule == null) return;
            tip = rule.getDisplay();
            String s = rule.getDisplay().getString();
            if (font.width(s) * 0.9 > width - 2) {
                for (int i = s.length(); i > 0; i--) {
                    s = s.substring(0, s.length() - 1);
                    if (font.width(s) * 0.9 <= width - 2) {
                        s = s.substring(0, s.length() - 3) + "...";
                        break;
                    }
                }
            }
            stringTemp = s;
            if (rule.ruleType.equals(RuleType.FLUID)) {
                fluidStack = new FluidStack(Tools.getFluid(rule.value), 1);
            } else fluidStack = FluidStack.EMPTY;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            visible = menu.hasRule(id);
            active = menu.hasRule(id);
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            if (rule != menu.getRule(id)) {
                this.rule = menu.getRule(id);
                setString();
            }
            if (!fluidStack.equals(FluidStack.EMPTY)) FluidItemRender.renderFluid(fluidStack, pPoseStack, getX() - 19, getY() + 3, 0);
            font.draw(pPoseStack, stringTemp, getX() + 1, getY() + 2, 14737632);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            renderTooltip(pPoseStack, tip, pMouseX, pMouseY);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }
    }

    private class ChoosingRuleDisplayLabel extends AbstractWidget {

        private InfoRule rule;
        private String stringTemp;

        public ChoosingRuleDisplayLabel(int pX, int pY, int pWidth, int pHeight) {
            super(pX, pY, pWidth, pHeight, CommonComponents.EMPTY);
            rule = menu.choosingRules.getChoosingRule();
            setString();
        }

        private void setString() {
            String s = rule.getDisplay().getString();
            if (font.width(s) * 0.9 > width - 2) {
                for (int i = s.length(); i > 0; i--) {
                    s = s.substring(0, s.length() - 1);
                    if (font.width(s) * 0.9 <= width - 2) {
                        s = s.substring(0, s.length() - 3) + "...";
                        break;
                    }
                }
            }
            stringTemp = s;
        }

        public boolean isHovered() {
            return isHovered;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            visible = menu.checkedSlotActive();
            active = menu.checkedSlotActive();
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            InfoRule thisRule = menu.choosingRules.getChoosingRule();
            if (rule != thisRule) {
                RuleType lastRuleType = rule.ruleType;
                RuleType thisRuleType = thisRule.ruleType;
                if (!lastRuleType.equals(thisRuleType)) {
                    switch (lastRuleType) {
                        case ITEM, ITEM_TAG, MOD_ITEM, ANY_ITEM -> itemRate = Integer.parseInt(ruleRateBox.getValue());
                        case FLUID, MOD_FLUID, ANY_FLUID -> fluidRate = Integer.parseInt(ruleRateBox.getValue());
                        case FORGE_ENERGY -> feRate = Integer.parseInt(ruleRateBox.getValue());
                    }
                    switch (thisRuleType) {
                        case ITEM, ITEM_TAG, MOD_ITEM, ANY_ITEM -> ruleRateBox.setValue(String.valueOf(itemRate));
                        case FLUID, MOD_FLUID, ANY_FLUID -> ruleRateBox.setValue(String.valueOf(fluidRate));
                        case FORGE_ENERGY -> ruleRateBox.setValue(String.valueOf(feRate));
                    }
                }
                rule = thisRule;
                setString();
            }
            font.draw(pPoseStack, stringTemp, getX() + 1, getY() + 2, 14737632);
        }

        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            renderTooltip(pPoseStack, menu.choosingRules.rulesTooltip, pMouseX, pMouseY);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }
    }

    private class RuleScrollBar extends SimpleScrollBar {


        public RuleScrollBar(int x, int y, int weight, int height) {
            super(x, y, weight, height);
        }

        @Override
        public void draggedTo(double scrolledOn) {
            if (menu.getSelectedRules().size() < 5) menu.scrollAt = 0;
            else menu.scrollAt = Math.round((float) (scrolledOn * (menu.getSelectedRules().size() - 4)));
        }

        @Override
        public void beforeRender() {}

        @Override
        @ParametersAreNonnullByDefault
        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }
    }
}