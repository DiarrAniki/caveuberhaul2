package diarr.caveuberhaul2.gen;

import diarr.caveuberhaul2.FastNoiseLite;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.LargeFeature;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;

public class NoiseCaveGenerator extends LargeFeature
{
	protected int maxCaveHeight = 128; //determine per chunk
	private int layer1MinY = 96;
	private int layer1MinYTunnels = layer1MinY-16;

	private int layer2MinY = 36;
	private int layer2MinYTunnels = layer2MinY-16;
	private int layer3MinY = 0;
	protected float wallThickness = .1f;
	private float layer1BlobThreshold = .7f;
	private float layer2BlobThreshold = .42f;
	private float layer3BlobThreshold = .55f;
	private float layer1WrigglyThreshold = .07f;
	private float layer2WrigglyThreshold = .05f;
	private float layer3WrigglyThreshold = .06f;
	private float bigWrigglyThreshold = .06f;
	private float layer1BlobFreqXZ = 0.015f;
	private float layer1BlobFreqY = 0.025f;
	private float layer2BlobFreqXZ = 0.021f;
	private float layer2BlobFreqY = 0.045f;
	private float layer3BlobFreqXZ = 0.02f;
	private float layer3BlobFreqY = 0.03f;

	private float wrigglyFreqXZ = 0.027f;
	private float wrigglyFreqY = 0.039f;

	private float wrigglyBigFreqXZ = 0.01f;
	private float wrigglyBigFreqY = 0.02f;
	private float caveClearFreq = 0.001f;
	private static final FastNoiseLite caveDistributionNoise = new FastNoiseLite();
	private static final FastNoiseLite blobNoise = new FastNoiseLite();
	private static final FastNoiseLite wrigglyCaveNoise = new FastNoiseLite();

	private static final FastNoiseLite caveClearNoise = new FastNoiseLite();
	private static final FastNoiseLite pillarNoise = new FastNoiseLite();

	public NoiseCaveGenerator() {

	}

	private void initializeNoise(World world)
	{
		int seed = (int) world.getRandomSeed();
		caveDistributionNoise.SetSeed(seed);
		caveDistributionNoise.SetNoiseType(FastNoiseLite.NoiseType.Value);
		caveDistributionNoise.SetFrequency(0.01f,0.01f);

		blobNoise.SetSeed(seed);
		blobNoise.SetNoiseType(FastNoiseLite.NoiseType.Value);
		blobNoise.SetFrequency(layer1BlobFreqXZ,layer1BlobFreqY);
		blobNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
		blobNoise.SetFractalOctaves(2);
		blobNoise.SetFractalGain(.8f);
		blobNoise.SetFractalLacunarity(3f);

		wrigglyCaveNoise.SetSeed(seed);
		wrigglyCaveNoise.SetNoiseType(FastNoiseLite.NoiseType.Value);
		wrigglyCaveNoise.SetFractalType(FastNoiseLite.FractalType.None);

		caveClearNoise.SetSeed(seed);
		caveClearNoise.SetFrequency(caveClearFreq,caveClearFreq);
		caveClearNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
	}

