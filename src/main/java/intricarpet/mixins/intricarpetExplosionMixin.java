package intricarpet.mixins;

import net.minecraft.world.explosion.Explosion;
import intricarpet.logging.logHelpers.intricarpetExplosionLogHelper;
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

@Mixin(Explosion.class)
public abstract class intricarpetExplosionMixin
{
    @Shadow
    @Final
    private List<BlockPos> affectedBlocks;

    @Shadow @Final private World world;

    private intricarpetExplosionLogHelper logger;

    @Inject(method = "affectWorld", at = @At("HEAD"),
            cancellable = true)
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
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V",
            at = @At(value = "RETURN"))
    private void onExplosionCreated(World world, Entity entity, DamageSource damageSource, ExplosionBehavior explosionBehavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, CallbackInfo ci)
    {
        if (LoggerRegistry.__explosions && ! world.isClient)
        {
            logger = new intricarpetExplosionLogHelper(entity, x, y, z, power, createFire, destructionType);
        }
    }
}