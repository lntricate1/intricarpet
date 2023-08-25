package me.lntricate.intricarpet.logging.logHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import me.lntricate.intricarpet.helpers.ExplosionHelper;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.phys.Vec3;

public class ExplosionLogHelper
{
  private static BaseComponent log;

  public static void onExplosion(Vec3 pos, long tick, boolean affectBlocks)
  {
    if(ExplosionHelper.isEmpty() || ExplosionHelper.isNew(pos, tick))
    {
      long time = System.currentTimeMillis();
      logCompact(time, false);
      ExplosionHelper.registerNewPos(pos, tick, time, affectBlocks);
    }
    else
    {
      log = null;
      ExplosionHelper.incrementCounts(tick);
    }
  }

  public static List<BaseComponent> onLog(List<BaseComponent> messages, String option)
  {
    if(option.equals("compact"))
    {
      if(log != null)
        messages.add(log);
    }
    return messages;
  }

  private static void logCompact(long time, boolean endOfTick)
  {
    if(ExplosionHelper.isEmpty())
      return;

    Vec3 pos = ExplosionHelper.getPos();
    log = Messenger.c(
      "d " + ExplosionHelper.getCountInPos() + "x ",
      Messenger.dblt("l", pos.x, pos.y, pos.z),
      "p  [Tp]", String.format(Locale.ENGLISH, "!/tp %.3f %.3f %.3f", pos.x, pos.y, pos.z),
      ExplosionHelper.getAffectBlocks() ? "m   damage" : "m  no damage",
      "g  (", "d " + (time - ExplosionHelper.getTime()), "g ms)",
      endOfTick ? Messenger.c("g  \n(", "d " + ExplosionHelper.getCountInTick(), "g  total)") : Messenger.c()
    );
  }

  public static void afterEntities()
  {
    if(LoggerRegistry.__explosions)
    {
      logCompact(System.currentTimeMillis(), true);
      LoggerRegistry.getLogger("explosions").log((option) ->
      {
        return onLog(new ArrayList<BaseComponent>(), option).toArray(new BaseComponent[0]);
      });
      log = null;
    }
    ExplosionHelper.clear();
  }
}
