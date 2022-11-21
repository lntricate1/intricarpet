package intricarpet.logging.logHelpers;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.Lists;

import net.minecraft.text.Text;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;

import carpet.utils.Messenger;

public class ExplosionLogHelper
{
  private static Vec3d previousPosition = null;
  private static int explosionCountInCurrentPos = 0;
  private static long startTime = 0;

  private static boolean affectBlocksCopy = false;
  private static int explosionCountInCurrentGT = 0;

  public static void onExplosion(long lastGametime, long gametime, Vec3d pos, boolean affectBlocks, int explosionCountInCurretGT)
  {
    if(lastGametime != gametime)
    {
      ExplosionLogHelper.explosionCountInCurrentPos = 0;
      ExplosionLogHelper.startTime = System.currentTimeMillis();
      ExplosionLogHelper.previousPosition = pos;
      ExplosionLogHelper.affectBlocksCopy = affectBlocks;
    }
    ExplosionLogHelper.explosionCountInCurrentGT = explosionCountInCurretGT + 1;
    ExplosionLogHelper.explosionCountInCurrentPos ++;
  }

  public static BaseText[] onLog(Text[] messages_, long gametime, String option, Vec3d pos)
  {
    List<BaseText> messages = Lists.newArrayList();
    for(Text text : messages_) if(text instanceof BaseText)
      messages.add((BaseText)text);

    if("compact".equals(option) && !pos.equals(previousPosition)) finishCompactInternal(messages, pos);
    return messages.toArray(new BaseText[0]);
  }

  public static void resetAtEndOfTick()
  {
    ExplosionLogHelper.previousPosition = null;
  }

  public static void finishCompactInternal(List<BaseText> messages, Vec3d pos)
  {
    if(previousPosition != null)
    {
      messages.add((BaseText)Messenger.c("d #" + (explosionCountInCurrentGT - explosionCountInCurrentPos + 1) + " ","gb : ",
        "d " + (explosionCountInCurrentPos - 1) + "x ",
        Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z), (affectBlocksCopy)?"m  damage":"m  no damage",
        "g  (", "d " + (System.currentTimeMillis() - startTime), "g ms)",
        Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y, previousPosition.z)),
        Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y - 1.12, previousPosition.z))));
      ExplosionLogHelper.explosionCountInCurrentPos = 1;
      ExplosionLogHelper.previousPosition = pos;
    }
  }

  public static void finishCompact(List<BaseText> messages)
  {
    if(previousPosition != null)
    {
      messages.add((BaseText)Messenger.c("d #" + (explosionCountInCurrentGT - explosionCountInCurrentPos + 1) + " ","gb : ",
        "d " + explosionCountInCurrentPos + "x ",
        Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z), (affectBlocksCopy)?"m  damage":"m  no damage",
        "g  (", "d " + (System.currentTimeMillis() - startTime), "g ms)",
        Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y, previousPosition.z)),
        Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y - 1.12, previousPosition.z))));
      if(explosionCountInCurrentGT != explosionCountInCurrentPos)
        messages.add((BaseText)Messenger.c("d " + explosionCountInCurrentGT + " Total"));
    }
  }
}
