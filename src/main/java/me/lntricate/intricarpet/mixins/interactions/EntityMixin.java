package me.lntricate.intricarpet.mixins.interactions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import me.lntricate.intricarpet.interactions.Interaction;
import me.lntricate.intricarpet.interfaces.IServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Entity.class)
public class EntityMixin
{
  private boolean noBlockInteraction()
  {
    return (Entity)(Object)this instanceof IServerPlayer player && !player.getInteraction(Interaction.BLOCKS);
  }

  // TODO: Redstone ore
  // @ModifyReturnValue(method = "isSteppingCarefully", at = @At("RETURN"))
  // private boolean isSteppingCarefully(boolean original)
  // {
  //   if((Entity)(Object)this instanceof IServerPlayer)
  //     System.out.println(original || noBlockInteraction());
  //   return original || noBlockInteraction();
  // }

  @WrapWithCondition(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V"))
  private boolean shouldFallOn(Block instance, Level level, BlockState blockState, BlockPos blockPos, Entity entity, float fallDistance)
  {
    return !noBlockInteraction();
  }

  @Inject(method = "checkInsideBlocks", at = @At("HEAD"), cancellable = true)
  private void checkInsideBlocks(CallbackInfo ci)
  {
    if(noBlockInteraction())
      ci.cancel();
  }

  @ModifyReturnValue(method = "isIgnoringBlockTriggers", at = @At("RETURN"))
  private boolean isIgnoringBlockTriggers(boolean original)
  {
    return original || noBlockInteraction();
  }

  @Inject(method = "teleportToWithTicket", at = @At("HEAD"), cancellable = true)
  private void teleportToWithTicket(double x, double y, double z, CallbackInfo ci)
  {
    if((Entity)(Object)this instanceof IServerPlayer player && !player.getInteraction(Interaction.CHUNKLOADING))
      ci.cancel();
  }
}
