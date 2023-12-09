package com.fiercemanul.blackholestorage.item;

import com.fiercemanul.blackholestorage.render.BlackHoleItemRender;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class ActivePortBlockItem extends BlockItem {

    public ActivePortBlockItem(Block block, Properties properties) {
        super(block, properties.fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return BlackHoleItemRender.getInstance();
            }
        });
    }
}
