package me.lntricate.intricarpet.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;

@Mixin(Explosion.class)
public interface ExplosionDamageAccessor
{
  @Accessor
  ExplosionDamageCalculator getDamageCalculator();

  @Accessor
  Entity getSource();
}
