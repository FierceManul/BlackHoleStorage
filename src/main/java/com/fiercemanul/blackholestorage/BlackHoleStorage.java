package com.fiercemanul.blackholestorage;

import com.fiercemanul.blackholestorage.block.*;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import com.fiercemanul.blackholestorage.gui.ControlPanelScreen;
import com.fiercemanul.blackholestorage.item.PassivePortBlockItem;
import com.fiercemanul.blackholestorage.model.BlackHoleModelLoader;
import com.mojang.logging.LogUtils;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(BlackHoleStorage.MODID)
public class BlackHoleStorage {

    public static final String MODID = "blackholestorage";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);


    public static final RegistryObject<Block> PASSIVE_PORT = BLOCKS.register(
            "passive_port", PassivePortBlock::new);
    public static final RegistryObject<BlockEntityType<PassivePortBlockEntity>> PASSIVE_PORT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "passive_port", () -> BlockEntityType.Builder.of(PassivePortBlockEntity::new, PASSIVE_PORT.get()).build(null)
    );
    public static final RegistryObject<Item> PASSIVE_PORT_ITEM = ITEMS.register(
            "passive_port", () -> new PassivePortBlockItem(PASSIVE_PORT.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Block> ACTIVE_PORT = BLOCKS.register(
            "active_port", ActivePortBlock::new);
    public static final RegistryObject<BlockEntityType<ActivePortBlockEntity>> ACTIVE_PORT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "active_port", () -> BlockEntityType.Builder.of(ActivePortBlockEntity::new, ACTIVE_PORT.get()).build(null)
    );
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register(
            "active_port", () -> new BlockItem(ACTIVE_PORT.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Block> CONTROL_PANEL = BLOCKS.register(
            "control_panel", ControlPanelBlock::new);
    public static final RegistryObject<BlockEntityType<ControlPanelBlockEntity>> CONTROL_PANEL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "control_panel", () -> BlockEntityType.Builder.of(ControlPanelBlockEntity::new, CONTROL_PANEL.get()).build(null)
    );
    public static final RegistryObject<Item> CONTROL_PANEL_ITEM = ITEMS.register(
            "control_panel", () -> new BlockItem(CONTROL_PANEL.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> PORTABLE_CONTROL_PANEL_ITEM = ITEMS.register(
            "portable_control_panel", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
    );
    public static final RegistryObject<Item> CORE = ITEMS.register(
            "core", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
    );
    public static final RegistryObject<Item> STORAGE_CORE = ITEMS.register(
            "storage_core", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
    );

    public static final RegistryObject<MenuType<ControlPanelMenu>> CONTROL_PANEL_MENU = MENU_TYPE.register(
            "control_panel_menu", () -> IForgeMenuType.create(ControlPanelMenu::new));


    public BlackHoleStorage() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        MENU_TYPE.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }




    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers register) {
            register.registerBlockEntityRenderer(PASSIVE_PORT_BLOCK_ENTITY.get(), PassivePortBlockEntityRender::new);
            register.registerBlockEntityRenderer(ACTIVE_PORT_BLOCK_ENTITY.get(), ActivePortBlockEntityRender::new);
        }

        @SubscribeEvent
        public static void onRegisterGeometryLoadersEvent(ModelEvent.RegisterGeometryLoaders event) {
            event.register(new ResourceLocation(MODID, "blackholemodelloader").getPath(), new BlackHoleModelLoader());
        }

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {

            event.getGenerator().addProvider(event.includeClient(), new BlockStateProvider(event.getGenerator(), MODID, event.getExistingFileHelper()) {
                @Override
                protected void registerStatesAndModels() {

                    BlockModelBuilder modelBuilder = models().getBuilder(PASSIVE_PORT.getId().getPath())
                            //.parent(models().getExistingFile(mcLoc("cube")))
                            .customLoader((blockModelBuilder, helper) -> new CustomLoaderBuilder<BlockModelBuilder>(
                                    new ResourceLocation(MODID, "blackholemodelloader"),
                                    blockModelBuilder,
                                    helper) {
                            }).end();
                    simpleBlockItem(PASSIVE_PORT.get(), modelBuilder);

                    //ItemModelBuilder itemModelBuilder = itemModels().nested();
                }
            });

        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(CONTROL_PANEL_MENU.get(), ControlPanelScreen::new);
            });
        }
    }

}
