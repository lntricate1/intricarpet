package intricarpet.mixins.interaction;

import java.util.function.Predicate;

import com.google.common.base.Predicates;

import org.spongepowered.asm.mixin.Mixin;

import intricarpet.helpers.Interactions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPredicates.class)
public class EntityPredicatesMixin
{
    @Inject(method = "canBePushedBy", at = @At("RETURN"))
    private static void canBePushedBy(Entity entity, CallbackInfoReturnable<Predicate<Entity>> cir) {
        if (entity instanceof PlayerEntity) {
            String playerName = entity.getName().getString();
            if (Interactions.onlinePlayerMap.containsKey(playerName) &&
                    Interactions.onlinePlayerMap.get(playerName).contains("entities")) {
                cir.setReturnValue(Predicates.alwaysFalse());
                cir.cancel();
            }
        }
    }
}
