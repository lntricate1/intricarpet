package intricarpet.mixins.interaction;

import org.spongepowered.asm.mixin.Mixin;
import intricarpet.helpers.Interactions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
{
    @Inject(method="isAffectedBySplashPotions", at=@At("HEAD"),cancellable = true)
    public void isAffectedBySplashPotions(CallbackInfoReturnable<Boolean> cir)
    {
        if(((Object) this) instanceof PlayerEntity)
        {
            String playerName = ((PlayerEntity) (Object) this).getName().getString();
            if(Interactions.onlinePlayerMap.containsKey(playerName) &&
                Interactions.onlinePlayerMap.get(playerName).contains("entities")) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
    @Inject(method="isPushable",at=@At("HEAD"),cancellable = true)
    private void isPushableMixin(CallbackInfoReturnable<Boolean> cir) {
        if(((Object) this) instanceof PlayerEntity) {
            String playerName = ((PlayerEntity) (Object) this).getName().getString();
            if(Interactions.onlinePlayerMap.containsKey(playerName) &&
                    Interactions.onlinePlayerMap.get(playerName).contains("entities")) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
