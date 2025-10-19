package com.evolution.event;

import com.evolution.Evolution;
import com.evolution.network.EntityTraitMessage;
import com.evolution.network.Network;
import com.evolution.trait.Traits;
import com.evolution.trait.selection.EntityTraitManager;
import com.evolution.trait.storage.ITraitEntity;
import com.evolution.trait.storage.TraitRegionLevelData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    @SubscribeEvent
    public static void onLootTableLoad(@NotNull final LootingLevelEvent event)
    {
        event.setLootingLevel(event.getLootingLevel() + EntityTraitManager.getLootingLevelBonus(event.getEntity()));
    }

    @SubscribeEvent
    public static void onEntitySpawn(@NotNull final EntityJoinLevelEvent event)
    {
        if (!(event.getEntity() instanceof Mob) || event.getEntity().level().isClientSide() || event.loadedFromDisk() || !(event.getEntity() instanceof ITraitEntity))
        {
            return;
        }

        EntityTraitManager.onEntityAdd((Mob) event.getEntity());
    }

    private static long lastTime = 0;

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event)
    {
        if (event.getServer().getTickCount() % 100 == 35)
        {
            if (lastTime == 0)
            {
                lastTime = Calendar.getInstance().getTimeInMillis();
                return;
            }

            if (event.getServer().getPlayerCount() > 0 && (Calendar.getInstance().getTimeInMillis() - lastTime) > 60 * 5 * 1000)
            {
                lastTime = Calendar.getInstance().getTimeInMillis();

                for (final ServerLevel level : event.getServer().getAllLevels())
                {
                    final TraitRegionLevelData data = TraitRegionLevelData.getTraitRegionLevelData(level);
                    if (data != null)
                    {
                        data.increaseTime(60 * 5);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMobAttackLiving(final LivingAttackEvent event)
    {
        if (event.getEntity().level().isClientSide())
        {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer && event.getSource().getEntity() instanceof ITraitEntity traitEntity)
        {
            traitEntity.addCombatSuccess(1);
            traitEntity.onPlayerContact(event.getEntity().tickCount);
        }
    }

    @SubscribeEvent
    public static void onMobAttacking(final LivingHurtEvent event)
    {
        if (event.getSource().getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.damageBoost))
        {
            event.setAmount(event.getAmount() * (1.0f + 0.2f * traitEntity.getTraits().get(Traits.damageBoost).getLevel()));
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.mutant))
        {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0));
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.darkness) && event.getSource().getEntity() instanceof Player)
        {
            ((Player) event.getSource().getEntity()).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.enduring))
        {
            final float maxHealth = event.getEntity().getMaxHealth();
            final float allowedPercent = 0.45f - traitEntity.getTraits().get(Traits.enduring).getLevel() * 0.1f; // 35% 25% 15%
            final float allowedTotal = allowedPercent * maxHealth;
            event.setAmount(Math.min(allowedTotal, event.getAmount()));
        }
    }

    private static TagKey<DamageType> MELEE_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Evolution.MOD_ID, "meleedamagetypes"));

    @SubscribeEvent
    public static void onMobAttacked(final LivingDamageEvent event)
    {
        if (event.getSource().getDirectEntity() instanceof Projectile
            && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.projectileProtection))
        {
            event.setAmount(event.getAmount() * (1.0f - (0.45f * traitEntity.getTraits().get(Traits.projectileProtection).getLevel())));
        }
        else if (event.getSource().is(MELEE_DAMAGE) && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.meleeProtection))
        {
            event.setAmount(event.getAmount() * (1.0f - (0.25f * traitEntity.getTraits().get(Traits.meleeProtection).getLevel())));
        }

        if (event.getSource().getDirectEntity() == null
            && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.environmentalAdaption))
        {
            event.setAmount(event.getAmount() * (1.0f - (0.5f * traitEntity.getTraits().get(Traits.environmentalAdaption).getLevel())));
        }

        if (event.getSource().getEntity() instanceof LivingEntity
            && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.thorns))
        {
            event.getSource().getEntity().hurt(event.getEntity().level().damageSources().magic(), traitEntity.getTraits().get(Traits.thorns).getLevel());
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && event.getSource().getEntity() instanceof ServerPlayer)
        {
            traitEntity.onPlayerContact(event.getEntity().tickCount);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(final LivingDeathEvent event)
    {
        if (event.getEntity().level().isClientSide())
        {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer)
        {
            if (event.getSource().getEntity() instanceof ITraitEntity traitEntity)
            {
                traitEntity.addCombatSuccess(20);
            }
            return;
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && event.getSource().getEntity() instanceof ServerPlayer)
        {
            traitEntity.onPlayerContact(event.getEntity().tickCount);
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasRolledTraits())
        {
            EntityTraitManager.onEntityDeath((Mob & ITraitEntity) traitEntity, event.getSource());
        }
    }

    @SubscribeEvent
    public static void onTargetEvent(final LivingChangeTargetEvent event)
    {
        if (event.getEntity().level().isClientSide())
        {
            return;
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && event.getNewTarget() instanceof ServerPlayer)
        {
            traitEntity.onPlayerContact(event.getEntity().tickCount);
        }
    }

    @SubscribeEvent
    public static void onTrack(PlayerEvent.StartTracking event)
    {
        final Entity entity = event.getTarget();
        final Player Player = event.getEntity();

        if (Player instanceof ServerPlayer)
        {
            if (entity instanceof ITraitEntity traitEntity && !traitEntity.getTraits().isEmpty())
            {
                Network.instance.sendPacket((ServerPlayer) Player, new EntityTraitMessage((LivingEntity & ITraitEntity) entity));
            }
        }
    }

    private static boolean spawningChild = false;

    @SubscribeEvent
    public static void onChildSpawn(BabyEntitySpawnEvent event)
    {
        if (event.getParentA() instanceof ITraitEntity parentA && event.getParentB() instanceof ITraitEntity parentB && event.getChild() instanceof ITraitEntity
            && !event.getParentA().level().isClientSide())
        {

            event.setChild(EntityTraitManager.onAnimalBreed((Mob & ITraitEntity) event.getParentA(),
                (Mob & ITraitEntity) event.getParentB(),
                (Mob & ITraitEntity) event.getChild()));

            if (!spawningChild)
            {
                spawningChild = true;
                if (parentA.hasTrait(Traits.fertile) || parentB.hasTrait(Traits.fertile))
                {
                    if (event.getParentA() instanceof Animal parentAnimal && event.getParentB() instanceof Animal)
                    {
                        parentAnimal.spawnChildFromBreeding((ServerLevel) parentAnimal.level(), (Animal) event.getParentB());
                    }
                }
                spawningChild = false;
            }
        }
    }

    /**
     * Take over compatible traits on conversion
     *
     * @param event
     */
    @SubscribeEvent
    public static void onEntityConversion(LivingConversionEvent.Post event)
    {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ITraitEntity beforeEntity && event.getOutcome() instanceof ITraitEntity resulting)
        {
            for (final var entry : beforeEntity.getTraits().entrySet())
            {
                if (entry.getKey().isCompatibleEntityType(event.getOutcome().getType(), event.getEntity().level()))
                {
                    resulting.getTraits().put(entry.getKey(), entry.getValue());
                }
            }

            resulting.hasRolledTraits();
        }
    }
}
