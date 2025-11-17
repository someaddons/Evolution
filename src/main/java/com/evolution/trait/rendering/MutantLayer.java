package com.evolution.trait.rendering;

import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class MutantLayer<T extends LivingEntity, M extends EntityModel<T>>
    extends RenderLayer<T, M>
{
    public MutantLayer(RenderLayerParent<T, M> parent)
    {
        super(parent);
    }

    @Override
    public void render(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int p_116972_,
        T entity,
        float p_116974_,
        float p_116975_,
        float p_116976_,
        float p_116977_,
        float p_116978_,
        float p_116979_)
    {

        if (!(entity instanceof ITraitEntity mutant && mutant.hasTrait(Traits.mutant)))
        {
            return;
        }

        float f = (float) entity.tickCount + p_116976_;
        EntityModel<T> entitymodel = this.getParentModel();
        entitymodel.prepareMobModel(entity, p_116974_, p_116975_, p_116976_);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.energySwirl(POWER_LOCATION, this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
        entitymodel.setupAnim(entity, p_116974_, p_116975_, p_116977_, p_116978_, p_116979_);

        int color = ((int) (0.11F * 255) << 24) |   // alpha
            ((int) (1.0F * 255) << 16) |   // red
            ((int) (0.2F * 255) << 8) |   // green
            ((int) (0.1F * 255));          // blue

        entitymodel.renderToBuffer(poseStack, vertexconsumer, p_116972_, OverlayTexture.NO_OVERLAY, color);
    }

    protected float xOffset(float p_116683_)
    {
        return p_116683_ * 0.001F;
    }

    private static final ResourceLocation POWER_LOCATION = ResourceLocation.tryParse("textures/entity/creeper/creeper_armor.png");
}
