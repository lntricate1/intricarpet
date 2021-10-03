package intricarpet.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import intricarpet.intricarpetRules;
import net.minecraft.block.TntBlock;

@Mixin(TntBlock.class)
public class intricarpetTntBlockMixin
{
    @Inject(method = "onDestroyedByExplosion", at = @At("HEAD"), cancellable = true)
    private void noChainMixin(CallbackInfo ci)
    {
        if(intricarpetRules.disableTNTChainReaction) ci.cancel();
    }
}
