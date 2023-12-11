package com.fiercemanul.blackholestorage.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BlackHoleItemRender extends BlockEntityWithoutLevelRenderer {
    private static volatile BlackHoleItemRender instance;

    public static BlackHoleItemRender getInstance() {
        if (instance == null) {
            synchronized (BlackHoleItemRender.class) {
                if (instance == null) instance = new BlackHoleItemRender();
            }
        }
        return instance;
    }

    private BlackHoleItemRender() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        instance = this;
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
        Block block = ((BlockItem) itemStack.getItem()).getBlock();
        BlockState state = block.defaultBlockState();
        if (itemStack.getOrCreateTag().contains("BlockStateTag")) {
            CompoundTag stateTag = itemStack.getTag().getCompound("BlockStateTag");
            //在nbt里的方块状态，布尔是字符串形式。
            state = state.setValue(BlockStateProperties.NORTH, stateTag.getString("north").equals("true"))
                    .setValue(BlockStateProperties.SOUTH, stateTag.getString("south").equals("true"))
                    .setValue(BlockStateProperties.EAST, stateTag.getString("east").equals("true"))
                    .setValue(BlockStateProperties.WEST, stateTag.getString("west").equals("true"))
                    .setValue(BlockStateProperties.UP, stateTag.getString("up").equals("true"))
                    .setValue(BlockStateProperties.DOWN, stateTag.getString("down").equals("true"));
        }
        renderer.renderSingleBlock(state, poseStack, buffer, packedLight, packedOverlay, ModelData.EMPTY, null);

        //BlockStateTag

        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.endGateway());
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.25F);
        renderFace(matrix4f, consumer, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(matrix4f, consumer, 0.25F, 0.25F, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.75F, 0.75F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F);
    }

    private void renderFace(Matrix4f matrix4f, VertexConsumer consumer, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8) {
        consumer.vertex(matrix4f, v1, v3, v5).endVertex();
        consumer.vertex(matrix4f, v2, v3, v6).endVertex();
        consumer.vertex(matrix4f, v2, v4, v7).endVertex();
        consumer.vertex(matrix4f, v1, v4, v8).endVertex();
    }
}
