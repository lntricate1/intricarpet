package intricarpet.mixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import intricarpet.intricarpetRules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.StructureWorldAccess;

@Mixin(SlimeEntity.class)
public class intricarpetSlimeEntityMixin
{
    @Inject(method = "canSpawn", at = @At(value = "INVOKE",
    target = "Lnet/minecraft/util/math/ChunkPos;<init>(Lnet/minecraft/util/math/BlockPos;)V"),
    cancellable = true)
    private static void newCanSpawn(EntityType<SlimeEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir)
    {
        switch (intricarpetRules.slimeChunks)
        {
            case "none":
                cir.setReturnValue(false);
            break;
            case "all":
                ChunkPos chunkPos = new ChunkPos(pos);
                // The check is unused, but I'm leaving this here to not change the rng
                boolean bl = ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z, ((StructureWorldAccess)world).getSeed(), 987234911L).nextInt(10) == 0;
                if (random.nextInt(10) == 0 && pos.getY() < 40)
                    cir.setReturnValue(MobEntity.canMobSpawn(type, world, spawnReason, pos, random));
            break;
        }
    }
}
