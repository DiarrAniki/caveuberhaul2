package diarr.caveuberhaul2.gen.chunk;

import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCoordinate;

import java.util.HashMap;
import java.util.Map;

public class TempChunkData {
	public static TempChunkData instance;
	public static Map<ChunkCoordinate,byte[][][]> decoratorChunkMap;

	public TempChunkData()
	{
		decoratorChunkMap = new HashMap<>();
		instance = this;
	}

	public static byte[][][] RetrieveData(World world, ChunkCoordinate chunkCoords)
	{
		if(decoratorChunkMap.containsKey(chunkCoords))
		{
			return decoratorChunkMap.get(chunkCoords);
		}
		else
		{
			byte[][][] arr = new byte[16][world.getHeightBlocks()][16];
			decoratorChunkMap.put(chunkCoords,arr);
			return arr;
		}
	}

}
