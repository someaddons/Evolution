package com.evolution.network;

import com.evolution.Evolution;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network
{
    private static final String        PROTOCOL_VERSION = "1";
    private final        SimpleChannel channel;

    public static final Network instance = new Network();

    private Network()
    {
        channel = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Evolution.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            e -> true,
            e -> true
        );
    }

    public void registerMessages()
    {
        channel.registerMessage(1, EntityTraitMessage.class,
            EntityTraitMessage::write,
            p -> {
                final EntityTraitMessage msg = new EntityTraitMessage();
                msg.read(p);
                return msg;
            }, (p, c) -> catchErrorsFor(() -> p.handle(c)));
    }

    private void catchErrorsFor(final Runnable runnable)
    {
        try
        {
            runnable.run();
        }
        catch (Exception e)
        {
            Evolution.LOGGER.warn("error during packet:", e);
        }
    }

    public void sendPacket(final ServerPlayer Player, final IMessage msg)
    {
        channel.send(PacketDistributor.PLAYER.with(() -> Player), msg);
    }
}
