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

  @Override
  public boolean noPlayersCloseWithInteraction(ChunkPos chunkPos, Interaction interaction)
  {
    return playerMap.getPlayers(chunkPos.toLong()).noneMatch(player -> ((IServerPlayer)player).getInteraction(interaction) && euclideanDistanceSquared(chunkPos, player) < 16384.0);
  }

  @Inject(method = "skipPlayer", at = @At("HEAD"), cancellable = true)
  private void skipPlayer(ServerPlayer player, CallbackInfoReturnable<Boolean> cir)
  {
    if(!((IServerPlayer)player).getInteraction(Interaction.CHUNKLOADING))
      cir.setReturnValue(true);
  }
}
