package diarr.caveuberhaul2.particles;

import net.minecraft.client.entity.particle.Particle;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.world.World;

public class ParticleVoidFox extends Particle {

    public ParticleVoidFox(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        float var14 = this.random.nextFloat() * 0.1F + 0.2F;
        this.rCol = var14;
        this.gCol = var14;
        this.bCol = var14;
        this.tex = TextureRegistry.getTexture("minecraft:particle/puff_0");
        this.setSize(0.02F, 0.02F);
        this.size *= this.random.nextFloat() * 0.6F + 0.5F;
        this.xd *= 0.02F;
        this.yd *= 0.02F;
        this.zd *= 0.02F;
        this.lifetime = (int)(20.0D / (Math.random() * 0.8D + 0.2D));
        this.collision = false;
    }

    public void render(Tessellator t, float partialTick, double xOff, double yOff, double zOff, float xa, float ya, float za, float xa2, float za2) {
        super.render(t,partialTick,xOff,yOff,zOff,xa,ya,za,xa2,za2);
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.99D;
        this.yd *= 0.99D;
        this.zd *= 0.99D;
        if(this.lifetime-- <= 0) {
            this.remove();
        }
    }
}
