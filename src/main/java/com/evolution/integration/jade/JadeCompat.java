package com.evolution.integration.jade;

import com.evolution.config.CommonConfiguration;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.evolution.trait.type.ITraitType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

import java.util.Map;

@WailaPlugin
public class JadeCompat implements IWailaPlugin
{
    public static void init()
    {

    }

    @Override
    public void register(IWailaCommonRegistration registration)
    {
        //TODO register data providers
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerEntityComponent(new IEntityComponentProvider()
        {
            @Override
            public void appendTooltip(final ITooltip iTooltip, final EntityAccessor entityAccessor, final IPluginConfig iPluginConfig)
            {
                if (CommonConfiguration.config.getCommonConfig().displayTraitsInJadeOrWaila)
                {
                    if (entityAccessor.getRawEntity() instanceof ITraitEntity traitEntity)
                    {
                        for (final Map.Entry<ITraitType, ITrait> entry : traitEntity.getTraits().entrySet())
                        {
                            iTooltip.add(entry.getKey().getDisplayName(entry.getValue()));
                        }
                    }
                }
            }

            final ResourceLocation ID = ResourceLocation.tryParse("evolution:jade");

            @Override
            public ResourceLocation getUid()
            {
                return ID;
            }
        }, Mob.class);
        //TODO register component providers, icon providers, callbacks, and config options here
    }
}
