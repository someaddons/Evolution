package com.evolution.event;

import com.cupboard.Cupboard;
import com.evolution.Evolution;
import com.evolution.trait.rendering.MutantLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.Map;

public class ClientEventHandler
{
    @SubscribeEvent
    public static <T extends EntityType<? extends LivingEntity>> void onRegisterRenderer(EntityRenderersEvent.AddLayers event)
    {
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet())
        {
            try
            {
                var renderer = event.getRenderer((EntityType) entry.getValue());
                if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer)
                {
                    livingRenderer.addLayer(new MutantLayer(livingRenderer));
                }
            }
            catch (Exception e)
            {
                if (!FMLEnvironment.production)
                {
                    Evolution.LOGGER.info(e);
                }
            }
        }
    }
}
