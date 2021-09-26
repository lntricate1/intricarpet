package intricarpet.mixins;

import carpet.fakes.EntityInterface;
import carpet.script.EntityEventsGroup;
import intricarpet.script.intricarpetEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class intricarpet_Entity_scarpetEventsMixin implements EntityInterface
{
    @Shadow private Vec3d pos, velocity;

    private final EntityEventsGroup events = new EntityEventsGroup((Entity) (Object)this);

    private Vec3d pos1, motion;

    @Override
    public EntityEventsGroup getEventContainer()
    {
        return events;
    }

    @Inject(method = "setPos", at = @At("HEAD"))
    private void firstPos(CallbackInfo ci)
    {
        pos1 = this.pos;
        motion = this.velocity;
    }

    @Inject(method = "setPos", at = @At("TAIL"))
    private void secondPos(CallbackInfo ci)
    {
        events.onEvent(intricarpetEntityEvents.ON_MOVE, motion, pos1, this.pos);
    }
}