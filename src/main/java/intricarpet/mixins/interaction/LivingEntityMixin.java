package intricarpet.mixins.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import intricarpet.helpers.Interactions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
{
    @Overwrite
    public boolean isAffectedBySplashPotions()
    {
        if(((Object) this) instanceof PlayerEntity)
        {
            String playerName = ((PlayerEntity) (Object) this).getName().getString();
            if(Interactions.onlinePlayerMap.containsKey(playerName) &&
                Interactions.onlinePlayerMap.get(playerName).contains("entities"))
                return false;
        }
        return true;
    }
}
