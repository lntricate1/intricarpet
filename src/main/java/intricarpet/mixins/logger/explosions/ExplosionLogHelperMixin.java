package intricarpet.mixins.logger.explosions;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.Lists;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.logging.logHelpers.ExplosionLogHelper.EntityChangedStatusWithCount;
import carpet.utils.Messenger;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.text.BaseText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;
import intricarpet.logging.logHelpers.intricarpetExplosionLogHelper;

import static carpet.utils.Messenger.c;

@Mixin(ExplosionLogHelper.class)
public class ExplosionLogHelperMixin
{
  @Shadow @Final private Vec3d pos;
  @Shadow private static int explosionCountInCurretGT;
  @Shadow private static long lastGametime;
  @Shadow private boolean affectBlocks;

  private static Vec3d previousPosition = null;
  private static int explosionCountInCurrentPos = 0;
  private static long startTime = 0;

  private void reset()
  {
    ExplosionLogHelperMixin.explosionCountInCurrentPos = 0;
    ExplosionLogHelperMixin.previousPosition = pos;
    ExplosionLogHelperMixin.startTime = System.currentTimeMillis();
  }

  @Inject(method = "onExplosionDone", at = @At("HEAD"), remap = false)
  private void increment(Long gametime,
    //#if MC >= 11900
    //$$ CallbackInfoReturnable<Text[]> cir
    //#else
    CallbackInfoReturnable<BaseText[]> cir
    //#endif
  )
  {
    if(lastGametime != gametime) this.reset();
  }

  @Inject(method = "onExplosionDone", at = @At("RETURN"), remap = false, cancellable = true)
  private void addCompact(long gametime, String option,
    //#if MC >= 11900
    //$$ List<Text> messages_,
    //$$ CallbackInfoReturnable<Text[]> cir
    //#else
    List<BaseText> messages_,
    CallbackInfoReturnable<BaseText[]> cir
    //#endif
  )
  {
    List<BaseText> messages = Lists.newArrayList();
    for(Text text : cir.getReturnValue())
      if(text instanceof BaseText)
        messages.add((BaseText)text);
    if("compact".equals(option))
    {
      if (previousPosition != null && !pos.equals(previousPosition))
      {
        messages.add((BaseText)Messenger.c("d #" + (explosionCountInCurretGT - explosionCountInCurrentPos) + " ","gb : ",
          "d " + explosionCountInCurrentPos + "x ",
          Messenger.dblt("l", previousPosition.x, previousPosition.y, previousPosition.z), (affectBlocks)?"m  damage":"m  no damage",
          "g  (", "d " + (System.currentTimeMillis() - startTime), "g ms)",
          Messenger.c("p  [Tp]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y, previousPosition.z)),
          Messenger.c("p  [TpEyes]", String.format("!/tp %.3f %.3f %.3f", previousPosition.x, previousPosition.y - 1.12, previousPosition.z))));
        this.reset();
      }
    }
    cir.setReturnValue(messages.toArray(new BaseText[0]));
  }
}
