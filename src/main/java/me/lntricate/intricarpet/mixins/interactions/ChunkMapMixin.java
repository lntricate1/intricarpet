package me.lntricate.intricarpet.mixins.interactions;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.lntricate.intricarpet.interactions.Interaction;
import me.lntricate.intricarpet.interfaces.IChunkMap;
import me.lntricate.intricarpet.interfaces.IServerPlayer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

@Mixin(ChunkMap.class)
public class ChunkMapMixin implements IChunkMap
{
  @Shadow private static double euclideanDistanceSquared(ChunkPos chunkPos, Entity entity){return 0.0;}
  @Shadow @Final private PlayerMap playerMap;

  private boolean playerValid(ServerPlayer player, ChunkPos chunkPos, Interaction interaction)
  {
    return ((IServerPlayer)player).getInteraction(interaction) && euclideanDistanceSquared(chunkPos, player) < 16384d;
  }

  @Override
  public boolean anyPlayerCloseWithInteraction(ChunkPos chunkPos, Interaction interaction)
  {
    //#if MC >= 11800
      //#if MC >= 12002
      //$$ for(ServerPlayer player : playerMap.getAllPlayers())
      //#else
      //$$ for(ServerPlayer player : playerMap.getPlayers(chunkPos.toLong()))
      //#endif
    //$$   if(playerValid(player, chunkPos, interaction))
    //$$     return true;
    //$$ return false;
    //#else
    return playerMap.getPlayers(chunkPos.toLong()).anyMatch(player -> playerValid(player, chunkPos, interaction));
    //#endif
  }

  @Inject(method = "skipPlayer", at = @At("HEAD"), cancellable = true)
  private void skipPlayer(ServerPlayer player, CallbackInfoReturnable<Boolean> cir)
  {
    if(!((IServerPlayer)player).getInteraction(Interaction.CHUNKLOADING))
      cir.setReturnValue(true);
  }
}
