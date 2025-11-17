package com.evolution.trait.type;

import com.evolution.Evolution;
import com.evolution.trait.ITrait;
import com.evolution.trait.storage.ITraitEntity;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
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

import java.util.UUID;

/**
 * Trait which increase the dropped loot
 */
public class Mutant implements ITraitType
{
    public static final ResourceLocation      ID         = ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID, "mutant");
    final          TagKey<EntityType<?>> compatible     = TagKey.create(Registries.ENTITY_TYPE, ID);
    private static AttributeModifier     BOOST_ALL_TEN = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,"mutant"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    /**
     * Appareance chance
     **/
    private int chance = 1;

    public Mutant()
    {

    }

    @Override
    public void loadFromJson(final JsonElement data)
    {
        // TODO: load data settings from json
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

        for(final Holder<Attribute> attribute: entity.getAttributes().attributes.keySet())
        {
            entity.getAttribute(attribute).addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID,""+ attribute.hashCode()), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
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
}
