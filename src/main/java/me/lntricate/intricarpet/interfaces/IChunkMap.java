package me.lntricate.intricarpet.interfaces;

import me.lntricate.intricarpet.interactions.Interaction;
import net.minecraft.world.level.ChunkPos;

public interface IChunkMap
{
  public boolean noPlayersCloseWithInteraction(ChunkPos chunkPos, Interaction interaction);
}
