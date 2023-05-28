package me.lntricate.intricarpet.mixins.interactions;

import me.lntricate.intricarpet.interactions.Interaction;
import me.lntricate.intricarpet.interfaces.IServerPlayer;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IServerPlayer
{
  private Map<Interaction, Boolean> interactions;

  @Inject(method = "<init>", at = @At("TAIL"))
  private void onInit(CallbackInfo ci)
  {
    interactions = Interaction.get(((ServerPlayer)(Object)this).getUUID());
  }

  @Override
  public boolean getInteraction(Interaction key)
  {
    return interactions.get(key);
  }

  @Override
  public Map<Interaction, Boolean> getInteractions()
  {
    return interactions;
  }

  @Override
  public void setInteraction(Interaction key, boolean value)
  {
    interactions.put(key, value);
    Interaction.set(((ServerPlayer)(Object)this).getUUID(), key, value);
  }
}
