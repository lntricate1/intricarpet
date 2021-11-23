package intricarpet.mixins.rule.optimizedTNTExtra;

import net.minecraft.world.explosion.Explosion;
import intricarpet.intricarpetRules;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.logging.LoggerRegistry;
import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;

@Mixin(value = Explosion.class, priority = 10)
public abstract class ExplosionMixin
{
    @Shadow @Final private List<BlockPos> affectedBlocks;

    @Shadow @Final private World world;

    private ExplosionLogHelper logger;

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("HEAD"), cancellable = true)
    private void doExplosionA(CallbackInfo ci)
    {
        if(intricarpetRules.optimizedTNTExtra)
        {
            intricarpet.helpers.OptimizedExplosion.doExplosionA((Explosion) (Object) this, logger);
            ci.cancel();
            return;
        }
        if (CarpetSettings.optimizedTNT)
        {
            carpet.helpers.OptimizedExplosion.doExplosionA((Explosion) (Object) this, logger);
        }
        ci.cancel();
    }

    @Inject(method = "affectWorld", at = @At("HEAD"), cancellable = true)
    private void onExplosionB(boolean spawnParticles, CallbackInfo ci)
    {
        if (logger != null)
        {
            logger.setAffectBlocks( ! affectedBlocks.isEmpty());
            logger.onExplosionDone(this.world.getTime());
        }
        if (CarpetSettings.explosionNoBlockDamage)
        {
            affectedBlocks.clear();
        }
        if (intricarpetRules.optimizedTNTExtra)
        {
            intricarpet.helpers.OptimizedExplosion.doExplosionB((Explosion) (Object) this, spawnParticles);
            ci.cancel();
            return;
        }
        if (CarpetSettings.optimizedTNT)
        {
            carpet.helpers.OptimizedExplosion.doExplosionB((Explosion) (Object) this, spawnParticles);
        }
        ci.cancel();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V",
            at = @At(value = "RETURN"))
    private void onExplosionCreated(World world, Entity entity, DamageSource damageSource, ExplosionBehavior explosionBehavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, CallbackInfo ci)
    {
        if (LoggerRegistry.__explosions && ! world.isClient)
        {
            logger = new ExplosionLogHelper(entity, x, y, z, power, createFire, destructionType);
        }
    }
}