package com.evolution;

import com.cupboard.config.CupboardConfig;
import com.evolution.config.CommonConfiguration;
import com.evolution.event.EventHandler;
import com.evolution.network.Network;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.evolution.Evolution.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MOD_ID)
public class Evolution
{
    public static final String                              MOD_ID = "evolution";
    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       Random                              rand   = new Random();

    public Evolution()
    {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (a, b) -> true));
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        EvolutionClient.onInitializeClient(event);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        Network.instance.registerMessages();
    }

    // TODO LIST:
    // Add blacklist tags per trait
    // Move compatible entity types into tags more
    // Recheck all compatible entity types, see if some entities are left out
}
