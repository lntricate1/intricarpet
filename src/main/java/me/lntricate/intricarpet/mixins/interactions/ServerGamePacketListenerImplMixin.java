package me.lntricate.intricarpet.mixins.interactions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import carpet.CarpetSettings;
import me.lntricate.intricarpet.interactions.Interaction;
import me.lntricate.intricarpet.interfaces.IServerPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{
  @Shadow private ServerPlayer player;

  @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
  private void beforeInteractBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci)
  {
    if(!((IServerPlayer)player).getInteraction(Interaction.UPDATES))
      CarpetSettings.impendingFillSkipUpdates.set(true);
  }

  @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", shift = Shift.AFTER))
  private void afterInteractBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci)
  {
    CarpetSettings.impendingFillSkipUpdates.set(false);
  }

  @Inject(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
  private void beforeInteractItem(ServerboundUseItemPacket packet, CallbackInfo ci)
  {
    if(!((IServerPlayer)player).getInteraction(Interaction.UPDATES))
      CarpetSettings.impendingFillSkipUpdates.set(true);
  }

  @Inject(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", shift = Shift.AFTER))
  private void afterInteractItem(ServerboundUseItemPacket packet, CallbackInfo ci)
  {
    CarpetSettings.impendingFillSkipUpdates.set(false);
  }

  @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;I)V"))
  private void beforeBreakBlock(ServerboundPlayerActionPacket packet, CallbackInfo ci)
  {
    if(!((IServerPlayer)player).getInteraction(Interaction.UPDATES))
      CarpetSettings.impendingFillSkipUpdates.set(true);
  }

  @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;I)V", shift = Shift.AFTER))
  private void afterBreakBlock(ServerboundPlayerActionPacket packet, CallbackInfo ci)
  {
    CarpetSettings.impendingFillSkipUpdates.set(false);
  }
}
