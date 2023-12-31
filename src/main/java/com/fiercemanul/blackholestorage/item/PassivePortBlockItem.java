package com.fiercemanul.blackholestorage.item;

import com.fiercemanul.blackholestorage.render.BlackHoleItemRender;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class PassivePortBlockItem extends BlockItem {

    public PassivePortBlockItem(Block block, Properties properties) {
        super(block, properties.fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return BlackHoleItemRender.getInstance();
            }
        });
    }
}
