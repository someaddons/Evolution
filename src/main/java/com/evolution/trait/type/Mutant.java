package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.ValidIdentifiers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Trait which increase the dropped loot
 */
public class Mutant implements ITraitType
{
    public static final ResourceLocation      ID         = new ResourceLocation(Evolution.MOD_ID, "mutant");
    final          TagKey<EntityType<?>> compatible     = TagKey.create(Registries.ENTITY_TYPE, ID);
    private static AttributeModifier     BOOST_ALL_TEN = new AttributeModifier("evolution_mutant", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

    /**
     * Appareance chance
     **/
    private int         chance    = 1;
    private Set<String> blackList = new HashSet<>();

    public Mutant()
    {

    }

    @Override
    public void loadFromJson(final JsonObject data)
    {
        chance = data.get("weight").getAsInt();
        blackList.clear();
        JsonArray traitblacklist = data.get("traitblacklist").getAsJsonArray();
        for(final JsonElement element: traitblacklist)
        {
            blackList.add(element.getAsString());
        }
    }

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public int getChance()
    {
        return chance;
    }

    @Override
    public boolean isCompatibleEntityType(final EntityType type, final Level level)
    {
        if (type.is(compatible))
        {
            return true;
        }

        final Entity entity = type.create(level);
        if (entity instanceof Enemy || entity instanceof Animal)
        {
            return true;
        }

        return false;
    }

    @Override
    public <T extends Mob & ITraitEntity> void onAddTo(T entity, ITrait iTrait)
    {
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 1, true, true));

        for(final Attribute attribute: entity.getAttributes().attributes.keySet())
        {
            entity.getAttribute(attribute).addTransientModifier(new AttributeModifier(UUID.randomUUID(),"mutant", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    public int maxLevel()
    {
        return 1;
    }

    @Override
    public Component getDisplayName(ITrait trait)
    {
        return Component.translatable("evolution.trait.mutant", ITraitType.levelToString(trait.getLevel()));
    }

    public boolean isBlackListed(final ITraitType iTraitType)
    {
        return blackList.contains(iTraitType.getID().toString());
    }
}
