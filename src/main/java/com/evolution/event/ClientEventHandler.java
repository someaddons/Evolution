package com.evolution.event;

import com.evolution.trait.rendering.MutantLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class ClientEventHandler
{
    @SubscribeEvent
    public static <T extends EntityType<? extends LivingEntity>> void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers.AddLayers event)
    {
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet())
        {
           try
           {
               var renderer = event.getRenderer((EntityType) entry.getValue());
               renderer.addLayer(new MutantLayer(renderer));
           }
           catch (Exception e)
           {
               
           }
        }
    }
}
