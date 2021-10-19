package intricarpet.mixins.rule.optimizedTNTExtra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intricarpet.helpers.OptimizedExplosion;
import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public class EntityMixin
{
    @Inject(method = "setPos", at = @At("HEAD"))
    private void countEntityMovements(CallbackInfo ci)
    {
        OptimizedExplosion.entitiesTicked ++;
    }
}