package intricarpet.mixins.interaction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import intricarpet.helpers.Interactions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.GameRules;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin
{
    @Shadow @Final private ServerWorld world;

    @Inject(method = "doesNotGenerateChunks", at = @At("HEAD"), cancellable = true)
    private void doesNotGenerateChunks(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir)
    {
        String playerName = player.getName().getString();
        cir.setReturnValue(Interactions.onlinePlayerMap.containsKey(playerName) && Interactions.onlinePlayerMap.get(playerName).contains("chunkloading") ||
            (player.isSpectator() && !this.world.getGameRules().getBoolean(GameRules.SPECTATORS_GENERATE_CHUNKS)));
    }
}
