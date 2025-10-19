package com.evolution;

import com.evolution.event.ClientEventHandler;
import com.evolution.integration.jade.JadeCompat;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;

public class EvolutionClient
{
    public static void onInitializeClient(final FMLClientSetupEvent event)
    {
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ClientEventHandler.class);
        if (FMLLoader.getLoadingModList().getModFileById("jade") != null)
        {
            JadeCompat.init();
        }
    }
}
