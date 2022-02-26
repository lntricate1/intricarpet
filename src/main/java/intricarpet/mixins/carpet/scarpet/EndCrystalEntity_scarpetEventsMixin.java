package intricarpet.mixins.carpet.scarpet;

import carpet.fakes.EntityInterface;
import carpet.script.EntityEventsGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntity.class)
public abstract class EndCrystalEntity_scarpetEventsMixin extends Entity
{
    public EndCrystalEntity_scarpetEventsMixin(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickCall(CallbackInfo ci)
    {
        // calling extra on_tick because falling blocks do not fall back to super tick call
        ((EntityInterface)this).getEventContainer().onEvent(EntityEventsGroup.Event.ON_TICK);
    }
} 