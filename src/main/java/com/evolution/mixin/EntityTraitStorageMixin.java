package com.evolution.mixin;

import com.evolution.Evolution;
import com.evolution.trait.DefaultTraitData;
import com.evolution.trait.ITrait;
import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import com.evolution.trait.type.ITraitType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Mob.class)
public abstract class EntityTraitStorageMixin implements ITraitEntity
{
    @Unique
    private Map<ITraitType, ITrait> traits = new HashMap<>();

    /**
     * Whether the entity tried to obtain traits yet
     */
    @Unique
    private boolean rolledTraits = false;

    @Unique
    @Override
    public Map<ITraitType, ITrait> getTraits()
    {
        return traits;
    }

    @Unique
    @Override
    public boolean hasTrait(final ITraitType traitType)
    {
        return traits.containsKey(traitType);
    }

    @Unique
    @Override
    public int getTraitLevel(ITraitType traitType)
    {
        return traits.get(traitType).getLevel();
    }

    @Unique
    @Override
    public boolean addTraitType(final ITraitType type)
    {
        if (!traits.containsKey(type))
        {
            traits.put(type, type.createTraitData());
        }
        else if (traits.get(type).getLevel() < type.maxLevel())
        {
            traits.get(type).setLevel(traits.get(type).getLevel() + 1);
        }
        else
        {
            return false;
        }

        type.onAddTo((Mob & ITraitEntity)(Object) this, traits.get(type));
        return true;
    }

    @Unique
    @Override
    public void removeTrait(final ITraitType traitType)
    {
        traits.remove(traitType);
    }

    @Unique
    private int combatSuccess = 0;

    @Unique
    @Override
    public void addCombatSuccess(final int success)
    {
        combatSuccess += success;
    }

    @Unique
    @Override
    public int getCombatSuccess()
    {
        return combatSuccess;
    }

    @Unique
    private long lastPlayerContact = 0;

    @Unique
    @Override
    public void onPlayerContact(final long time)
    {
        if (lastPlayerContact == 0)
        {
            lastPlayerContact = time;
        }
    }

    @Unique
    @Override
    public long getFirstPlayerContact()
    {
        return lastPlayerContact;
    }

    @Unique
    @Override
    public void rolledTraits()
    {
        rolledTraits = true;
    }

    @Unique
    @Override
    public boolean hasRolledTraits()
    {
        return rolledTraits;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveTraits(final CompoundTag tag, final CallbackInfo ci)
    {
        try
        {
            tag.putBoolean("rolledTraits", rolledTraits);
            for (final ITrait trait : traits.values())
            {
                tag.put(trait.getType().getID().toString(), trait.serializeNbt());
            }

            tag.putInt("combatSuccess",combatSuccess);
        }
        catch (Exception e)
        {
            Evolution.LOGGER.warn("Failed to save traits for entity: " + this, e);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readTraits(final CompoundTag tag, final CallbackInfo ci)
    {
        try
        {
            rolledTraits = tag.getBoolean("rolledTraits");
            this.traits = new HashMap<>();
            var traitSet = Traits.readTraits(tag);
            if (traitSet != null)
            {
                for (final ITrait trait : traitSet)
                {
                    this.traits.put(trait.getType(), trait);
                    trait.getType().onAddTo((Mob & ITraitEntity)(Object) this, trait);
                }
            }

            if (tag.contains("combatSuccess"))
            {
                combatSuccess = tag.getInt("combatSuccess");
            }
        }
        catch (Exception e)
        {
            Evolution.LOGGER.warn("Failed to read traits for entity: " + this, e);
        }
    }
}