	public void generate(World world, int baseChunkX, int baseChunkZ, ChunkGeneratorResult result){
		maxCaveHeight = getChunkHeight(world,result);
		initializeNoise(world);

		byte[][][] decoratorValues = new byte[16][world.getHeightBlocks()][16]; //0 = do Nothing; 1 = to be mined; 2 = marked for replacement by cave biome blocks

		float[][][] NoiseMapCompound = sampleBlobNoiseCompound3D(baseChunkX,baseChunkZ,maxCaveHeight);

		wrigglyCaveNoise.SetNoiseType(FastNoiseLite.NoiseType.Value);
		wrigglyCaveNoise.SetFractalType(FastNoiseLite.FractalType.None);

		float[][][] NoiseMapTunnelCompound = sampleTunnelNoiseCompound3D(wrigglyCaveNoise,baseChunkX,baseChunkZ,maxCaveHeight,0,0,0,wrigglyFreqXZ,wrigglyFreqY);
		float[][][] NoiseMapTunnelCompoundOffset = sampleTunnelNoiseCompound3D(wrigglyCaveNoise,baseChunkX,baseChunkZ,maxCaveHeight,32,16,32,wrigglyFreqXZ,wrigglyFreqY);

		wrigglyCaveNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
		wrigglyCaveNoise.SetFractalOctaves(2);
		wrigglyCaveNoise.SetFractalGain(.6f);
		wrigglyCaveNoise.SetFractalLacunarity(2.5f);

		float[][][] NoiseMapBigTunnelCompound = sampleTunnelNoiseCompound3D(wrigglyCaveNoise,baseChunkX,baseChunkZ,maxCaveHeight,0,0,0,wrigglyBigFreqXZ,wrigglyBigFreqY);
		float[][][] NoiseMapBigTunnelCompoundOffset = sampleTunnelNoiseCompound3D(wrigglyCaveNoise,baseChunkX,baseChunkZ,maxCaveHeight,256,64,256,wrigglyBigFreqXZ,wrigglyBigFreqY);

		//float[][][] NoiseMapRavineCompound = sampleTunnelNoiseCompound3D(ravineCaveNoise,baseChunkX,baseChunkZ, maxCaveHeight, 128,0,128,ravineFreqXZ,ravineFreqY);
		//float[][] RavineActivatorNoise = sampleNoise2D(ravinePlacementNoise,baseChunkX,baseChunkZ,512,256);

		float[][] caveClearNoise = sampleNoise2D(caveDistributionNoise,baseChunkX,baseChunkZ,256,128);

		float blobThres = layer1BlobThreshold;
		float wrigglyThres = layer1WrigglyThreshold;
		float bigWrigglyThres = bigWrigglyThreshold;

		for (int y = maxCaveHeight; y > 0; y--) {


			if(y<world.getHeightBlocks()/2+24&&y>=world.getHeightBlocks()/2-8)
			{
				bigWrigglyThres-=(y-(world.getHeightBlocks()/2-8)*0.03125)*0.2f;
			}
			else
			{
				bigWrigglyThres = bigWrigglyThreshold;
			}
			if(y>layer3MinY)
			{
				if(y>layer2MinYTunnels)
				{
					if(y>layer1MinYTunnels)
					{
						wrigglyThres = layer1WrigglyThreshold;
					}
					else {
						wrigglyThres = layer2WrigglyThreshold;
					}
				}
				else {
					wrigglyThres = layer3WrigglyThreshold;
				}
			}

			if(y>layer3MinY)
			{
				if(y>layer2MinY)
				{
					if(y>layer1MinY)
					{
						blobThres = layer1BlobThreshold;
					}
					else {
						blobThres = layer2BlobThreshold;
					}
				}
				else {
					blobThres = layer3BlobThreshold;
				}
			}

			for(int x = 0;x<16;x++) {
				for (int z = 0; z < 16; z++) {


					float caveClearValue = (float) MathHelper.clamp(Math.pow(Math.abs(caveClearNoise[x][z]),2), 0, .3);

					float noiseBlobValue = Math.abs(NoiseMapCompound[x][y][z]);
					float noiseTunnelValue = Math.abs(NoiseMapTunnelCompound[x][y][z]);
					float noiseTunnelValueOffset = Math.abs(NoiseMapTunnelCompoundOffset[x][y][z]);
					float noiseTunnelBigValue = Math.abs(NoiseMapBigTunnelCompound[x][y][z]);
					float noiseTunnelBigValueOffset = Math.abs(NoiseMapBigTunnelCompoundOffset[x][y][z]);

					boolean generateBlobs = noiseBlobValue>blobThres+caveClearValue;
					boolean generateBlobsWall = noiseBlobValue>blobThres-wallThickness+caveClearValue;
					boolean generateTunnels = noiseTunnelValue<wrigglyThres-caveClearValue&&noiseTunnelValueOffset<wrigglyThres-caveClearValue;
					boolean generateTunnelsWalls = noiseTunnelValue<wrigglyThres+wallThickness-caveClearValue&&noiseTunnelValueOffset<wrigglyThres+wallThickness-caveClearValue;
					boolean generateBigTunnels = noiseTunnelBigValue<bigWrigglyThres&&noiseTunnelBigValueOffset<bigWrigglyThres;
					boolean generateBigTunnelsWalls = noiseTunnelBigValue<bigWrigglyThres+wallThickness&&noiseTunnelBigValueOffset<bigWrigglyThres+wallThickness;

					if(generateBlobsWall||generateTunnelsWalls||generateBigTunnelsWalls)
					{
						decoratorValues[x][y][z] = 2;
					}

					if(generateBigTunnels)//if (generateBlobs||generateTunnels||generateBigTunnels)
					{
						decoratorValues[x][y][z] = 1;
					}

				}
			}
		}
		digBlocks(decoratorValues,maxCaveHeight,result);
	}

