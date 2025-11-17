package com.evolution.mixin;

import com.evolution.trait.Traits;
import com.evolution.trait.selection.EntityTraitManager;
import com.evolution.trait.storage.ITraitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Villager.class)
public abstract class VillagerBreedMixin extends AbstractVillager
{
    public VillagerBreedMixin(final EntityType<? extends AbstractVillager> p_35267_, final Level p_35268_)
    {
        super(p_35267_, p_35268_);
    }

    @Inject(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/npc/Villager;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;", remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onVillagerBreed(final ServerLevel p_150012_, final AgeableMob other, final CallbackInfoReturnable<Villager> cir, double d0, VillagerType type, Villager child)
    {
        final AgeableMob result = EntityTraitManager.onAnimalBreed((Mob & ITraitEntity) (Object) this, (Mob & ITraitEntity) other, (Mob & ITraitEntity) child);
        if (result == null)
        {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "updateSpecialPrices", at = @At("RETURN"))
    private void onUpdateTrades(final CallbackInfo ci)
    {
        if (this instanceof ITraitEntity traitEntity && traitEntity.hasTrait(Traits.generous))
        {
            for (final MerchantOffer offer : getOffers())
            {
                final int level = traitEntity.getTraits().get(Traits.generous).getLevel();

                final int cost = offer.getBaseCostA().getCount();
                if (cost > 1)
                {
                    if (cost < 4)
                    {
                        offer.addToSpecialPriceDiff(-1);
                    }
                    else if (cost < 10)
                    {
                        offer.addToSpecialPriceDiff(level);
                    }
                    else
                    {
                        offer.addToSpecialPriceDiff(level + 1);
                    }
                }
            }
        }
    }
}
