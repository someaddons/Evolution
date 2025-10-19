package com.evolution.mixin;

import com.evolution.trait.Traits;
import com.evolution.trait.storage.ITraitEntity;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class EternalYouthAgeMixin extends PathfinderMob
{
    protected EternalYouthAgeMixin(final EntityType<? extends PathfinderMob> p_21683_, final Level p_21684_)
    {
        super(p_21683_, p_21684_);
    }

    @Shadow public abstract void setBaby(final boolean p_146756_);

    @Shadow protected int age;

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void resetAge(final CallbackInfo ci)
    {
        if (this instanceof ITraitEntity traitEntity && this.tickCount % 10 == 7 && !this.level().isClientSide() && traitEntity.hasTrait(Traits.eternalYouth))
        {
            setBaby(true);
        }
    }
}