	protected void digBlocks(byte[][][] decoratorValues,int maxHeight,ChunkGeneratorResult result)
	{
		for(int x = 0;x<16;x++)
		{
			for(int z = 0;z<16;z++)
			{
				for (int y = maxHeight; y > 0; y--) {
					int blockId = result.getBlock(x,y,z);
					if(decoratorValues[x][y][z] == 1&& Blocks.hasTag(blockId, BlockTags.CAVES_CUT_THROUGH)) {
						//TODO: Check for oceans and what blocks can be replaced by caves
						if (y < 10) {
							result.setBlock(x, y, z, 273);
						}
						else {
							result.setBlock(x, y, z, 0);
						}
					}
				}
			}
		}
	}

	private int getChunkHeight(World world,ChunkGeneratorResult result)
	{
		for (int y=world.getHeightBlocks()/2;y<world.getHeightBlocks();y+=2)
		{
			if(result.getBlock(8,y,8)==0)
			{
				return y;
			}
		}
		return world.getHeightBlocks()/2+16;
	}

	private float[][][] sampleBlobNoiseCompound3D(int chunkX,int chunkZ,int maxCaveHeight)
	{
		int maxCaveHeightAdjusted = maxCaveHeight+2;
		float[][][] noiseSampleMap = new float[5][maxCaveHeightAdjusted/2+1][5];
		for(int y = maxCaveHeightAdjusted/2;y>=0;y--)
		{
			int realY = y*2;
			if(y>layer3MinY/2)
			{
				if(y>layer2MinY/2)
				{
					if(y>layer1MinY/2)
					{
						blobNoise.SetFrequency(layer1BlobFreqXZ,layer1BlobFreqY);
					}
					else
					{
						blobNoise.SetFrequency(layer2BlobFreqXZ,layer2BlobFreqY);
					}
					blobNoise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
					blobNoise.SetFractalType(FastNoiseLite.FractalType.None);
				}
				else
				{
					blobNoise.SetFrequency(layer3BlobFreqXZ,layer3BlobFreqY);
					blobNoise.SetNoiseType(FastNoiseLite.NoiseType.Value);
					blobNoise.SetFractalType(FastNoiseLite.FractalType.Ridged);
					blobNoise.SetFractalOctaves(2);
					blobNoise.SetFractalLacunarity(1.3f);
					blobNoise.SetFractalGain(0.6f);
				}
			}

			for(int x = 0;x<5;x++)
			{
				int realX = x*4+chunkX*16;
				for(int z = 0;z<5;z++)
				{
					int realZ = z*4+chunkZ*16;
					noiseSampleMap[x][y][z] =blobNoise.GetNoise(realX, realY, realZ);
				}
			}
		}
		return interpolateNoiseCompound3D(noiseSampleMap,maxCaveHeightAdjusted,true);
	}

