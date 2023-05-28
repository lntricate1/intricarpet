package me.lntricate.intricarpet.helpers;

import net.minecraft.world.phys.Vec3;

public class ExplosionHelper
{
  private static Vec3 pos = null;
  private static int countInPos = 0;
  private static int countInTick = 0;
  private static long tick = 0;
  private static long time = 0;
  private static boolean affectBlocks;

  public static Vec3 getPos(){return pos;}
  public static int getCountInPos(){return countInPos;}
  public static int getCountInTick(){return countInTick;}
  public static long getTick(){return tick;}
  public static long getTime(){return time;}
  public static boolean getAffectBlocks(){return affectBlocks;}

  public static boolean isNew(Vec3 pos_, long tick_)
  {
    return tick != tick_ || !pos.equals(pos_);
  }

  public static boolean isEmpty()
  {
    return pos == null;
  }

  public static void registerNewPos(Vec3 pos_, long tick_, long time_, boolean affectBlocks_)
  {
    pos = pos_;
    tick = tick_;
    countInPos = 1;
    countInTick += 1;
    time = time_;
    affectBlocks = affectBlocks_;
  }

  public static void clear()
  {
    pos = null;
    countInTick = 0;
  }

  public static void incrementCounts(long tick)
  {
    countInPos ++;
    countInTick ++;
  }
}
