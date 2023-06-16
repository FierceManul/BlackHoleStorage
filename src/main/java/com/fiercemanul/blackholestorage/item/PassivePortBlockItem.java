package com.fiercemanul.blackholestorage.item;

import com.fiercemanul.blackholestorage.util.BlackHoleBEWLRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class PassivePortBlockItem extends BlockItem {

    public PassivePortBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private final BlockEntityWithoutLevelRenderer render = new BlackHoleBEWLRender(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()
            );

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return render;
            }
        });
    }
}