	private float[][][] sampleNoiseRaw(FastNoiseLite tunnelNoise,int chunkX,int chunkZ,int maxCaveHeight,int offX,int offY,int offZ,float freqXZ,float freqY)
	{
		int maxCaveHeightAdjusted = maxCaveHeight+2;
		float[][][] noiseSampleMap = new float[16][maxCaveHeightAdjusted+1][16];
		tunnelNoise.SetFrequency(freqXZ,freqY);
		for(int y = maxCaveHeightAdjusted;y>=0;y--)
		{

			for(int x = 0;x<16;x++)
			{
				int realX = x+chunkX*16;
				for(int z = 0;z<16;z++)
				{
					int realZ = z+chunkZ*16;
					noiseSampleMap[x][y][z] =tunnelNoise.GetNoise(realX+offX,y+offY,realZ+offZ);
				}
			}
		}
		return noiseSampleMap;
	}

	protected float[][][] sampleTunnelNoiseCompound3D(FastNoiseLite tunnelNoise, int chunkX, int chunkZ, int maxCaveHeight, int offX, int offY, int offZ, float freqXZ, float freqY)
	{
		int maxCaveHeightAdjusted = maxCaveHeight+2;
		float[][][] noiseSampleMap = new float[5][maxCaveHeightAdjusted/2+1][5];
		tunnelNoise.SetFrequency(freqXZ,freqY);
		for(int y = maxCaveHeightAdjusted/2;y>=0;y--)
		{
			int realY = y*2;

			for(int x = 0;x<5;x++)
			{
				int realX = x*4+chunkX*16;
				for(int z = 0;z<5;z++)
				{
					int realZ = z*4+chunkZ*16;
						noiseSampleMap[x][y][z] =tunnelNoise.GetNoise(realX+offX,realY+offY,realZ+offZ);
				}
			}
		}
		return interpolateNoiseCompound3D(noiseSampleMap,maxCaveHeightAdjusted,false);
	}

	protected float[][] sampleNoise2D(FastNoiseLite noise, int chunkX, int chunkZ, int offX, int offZ)
	{
		float[][] noiseSampleMap = new float[5][5];

		for(int x = 0;x<5;x++)
		{
			int realX = x*4+chunkX*16;
			for(int z = 0;z<5;z++)
			{
				int realZ = z*4+chunkZ*16;
				noiseSampleMap[x][z] =noise.GetNoise(realX+offX,realZ+offZ);
			}
		}

		return interpolateNoiseCompound2D(noiseSampleMap);
	}

	public static float[][] interpolateNoiseCompound2D(float[][] NoiseSamples) {
		//Issues seem to come from the y coordinate just leave it hard coded I guess lol. Also freezing caused by too inefficient code
		float[][] vals = new float[16][16];
		int xzScale = 4;
		float quarter = 0.25f;

		for (int x = 0; x < xzScale; ++x) {
			for (int z = 0; z < xzScale; ++z) {

				float x0y0z0 = NoiseSamples[x][z];
				float x0y0z1 = NoiseSamples[x][z + 1];
				float x1y0z0 = NoiseSamples[x + 1][z];

				// noise values of 4 corners at y=0
				float noiseEndX1 = NoiseSamples[x + 1][z + 1];

				float noiseStartZ = x0y0z0;
				float noiseEndZ = x0y0z1;

				// how much to increment X values, linear interpolation
				float noiseStepX0 = (x1y0z0 - x0y0z0) * quarter;
				float noiseStepX1 = (noiseEndX1 - x0y0z1) * quarter;

				for (int subx = 0; subx < 4; subx++) {
					int localX = subx + x * 4;

					// how much to increment Z values, linear interpolation
					float noiseStepZ = (noiseEndZ - noiseStartZ) * quarter;

					// Y and X already interpolated, just need to interpolate final 4 Z block to get final noise value
					float noiseValue = noiseStartZ;

					for (int subz = 0; subz < 4; subz++) {
						int localZ = subz + z * 4;

						noiseValue += noiseStepZ;
						vals[localX][localZ] = noiseValue;

					}
					noiseStartZ += noiseStepX0;
					noiseEndZ += noiseStepX1;

				}
			}
		}
		return vals;
	}

