package com.fiercemanul.blackholestorage.events;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ChannelSelectScreen;
import com.fiercemanul.blackholestorage.gui.ControlPanelScreen;
import com.fiercemanul.blackholestorage.gui.PassivePortScreen;
import com.fiercemanul.blackholestorage.render.BlackHoleBlockRender;
import com.fiercemanul.blackholestorage.render.BlackHoleModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.fiercemanul.blackholestorage.BlackHoleStorage.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers register) {
        register.registerBlockEntityRenderer(BlackHoleStorage.PASSIVE_PORT_BLOCK_ENTITY.get(), BlackHoleBlockRender::new);
        register.registerBlockEntityRenderer(BlackHoleStorage.ACTIVE_PORT_BLOCK_ENTITY.get(), BlackHoleBlockRender::new);
    }

    @SubscribeEvent
    public static void onBakeModel(ModelEvent.BakingCompleted event) {
        ModelResourceLocation location = new ModelResourceLocation(MODID, "passive_port", "inventory");
        event.getModels().put(location, new BlackHoleModel(event.getModels().get(location)));
        location = new ModelResourceLocation(MODID, "active_port", "inventory");
        event.getModels().put(location, new BlackHoleModel(event.getModels().get(location)));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(BlackHoleStorage.CONTROL_PANEL_MENU.get(), ControlPanelScreen::new);
            MenuScreens.register(BlackHoleStorage.CHANNEL_SELECT_MENU.get(), ChannelSelectScreen::new);
            MenuScreens.register(BlackHoleStorage.PASSIVE_PORT_MENU.get(), PassivePortScreen::new);
        });
    }
}
