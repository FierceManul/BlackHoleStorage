package com.fiercemanul.blackholestorage.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

public final class BlackHoleModel extends BakedModelWrapper<BakedModel> {
    public BlackHoleModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }
}