	private float[][][] interpolateNoiseCompound3D(float[][][] NoiseSamples,int maxHeight,boolean shouldBlend)
	{
		float[][][] finalNoise = new float[16][maxHeight][16];
		int xzScale = 4;
		float quarter = 0.25f;
		float half = 0.5f;

		for (int x = 0; x < xzScale; ++x) {
			for (int z = 0; z < xzScale; ++z) {
				//int depth =0;

				for (int y = maxHeight / 2 - 1; y >= 0; y--) {

					float x0y0z0 = NoiseSamples[x][y][z];
					float x0y0z1 = NoiseSamples[x][y][z + 1];
					float x1y0z0 = NoiseSamples[x + 1][y][z];
					float x1y0z1 = NoiseSamples[x + 1][y][z + 1];
					float x0y1z0 = NoiseSamples[x][y + 1][z];
					float x0y1z1 = NoiseSamples[x][y + 1][z + 1];
					float x1y1z0 = NoiseSamples[x + 1][y + 1][z];
					float x1y1z1 = NoiseSamples[x + 1][y + 1][z + 1];

					// how much to increment noise along y value linear interpolation from start y and end y
					float noiseStepY00 = (x0y1z0 - x0y0z0) * -half;
					float noiseStepY01 = (x0y1z1 - x0y0z1) * -half;
					float noiseStepY10 = (x1y1z0 - x1y0z0) * -half;
					float noiseStepY11 = (x1y1z1 - x1y0z1) * -half;

					// noise values of 4 corners at y=0
					float noiseStartX0 = x0y0z0;
					float noiseStartX1 = x0y0z1;
					float noiseEndX0 = x1y0z0;
					float noiseEndX1 = x1y0z1;

					for (int suby = 1; suby >= 0; suby--) {
						int localY = suby + y * 2;

						float noiseStartZ = noiseStartX0;
						float noiseEndZ = noiseStartX1;

						// how much to increment X values, linear interpolation
						float noiseStepX0 = (noiseEndX0 - noiseStartX0) * quarter;
						float noiseStepX1 = (noiseEndX1 - noiseStartX1) * quarter;

						for (int subx = 0; subx < 4; subx++) {
							int localX = subx + x * 4;

							// how much to increment Z values, linear interpolation
							float noiseStepZ = (noiseEndZ - noiseStartZ) * quarter;

							// Y and X already interpolated, just need to interpolate final 4 Z block to get final noise value
							float noiseValue = noiseStartZ;

							for (int subz = 0; subz < 4; subz++) {
								int localZ = subz + z * 4;

								noiseValue += noiseStepZ;
								finalNoise[localX][localY][localZ] = noiseValue;
							}
							noiseStartZ += noiseStepX0;
							noiseEndZ += noiseStepX1;

						}
						noiseStartX0 += noiseStepY00;
						noiseStartX1 += noiseStepY01;
						noiseEndX0 += noiseStepY10;
						noiseEndX1 += noiseStepY11;
					}
				}
			}
		}
		if(shouldBlend) {
			return blendNoiseLayers(finalNoise);
		}
		else
		{return finalNoise;}
	}

	private float[][][] blendNoiseLayers(float[][][] input)
	{
		float eighth = 0.125f;
		float tenth = 0.1f;
		for (int y = maxCaveHeight ; y > 0; y--) {
			float blend;
			if(layer1MinY+8>=y&&y>layer1MinY)
			{
				blend = MathHelper.lerp(0,1,(y-layer1MinY)*eighth);
			}
			else if(layer1MinY>=y&&y>layer1MinY-8)
			{
				blend = MathHelper.lerp(0,1,(layer1MinY-y)*eighth);
			}
			else if(layer2MinY+8>=y&&y>layer2MinY)
			{
				blend = MathHelper.lerp(0,1,(y-layer2MinY)*eighth);
			}
			else if(layer2MinY>=y&&y>layer2MinY-8)
			{
				blend = MathHelper.lerp(0,1,(layer2MinY-y)*eighth);
			}
			else if(layer3MinY+10>=y&&y>layer3MinY)
			{
				blend = MathHelper.lerp(0,1,(y-layer3MinY)*tenth);
			}
			else
			{blend = 1;}
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					input[x][y][z] *=blend;
				}
			}
		}
		return input;
	}
}
