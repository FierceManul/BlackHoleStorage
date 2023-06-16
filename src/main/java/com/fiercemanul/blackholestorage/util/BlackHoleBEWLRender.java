package com.fiercemanul.blackholestorage.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlackHoleBEWLRender extends BlockEntityWithoutLevelRenderer {

    //private static final PassivePortBlockEntity PASSIVE_PORT_BLOCK = new PassivePortBlockEntity(BlockPos.ZERO, PASSIVE_PORT.get().defaultBlockState());

    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final EntityModelSet entityModelSet;

    public BlackHoleBEWLRender(BlockEntityRenderDispatcher dispatcher, EntityModelSet set) {
        super(dispatcher, set);
        this.blockEntityRenderDispatcher = dispatcher;
        this.entityModelSet = set;
    }


    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.endGateway());
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.25F);
        renderFace(matrix4f, consumer, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(matrix4f, consumer, 0.25F, 0.25F, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, 0.25F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.25F, 0.25F, 0.25F, 0.25F, 0.75F, 0.75F);
        renderFace(matrix4f, consumer, 0.25F, 0.75F, 0.75F, 0.75F, 0.75F, 0.75F, 0.25F, 0.25F);
        //BlockEntity PassivePortBlockEntity = new PassivePortBlockEntity(BlockPos.ZERO, null);
        //this.blockEntityRenderDispatcher.renderItem(PassivePortBlockEntity, poseStack, bufferSource, combinedLight, combinedLight);
        this.blockEntityRenderDispatcher.renderItem(new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState()), poseStack, bufferSource, combinedLight, combinedLight);
        //ShieldModel shieldModel = new ShieldModel(this.entityModelSet.bakeLayer(ModelLayers.SHIELD));
        //shieldModel.handle().render(poseStack, consumer, combinedOverlay, combinedLight);

    }

    private void renderFace(Matrix4f matrix4f, VertexConsumer consumer, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8) {
        consumer.vertex(matrix4f, v1, v3, v5).endVertex();
        consumer.vertex(matrix4f, v2, v3, v6).endVertex();
        consumer.vertex(matrix4f, v2, v4, v7).endVertex();
        consumer.vertex(matrix4f, v1, v4, v8).endVertex();
    }




}
