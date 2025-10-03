package diarr.caveuberhaul2.mixin;

import diarr.caveuberhaul2.gen.NoiseCaveGenerator;
import diarr.caveuberhaul2.gen.RavineGenerator;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.LargeFeature;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;
import net.minecraft.core.world.generate.chunk.perlin.ChunkGeneratorPerlin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChunkGeneratorPerlin.class,remap = false)
public class ChunkGeneratorPerlinMixin {

	@Redirect(method = "doBlockGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/generate/LargeFeature;generate(Lnet/minecraft/core/world/World;IILnet/minecraft/core/world/generate/chunk/ChunkGeneratorResult;)V"))
	private void doBlockGeneration(LargeFeature instance, World world, int chunkX, int chunkZ, ChunkGeneratorResult result)
	{
		new NoiseCaveGenerator().generate(world, chunkX, chunkZ, result);
		new RavineGenerator().generate(world,chunkX,chunkZ,result);
		instance.generate(world, chunkX, chunkZ, result);
	}
}
