package com.evolution.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public class CreeperPotionMixin
{
    /**
     * Limit duration to 2h
     * @param org
     * @return
     */
    @Redirect(method = "spawnLingeringCloud", at = @At(value = "NEW", target = "(Lnet/minecraft/world/effect/MobEffectInstance;)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private MobEffectInstance withDuration(final MobEffectInstance org)
    {
        return new MobEffectInstance(org.getEffect(),
            20 * 60 * 120,
            org.getAmplifier(),
            org.isAmbient(),
            org.isVisible(),
            org.showIcon(),
            null,
            org.getEffect().createFactorData());
    }
}
