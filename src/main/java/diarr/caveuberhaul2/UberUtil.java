package diarr.caveuberhaul2;

public class UberUtil {
	public static float normalizeValue(float v, float max,float min)
	{
		return (v-min)/(max-min);
	}

	public static float[][] sampleNoise2D(FastNoiseLite noise, int chunkX, int chunkZ, int offX, int offZ)
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
}
