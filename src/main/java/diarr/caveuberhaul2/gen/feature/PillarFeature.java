package diarr.caveuberhaul2.gen.feature;

import diarr.caveuberhaul2.FastNoiseLite;
import net.minecraft.core.block.*;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.util.phys.Vec3;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.feature.WorldFeature;

import java.util.Random;

public class PillarFeature extends WorldFeature {
	private final int maxLength = 32;
	private final int minLength = 5;
	private int inset;
	private FastNoiseLite noise;
	private int chunkX;
	private int chunkZ;
	public PillarFeature(int inset, FastNoiseLite noise, int chunkX, int chunkZ)
	{
		this.inset = inset;
		this.noise = noise;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}
	@Override
	public boolean place(World world, Random random, int i, int j, int k) {
		Vec3 start = Vec3.getPermanentVec3(i,j,k);
		Vec3 end = Vec3.getPermanentVec3(i+ (random.nextInt(5)-2),j-maxLength,k+ (random.nextInt(5)-2));
		HitResult hit = world.checkBlockCollisionBetweenPoints(start, end,false,true,true);
		if(hit == null  ) {
			return false;
		}
		else
		{
			int pillarLength = (j-hit.y);
			if(pillarLength<minLength ||pillarLength>maxLength)
			{
				return false;
			}
			else {
				//double maxRadius = (1 + Math.floor((pillarLength / 2) * random.nextDouble()));//random.nextDouble( (1+ Math.floor(pillarLength/2)));
				double maxRadius = random.nextInt(5)+2;
				if (canPlacePillarHere(world, i, j + inset, k, (int) Math.round(maxRadius)) && canPlacePillarHere(world, (int) Math.floor(hit.x), (int) Math.floor(hit.y) - inset, (int) Math.floor(hit.z), (int) Math.round(maxRadius))) {
					int id = world.getBlockId(i,j+1,k);
					double minRadius = MathHelper.clamp(maxRadius *random.nextDouble()-2,0,maxRadius*0.9);
					int bottomY = (int) Math.floor(hit.y) - inset;
					int topY = j + inset;
					for (int y = bottomY; y <= topY; y++) {
						double currentRadius;

						double max = topY-bottomY;
						double t = (max - (max - (y-bottomY))) / max ;
						currentRadius = MathHelper.lerp(minRadius, maxRadius, Math.abs(t*2f-1f));
						//System.out.println(topY);
						//System.out.println(bottomY);
						//System.out.println(y + " "+t);

						double squaredCurrentRadius = currentRadius * currentRadius;
						int currentRadiusInt = (int) Math.round(currentRadius)+1;
						int curX = (int) Math.floor(MathHelper.lerp(i, hit.x, t));
						int curZ = (int) Math.floor(MathHelper.lerp(k, hit.z, t));
						for (int x = curX - currentRadiusInt; x <= curX + currentRadiusInt; x++) {
							for (int z = curZ - currentRadiusInt; z <= curZ + currentRadiusInt; z++) {
								int dx = x - curX;
								int dz = z - curZ;
								//double rngFactor = (random.nextInt(3)-1)* random.nextDouble();
								if (dx * dx + dz * dz <= squaredCurrentRadius+noise.GetNoise(x,z)) {
									world.setBlock(x, y, z, id);
								}
								/*else
								{
									world.setBlock(x, y, z, Blocks.GLASS.id());
								}*/
							}
						}
					}
					return true;
				} else {
					return false;
				}
			}
		}
	}

	private boolean canPlacePillarHere(World world,int x,int y,int z,int radius)
	{
		Block[] blockArray = new Block[4];
		blockArray[0] = world.getBlock(x-radius,y,z);
		blockArray[1] = world.getBlock(x+radius,y,z);
		blockArray[2] = world.getBlock(x,y,z-radius);
		blockArray[3] = world.getBlock(x,y,z+radius);
		for(int i = 0;i<blockArray.length;i++)
		{
			if(blockArray[i] == null || !(blockArray[i].getLogic() instanceof BlockLogicStone))
			{
				return false;
			}
		}
		return true;
	}
}
