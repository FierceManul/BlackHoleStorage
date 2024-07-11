package com.fiercemanul.blackholestorage.gui;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.network.AddChannelPack;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import com.fiercemanul.blackholestorage.network.RenameChannelPack;
import com.fiercemanul.blackholestorage.network.SetChannelPack;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ChannelSelectScreen extends BaseScreen<ChannelSelectMenu> {

    private static final ResourceLocation GUI_IMG = new ResourceLocation(BlackHoleStorage.MODID, "textures/gui/channel_select.png");
    private EditBox searchBox;
    private EditBox nameBox;
    private ChannelScrollBar scrollBar;
    private final ArrayList<int[]> filterChannels = new ArrayList<>();
    private int scrollAt = 0;
    private final ClientChannelManager channelManager = ClientChannelManager.getInstance();
    private boolean lShifting = false;


    public ChannelSelectScreen(ChannelSelectMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        channelManager.addScreen(this);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - imageWidth + 4) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        this.scrollBar = new ChannelScrollBar(leftPos + 183, topPos + 7, 4, 182);
        this.addRenderableWidget(scrollBar);
        this.addRenderableWidget(new AddChannelButton(leftPos + 92, topPos + 221));
        this.addRenderableWidget(new RenameButton(leftPos + 116, topPos + 222));
        this.addRenderableWidget(new DeleteButton(leftPos + 70, topPos + 222));
        this.addRenderableWidget(new BackButton(leftPos + 175, topPos + 222));
        this.searchBox = new EditBox(this.font, leftPos + 41, topPos + 192, 114, 10, Component.translatable("bhs.GUI.search"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setBordered(false);
        this.addRenderableWidget(searchBox);
        this.nameBox = new EditBox(this.font, leftPos + 41, topPos + 209, 114, 10, Component.translatable("bhs.GUI.name"));
        this.nameBox.setMaxLength(64);
        this.nameBox.setBordered(false);
        this.addRenderableWidget(nameBox);
        for (int i = 0; i < 10; i++) {
            this.addRenderableWidget(new ChannelButton(leftPos + 23, topPos + 9 + i * 18, i));
        }
        this.updateChannelList();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        nameBox.tick();
        searchBox.tick();
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_IMG);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, imageWidth, 98);
        this.blit(poseStack, this.leftPos, this.topPos + 98, 0, 7, imageWidth, 151);
    }

    private void renderToolTip(PoseStack pPoseStack, List<? extends FormattedCharSequence> pTooltips, int pMouseX, int pMouseY) {
        super.renderTooltip(pPoseStack, pTooltips, pMouseX, pMouseY);
    }

    public void updateChannelList() {
        filterChannels.clear();
        ArrayList<int[]> temp = new ArrayList<>();

        channelManager.myChannels.forEach((integer, s) -> {
            if (s.contains(searchBox.getValue())) temp.add(new int[]{0, integer});
        });
        temp.sort((o1, o2) -> channelManager.myChannels.get(o1[1]).compareTo(channelManager.myChannels.get(o2[1])));
        filterChannels.addAll(temp);

        temp.clear();
        channelManager.otherChannels.forEach((integer, s) -> {
            if (s.contains(searchBox.getValue())) temp.add(new int[]{1, integer});
        });
        temp.sort((o1, o2) -> channelManager.otherChannels.get(o1[1]).compareTo(channelManager.otherChannels.get(o2[1])));
        filterChannels.addAll(temp);

        temp.clear();
        channelManager.publicChannels.forEach((integer, s) -> {
            if (s.contains(searchBox.getValue())) temp.add(new int[]{2, integer});
        });
        temp.sort((o1, o2) -> channelManager.publicChannels.get(o1[1]).compareTo(channelManager.publicChannels.get(o2[1])));
        filterChannels.addAll(temp);

        scrollBar.setScrollTagSize( 10.0D / filterChannels.size() * 182);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 1) {
            if (searchBox.isMouseOver(pMouseX, pMouseY)) {
                searchBox.setValue("");
                searchBox.setFocus(true);
                searchBox.setEditable(true);
                updateChannelList();
            }
            if (nameBox.isMouseOver(pMouseX, pMouseY)) {
                nameBox.setValue("");
                nameBox.setFocus(true);
                nameBox.setEditable(true);
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
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
        if (pMouseX >= leftPos + 21 && pMouseX <= leftPos + 187 && pMouseY >= topPos + 7 && pMouseY <= topPos + 189) {
            if (filterChannels.size() <= 10) {
                scrollAt = 0;
                scrollBar.setScrolledOn(0.0D);
            } else {
                int a;
                if (pDelta <= 0) a = scrollAt + 1;
                else a = scrollAt - 1;
                scrollAt = Math.max(0, Math.min(filterChannels.size() - 10, a));
                scrollBar.setScrolledOn((double) scrollAt / (filterChannels.size() - 10));
            }
            return true;
        } else return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (nameBox.isFocused() || searchBox.isFocused()) {
            if (pKeyCode >= InputConstants.KEY_0 && pKeyCode <= InputConstants.KEY_Z) return true;
        }
        if (pKeyCode == InputConstants.KEY_LSHIFT) lShifting = true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (searchBox.isFocused()) updateChannelList();
        if (pKeyCode == InputConstants.KEY_LSHIFT) lShifting = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    private class ChannelScrollBar extends SimpleScrollBar {

        public ChannelScrollBar(int x, int y, int weight, int height) {
            super(x, y, weight, height);
        }

        @Override
        public void draggedTo(double scrolledOn) {
            if (filterChannels.size() <= 10) scrollAt = 0;
            else scrollAt = Math.round((float) ( scrolledOn * (filterChannels.size() - 10) ));
        }

        @Override
        public void beforeRender() {
        }
    }

    private class ChannelButton extends ImageButton {

        private final int buttonID;

        public ChannelButton(int pX, int pY, int id) {
            super(pX, pY, 156, 16, 0, 158, GUI_IMG, button -> {
                int[] a = filterChannels.get(id + scrollAt);
                NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new SetChannelPack(menu.containerId, (byte) a[0], a[1]));
            });
            this.buttonID = id;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            this.visible = buttonID + scrollAt < filterChannels.size();
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            int[] a = filterChannels.get(buttonID + scrollAt);
            float vOffset = this.isHoveredOrFocused() ? 174.0F : 158.0F;
            if (a[0] == channelManager.selectedChannelType && a[1] == channelManager.selectedChannelID) vOffset += 32;
            blit(pPoseStack, this.x, this.y, 0.0F, vOffset, this.width, this.height, 256, 256);
            String channelName;
            switch (a[0]) {
                case 0 -> channelName = "§a" + channelManager.myChannels.get(a[1]);
                case 1 -> channelName = "§c" + channelManager.otherChannels.get(a[1]);
                default -> channelName = channelManager.publicChannels.get(a[1]);
            }
            font.draw(pPoseStack, channelName, this.x + 4.0F, this.y + 4.0F, 16777215);
        }
    }

    private class AddChannelButton extends ImageButton {

        public AddChannelButton(int pX, int pY) {
            super(pX, pY, 18, 18, 202, 0, GUI_IMG, pButton -> {
                String channelName = nameBox.getValue();
                if (channelName.equals("")) return;
                NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new AddChannelPack(channelName, lShifting));
            });
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = this.isHoveredOrFocused() ? 220.0F : 202.0F;
            blit(pPoseStack, this.x, this.y, uOffset, 0, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();
            list.add(Component.translatable("bhs.GUI.addChannel.tip1", nameBox.getValue()).getVisualOrderText());
            list.add(Component.translatable("bhs.GUI.addChannel.tip2").getVisualOrderText());
            list.add(Component.translatable("bhs.GUI.addChannel.tip3").getVisualOrderText());
            list.add(Component.translatable("bhs.GUI.addChannel.tip4").getVisualOrderText());
            ChannelSelectScreen.this.renderToolTip(pPoseStack, list, pMouseX, pMouseY);
        }
    }

    private class RenameButton extends ImageButton {

        public RenameButton(int pX, int pY) {
            super(pX, pY, 16, 16, 202, 34, GUI_IMG, pButton -> {
                String name = nameBox.getValue();
                if (name.equals("")) return;
                NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new RenameChannelPack(menu.containerId, name));
            });
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = this.isHoveredOrFocused() ? 218.0F : 202.0F;
            blit(pPoseStack, this.x, this.y, uOffset, 34, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();
            if (channelManager.selectedChannelName.isEmpty()) {
                list.add(Component.translatable("bhs.GUI.emptyChannel.tip4").getVisualOrderText());
            } else {
                String flag1 = "";
                boolean permissions = true;
                if (channelManager.selectedChannelType == 0) flag1 = "§a";
                    //频道名非空，类型为-1(其实非0和2就行)，代表是其他人设置的频道。
                else if (channelManager.selectedChannelType != 2) {
                    flag1 = "§c";
                    permissions = false;
                }
                list.add(Component.translatable("bhs.GUI.renameChannel.tip1", flag1 + channelManager.selectedChannelName).getVisualOrderText());
                list.add(Component.translatable("bhs.GUI.renameChannel.tip2", flag1 + nameBox.getValue()).getVisualOrderText());
                if (!permissions) list.add(Component.translatable("bhs.GUI.noPermission.tip3").getVisualOrderText());
            }
            ChannelSelectScreen.this.renderToolTip(pPoseStack, list, pMouseX, pMouseY);
        }
    }

    private class DeleteButton extends ImageButton {

        public DeleteButton(int pX, int pY) {
            super(pX, pY, 16, 16, 202, 18, GUI_IMG, pButton ->
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0));
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = this.isHoveredOrFocused() ? 218.0F : 202.0F;
            blit(pPoseStack, this.x, this.y, uOffset, 18, this.width, this.height, 256, 256);
            if (this.isHovered) this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            List<FormattedCharSequence> list = new ArrayList<>();
            if (channelManager.selectedChannelName.isEmpty()) {
                list.add(Component.translatable("bhs.GUI.emptyChannel.tip4").getVisualOrderText());
            } else {
                String flag1 = "";
                boolean permissions = true;
                if (channelManager.selectedChannelType == 0) flag1 = "§a";
                else if (channelManager.selectedChannelType != 2) {
                    flag1 = "§c";
                    permissions = false;
                }
                list.add(Component.translatable("bhs.GUI.removeChannel.tip1", flag1 + channelManager.selectedChannelName).getVisualOrderText());
                list.add(Component.translatable("bhs.GUI.removeChannel.tip2").getVisualOrderText());
                if (!permissions) list.add(Component.translatable("bhs.GUI.noPermission.tip3").getVisualOrderText());
            }
            ChannelSelectScreen.this.renderToolTip(pPoseStack, list, pMouseX, pMouseY);
        }
    }

    private class BackButton extends ImageButton {

        public BackButton(int pX, int pY) {
            super(pX, pY, 16, 16, 202, 50, GUI_IMG, pButton ->
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1));
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GUI_IMG);
            RenderSystem.enableDepthTest();
            float uOffset = this.isHoveredOrFocused() ? 218.0F : 202.0F;
            blit(pPoseStack, this.x, this.y, uOffset, 50, this.width, this.height, 256, 256);
        }
    }
}
