package intricarpet.mixins.interaction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intricarpet.helpers.Interactions;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin
{
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void pushAwayFrom(Entity entity, CallbackInfo ci)
    {
        if(entity instanceof PlayerEntity)
        {
            String playerName = entity.getName().getString();
            if(Interactions.onlinePlayerMap.containsKey(playerName) &&
                Interactions.onlinePlayerMap.get(playerName).contains("entities"))
                ci.cancel();
        }
    }
}
