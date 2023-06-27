package com.fiercemanul.blackholestorage;

import com.fiercemanul.blackholestorage.block.*;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenu;
import com.fiercemanul.blackholestorage.item.ActivePortBlockItem;
import com.fiercemanul.blackholestorage.item.PassivePortBlockItem;
import com.fiercemanul.blackholestorage.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.UUID;

@Mod(BlackHoleStorage.MODID)
public class BlackHoleStorage {

    public static final String MODID = "blackholestorage";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    //public static final GameProfile FAKE_PROFILE = new GameProfile(new UUID(-4684872810181343842L, -8974665844362031049L), "public");
    public static final String FAKE_PLAYER_NAME = "public";
    /**
     * befbfd00-2b41-459e-8373-94f6e0f85037
     */
    public static final UUID FAKE_PLAYER_UUID = new UUID(-4684872810181343842L, -8974665844362031049L);


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
    public static final RegistryObject<Item> ACTIVE_PORT_ITEM = ITEMS.register(
            "active_port", () -> new ActivePortBlockItem(ACTIVE_PORT.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
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

        NetworkHandler.init();
    }
}


//PacketBuffer可以拿来同步menu数据 案例：炸鸭乐事?
