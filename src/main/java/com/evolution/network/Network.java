package com.evolution.network;

import com.evolution.Evolution;
import net.minecraft.server.level.ServerPlayer;

public class Network
{
    public static final Network instance = new Network();


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

    public void sendPacket(final ServerPlayer player, final IMessage msg)
    {
        player.connection.send(msg);
    }
}
