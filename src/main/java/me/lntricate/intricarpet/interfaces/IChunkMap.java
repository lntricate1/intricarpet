package me.lntricate.intricarpet.interfaces;

import me.lntricate.intricarpet.interactions.Interaction;
import net.minecraft.world.level.ChunkPos;

public interface IChunkMap
{
  public boolean anyPlayerCloseWithInteraction(ChunkPos chunkPos, Interaction interaction);
}
