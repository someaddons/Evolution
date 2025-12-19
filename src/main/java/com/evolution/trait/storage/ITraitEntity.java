package com.evolution.trait.storage;

import com.evolution.trait.ITrait;
import com.evolution.trait.type.ITraitType;

import java.util.Map;

public interface ITraitEntity
{
    public Map<ITraitType, ITrait> getTraits();

    boolean hasTrait(ITraitType traitType);

    public int getTraitLevel(ITraitType traitType);

    boolean addTraitType(ITraitType type);

    void removeTrait(ITraitType traitType);

    /**
     * Adds a numeric value to how successful this mob has been, recorded on death or despawn/saving
     * @param success
     */
    public void addCombatSuccess(final int success);

    /**
     * Get the success score
     */
    public int getCombatSuccess();

    /**
     * Records the last contact with a player(target or hit)
     * @param time
     */
    public void onPlayerContact(final long time);

    /**
     * Get the last player contact
     * @return
     */
    public long getFirstPlayerContact();

    /**
     * Marker to check if the entity tried to get traits yet
     */
    public void rolledTraits();
    public boolean hasRolledTraits();
}
