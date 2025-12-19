package com.evolution.event;

import com.evolution.Evolution;
import com.evolution.network.EntityTraitMessage;
import com.evolution.network.Network;
import com.evolution.trait.Traits;
import com.evolution.trait.selection.EntityTraitManager;
import com.evolution.trait.storage.ITraitEntity;
import com.evolution.trait.storage.TraitJsonReloadListener;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddReloadListenerEvent event)
    {
        event.addListener(new TraitJsonReloadListener());
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
    public static void onServerTick(final ServerTickEvent.Post event)
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
    public static void onMobAttackLiving(final LivingIncomingDamageEvent event)
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
    public static void onMobAttacking(final LivingDamageEvent.Pre event)
    {
        if (event.getSource().getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.damageBoost))
        {
            event.setNewDamage(event.getNewDamage() * (1.0f + Traits.damageBoost.damageIncreasePerLevel() * traitEntity.getTraitLevel(Traits.damageBoost)));
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.mutant))
        {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0));
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.darkness) && event.getSource().getEntity() instanceof Player)
        {
            ((Player) event.getSource().getEntity()).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Traits.darkness.getDuration(), 0));
        }

        if (event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.enduring))
        {
            final float maxHealth = event.getEntity().getMaxHealth();
            final float allowedPercent = Traits.enduring.getMaxHpLostOnHit(traitEntity.getTraitLevel(Traits.enduring)); // 35% 25% 15%
            final float allowedTotal = allowedPercent * maxHealth;
            event.setNewDamage(Math.min(allowedTotal, event.getNewDamage()));
        }
    }

    private static TagKey<DamageType> MELEE_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID, "meleedamagetypes"));

    @SubscribeEvent
    public static void onMobAttacked(final LivingDamageEvent.Pre event)
    {
        if (event.getSource().getDirectEntity() instanceof Projectile
            && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.projectileProtection))
        {
            event.setNewDamage(event.getNewDamage() * Traits.projectileProtection.getDamageModifier(traitEntity.getTraitLevel(Traits.projectileProtection)));
        }
        else if (event.getSource().is(MELEE_DAMAGE) && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.meleeProtection))
        {
            event.setNewDamage(event.getNewDamage() * Traits.meleeProtection.getDamageModifier(traitEntity.getTraitLevel(Traits.meleeProtection)));
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity)
            && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.environmentalAdaption))
        {
            event.setNewDamage(event.getNewDamage() *  Traits.environmentalAdaption.getMaxEnvHpLostOnHit(traitEntity.getTraitLevel(Traits.environmentalAdaption)));
        }

        if (event.getSource().getEntity() instanceof LivingEntity
            && event.getEntity() instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.thorns))
        {
            event.getSource().getEntity().hurt(event.getEntity().level().damageSources().magic(), Traits.thorns.getDamageForLevel(traitEntity.getTraitLevel(Traits.thorns)));
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

        if (event.getEntity() instanceof ITraitEntity traitEntity && event.getNewAboutToBeSetTarget() instanceof ServerPlayer)
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
