package intricarpet.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intricarpet.helpers.Interactions;

@Mixin(Entity.class)
public abstract class Entity_InteractionMixin
{
    @Shadow public abstract Text getName();

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void pushAwayFrom(Entity entity, CallbackInfo ci)
    {
        String playerName = "";
        Boolean b1 = true;
        if(entity instanceof PlayerEntity)
            playerName = entity.getName().getString();
        else if(((Object) this) instanceof PlayerEntity)
            playerName = this.getName().getString();
        else b1 = false;
        
        if(b1 && Interactions.onlinePlayerMap.containsKey(playerName) &&
            Interactions.onlinePlayerMap.get(playerName).contains("entities"))
            ci.cancel();
    }
}
