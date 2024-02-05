package me.lntricate.intricarpet.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import carpet.helpers.OptimizedExplosion;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.mixins.ExplosionAccessor;
import me.lntricate.intricarpet.Rules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

@Mixin(OptimizedExplosion.class)
public class OptimizedExplosionMixin
{
  @Unique private static final double SAME_POSITION_VELOCITY = 0.9923437498509884;

  @Inject(method = "doExplosionA", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D", ordinal = 1, shift = Shift.BY, by = 3), locals = LocalCapture.CAPTURE_FAILHARD)
  private static void onSamePosition(Explosion e, ExplosionLogHelper eLogger, CallbackInfo ci, ExplosionAccessor eAccess, boolean eventNeeded, float f3, int k1, int l1, int i2, int i1, int j2, int j1, Vec3 vec3d, Entity explodingEntity, int k2, Entity entity)
  {
    if(Rules.optimizedTNTEdgeCases || !entity.isOnGround())
    {
      Vec3 vel = entity.getDeltaMovement();
      entity.setDeltaMovement(vel.x, vel.y - SAME_POSITION_VELOCITY, vel.z);
    }
  }
}
