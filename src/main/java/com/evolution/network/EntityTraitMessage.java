package com.evolution.network;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EntityTraitMessage implements IMessage
{
    public static final CustomPacketPayload.Type<EntityTraitMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID, "traitmessage"));

    private int         entityID = -1;
    private CompoundTag nbt      = null;

    public <T extends LivingEntity & ITraitEntity> EntityTraitMessage(final T entity)
    {
        entityID = entity.getId();
        nbt = new CompoundTag();
        for (final ITrait trait : entity.getTraits().values())
        {
            nbt.put(trait.getType().getID().toString(), trait.serializeNbt());
        }
    }

    public EntityTraitMessage()
    {
        // Deserial
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(entityID);
        buffer.writeNbt(nbt);
    }

    @Override
    public EntityTraitMessage read(final FriendlyByteBuf buffer)
    {
        entityID = buffer.readInt();
        nbt = buffer.readNbt();
        return this;
    }

    @Override
    public void handle(final Player player)
    {
        final Entity entity = player.level().getEntity(entityID);
        if (entity instanceof ITraitEntity traitEntity)
        {
            var traitSet = Traits.readTraits(nbt);
            if (traitSet != null)
            {
                traitEntity.getTraits().clear();
                for (final ITrait trait : traitSet)
                {
                    traitEntity.getTraits().put(trait.getType(), trait);
                }
            }
        }
    }

    @Override
    public ResourceLocation getID()
    {
        return TYPE.id();
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
