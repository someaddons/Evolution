package com.evolution;

import com.evolution.event.ClientEventHandler;
import com.evolution.integration.jade.JadeCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLLoader;

public class EvolutionClient
{
    public static void onInitializeClient(final FMLClientSetupEvent event, final IEventBus modEventBus)
    {
        modEventBus.register(ClientEventHandler.class);
        if (FMLLoader.getLoadingModList().getModFileById("jade") != null)
        {
            JadeCompat.init();
        }
    }
}
