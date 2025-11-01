package diarr.caveuberhaul2.mixin;

import diarr.caveuberhaul2.FastNoiseLite;
import diarr.caveuberhaul2.UberUtil;
import diarr.caveuberhaul2.gen.chunk.TempChunkData;
import diarr.caveuberhaul2.gen.feature.PillarFeature;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCoordinate;
import net.minecraft.core.world.generate.chunk.perlin.overworld.ChunkDecoratorOverworld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = ChunkDecoratorOverworld.class,remap = false)
public class ChunkDecoratorOverworldMixin {

	@Shadow
	private World world;
	@Unique
	private FastNoiseLite smallNoise = new FastNoiseLite();
	@Inject(method = "decorate", at = @At(value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V",shift = At.Shift.AFTER))
	public void decorate(Chunk chunk, CallbackInfo ci)
	{
		//TODO: Add method call for Pillar generation here. This is called AFTER the seed for the rng has been set in decorator
		Random rand = new Random(world.getRandomSeed());
		smallNoise.SetSeed((int) world.getRandomSeed());
		smallNoise.SetFrequency(0.5f,0.5f);
		smallNoise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
		//float[][] smallNoiseValues = UberUtil.sampleNoise2D(smallNoise,chunk.xPosition,chunk.zPosition,0,0);
		byte[][][] decoratorValues = TempChunkData.RetrieveData(world,new ChunkCoordinate(chunk.xPosition,chunk.zPosition));
		for(int c=0;c<75;c++)
		{
			int rx = rand.nextInt(16);
			int ry = rand.nextInt(world.getHeightBlocks());
			int rz = rand.nextInt(16);
			if(decoratorValues[rx][ry][rz] == 3)
			{
				new PillarFeature(3,smallNoise,rx,rz).place(world,rand,chunk.xPosition*16+rx,ry-1,chunk.zPosition*16+rz);
			}
		}
		for(int c=0;c<25;c++)
		{
			int rx = rand.nextInt(16);
			int ry = rand.nextInt(32);
			int rz = rand.nextInt(16);
			if(decoratorValues[rx][ry][rz] == 3)
			{
				new PillarFeature(3,smallNoise,rx,rz).place(world,rand,chunk.xPosition*16+rx,ry-1,chunk.zPosition*16+rz);
			}
		}
	}
}
