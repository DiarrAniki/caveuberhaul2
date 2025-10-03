package diarr.caveuberhaul2.gen;

import diarr.caveuberhaul2.FastNoiseLite;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;

public class RavineGenerator extends NoiseCaveGenerator{
	private static final FastNoiseLite ravineCaveNoise = new FastNoiseLite();
	private static final FastNoiseLite ravinePlacementNoise = new FastNoiseLite();
	private float ravineFreqXZ = 0.005f;
	private float ravineFreqY = 0.0007f;
	private float ravinePlacementFreq = 0.001f;
	private float ravineStartingThreshold = .05f;
	@Override
	public void generate(World world, int baseChunkX, int baseChunkZ, ChunkGeneratorResult result) {
		int seed = (int) world.getRandomSeed();
		maxCaveHeight = world.getHeightBlocks()-1;

		byte[][][] decoratorValues = new byte[16][world.getHeightBlocks()][16]; //0 = do Nothing; 1 = to be mined; 2 = marked for replacement by cave biome blocks

		ravineCaveNoise.SetSeed(seed);
		ravineCaveNoise.SetFrequency(ravineFreqXZ,ravineFreqY);
		ravineCaveNoise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);

		ravinePlacementNoise.SetSeed(seed);
		ravinePlacementNoise.SetFrequency(ravinePlacementFreq,ravinePlacementFreq);
		ravinePlacementNoise.SetNoiseType(FastNoiseLite.NoiseType.Value);

		float[][][] NoiseMapRavineCompound = sampleTunnelNoiseCompound3D(ravineCaveNoise,baseChunkX,baseChunkZ, world.getHeightBlocks(), 128,0,128,ravineFreqXZ,ravineFreqY);
		float[][] RavineActivatorNoise = sampleNoise2D(ravinePlacementNoise,baseChunkX,baseChunkZ,512,256);
		float ravineThres=ravineStartingThreshold;
		for (int y = maxCaveHeight; y > 0; y--) {

			if(y>40+rand.nextInt(3))
			{
				if(y>56+rand.nextInt(3))
				{
					if(y>80+rand.nextInt(3))
					{
						if(y>104+rand.nextInt(3))
						{
							if(y>128+rand.nextInt(3))
							{
								//ravineThres = ravineStartingThreshold * 1f;
								if(y>140+rand.nextInt(3))
								{
									if(y>172+rand.nextInt(3))
									{
										ravineThres = ravineStartingThreshold*2.2f;
									}
									else {
										ravineThres = ravineStartingThreshold * 1.5f;
									}
								}
								else {
									ravineThres = ravineStartingThreshold;
								}
							}
							else {
								ravineThres = ravineStartingThreshold * .75f;
							}
						}
						else
						{
							ravineThres = ravineStartingThreshold*0.45f;
						}
					}
					else
					{
						ravineThres = ravineStartingThreshold*0.3f;
					}
				}
				else
				{
					ravineThres *=0.8f;
				}
			}
			else
			{
				ravineThres *=0.25f;
			}

			for(int x = 0;x<16;x++) {
				for (int z = 0; z < 16; z++) {


					float ravinePlacementValue = MathHelper.clamp(Math.abs(RavineActivatorNoise[x][z]), 0, 1);

					float ravineValue = Math.abs(NoiseMapRavineCompound[x][y][z]) ;
					boolean generateRavines = ravineValue<ravineThres-ravinePlacementValue;
					boolean generateRavinesWalls = ravineValue<ravineThres+wallThickness-ravinePlacementValue;

					if(generateRavinesWalls)
					{
						decoratorValues[x][y][z] = 2;
					}
					if (generateRavines)
					{
						decoratorValues[x][y][z] = 1;
					}

				}
			}
		}
		digBlocks(decoratorValues,maxCaveHeight,result);
	}
}
