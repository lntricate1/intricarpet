package intricarpet.mixins.rule.instantScheduling;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intricarpet.intricarpetRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TrueTypeFontLoader;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;

@Mixin(ServerTickScheduler.class)
public class ServerTickSchedulerMixin<T>
{
    @Shadow @Final private ServerWorld world;
    @Shadow @Final Predicate<T> invalidObjPredicate;
    @Shadow private void addScheduledTick(ScheduledTick<T> tick){};

    private void tickFluid(ScheduledTick<Fluid> tick) {
        FluidState fluidState = world.getFluidState(tick.pos);
        if (fluidState.getFluid() == tick.getObject()) {
            fluidState.onScheduledTick(world, tick.pos);
        }
    }

    private void tickBlock(ScheduledTick<Block> tick) {
        BlockState blockState = world.getBlockState(tick.pos);
        if (blockState.isOf((Block)tick.getObject())) {
            blockState.scheduledTick(world, tick.pos, world.random);
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
    private void instantSchedule(BlockPos pos, T object, int delay, TickPriority priority, CallbackInfo ci)
    {
        if (!invalidObjPredicate.test(object))
        {
            ScheduledTick tick = new ScheduledTick(pos, object, (long)delay + world.getTime(), priority);
            if(intricarpetRules.instantScheduling.equals("false")) addScheduledTick(tick);
            else
            {
                if(object instanceof Block && !intricarpetRules.instantScheduling.equals("fluids")) tickBlock(tick);
                if(object instanceof Fluid && !intricarpetRules.instantScheduling.equals("blocks")) tickFluid(tick);
            }
        }
        ci.cancel();
    }
}
