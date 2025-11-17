package com.evolution;

import com.evolution.event.EventHandler;
import com.evolution.network.EntityTraitMessage;
import com.evolution.network.Network;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.evolution.Evolution.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MOD_ID)
public class Evolution
{
    public static final String MOD_ID = "evolution";
    public static final Logger LOGGER = LogManager.getLogger();
    public static       Random rand   = new Random();
    private final IEventBus modEventBus;

    public Evolution(IEventBus modEventBus, ModContainer modContainer)
    {
        this.modEventBus = modEventBus;
        NeoForge.EVENT_BUS.register(EventHandler.class);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::register);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        EvolutionClient.onInitializeClient(event, modEventBus);
    }

    @SubscribeEvent
    public void register(final RegisterPayloadHandlersEvent event)
    {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(EntityTraitMessage.TYPE,
            StreamCodec.of((buf, msg) -> msg.write(buf), byteBuf -> new EntityTraitMessage().read(byteBuf)),
            (msg, context) -> msg.handle(context.player()));
    }

    // TODO LIST:
    // Add blacklist tags per trait
    // Move compatible entity types into tags more
    // Recheck all compatible entity types, see if some entities are left out
}
