package diarr.caveuberhaul2.mixin;

import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.option.enums.RenderDistance;
import net.minecraft.client.render.FogManager;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

@Mixin(value = FogManager.class,remap = false)
public class FogManagerMixin {
	@Unique
	FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
	@Unique
	private float caveFogMinStart;
	@Shadow
	public float fogRed;
	@Shadow
	public float fogGreen;
	@Shadow
	public float fogBlue;
	@Final
	@Shadow
	public Minecraft mc;

	@Inject(method = "setupFog", at = @At("TAIL"))
	private void fogMixin(int fogMode, float farPlaneDistance, float partialTick, CallbackInfo ci) {
		PlayerLocal player = this.mc.thePlayer;
		Vec3 vec = player.getPosition(partialTick,false);
		int xCord = MathHelper.floor(vec.x);
		int yCord = MathHelper.floor(vec.y);
		int zCord = MathHelper.floor(vec.z);
		if (yCord < 48) {
			float min = (float)(RenderDistance.TINY.chunks * 16)/4;
			float max = (float)((this.mc.gameSettings.renderDistance.value) * 16);
			float skylightFogValue = this.mc.currentWorld.getSavedLightValue(LightLayer.Sky,xCord,yCord,zCord);
			float caveFogdist = (float) (MathHelper.clamp(MathHelper.lerp(min,max,(vec.y-8)/40),min,max)+skylightFogValue);
			if(fogMode < 0)
			{
				caveFogMinStart = 0.8f;
			}
			else
			{
				caveFogMinStart = 0.25f;
			}
			float caveFogMinStartLerped = (float) MathHelper.clamp(MathHelper.lerp(0.00f,caveFogMinStart,(vec.y-8)/40),0.05f,caveFogMinStart);
			GL11.glFogf(2915, caveFogdist *caveFogMinStartLerped);
			GL11.glFogf(2916, caveFogdist);
			GL11.glFogf(2914, 1.0F);
			GL11.glEnable(2903);
			GL11.glColorMaterial(1028, 4608);
		}
	}
	@Inject(method = "updateFogColor", at = @At("TAIL"))
	private void fogColorMixin(float renderPartialTicks, CallbackInfo ci) {
		Vec3 vec = this.mc.thePlayer.getPosition(renderPartialTicks,false);
		float yCord = (float) vec.y;
		if (yCord < 48) {
			float caveCol = MathHelper.lerp(0,1,(yCord-8)/40);
			//Vec3 vec3d1 = this.mc.currentWorld.getFogColor(mc.activeCamera, renderPartialTicks);
			this.fogRed *= caveCol;
			this.fogGreen *= caveCol;
			this.fogBlue *= caveCol;
			GL11.glClearColor(this.fogRed, this.fogGreen, this.fogBlue, 1.0F);
		}
	}
	@Unique
	private FloatBuffer getFogColor(float r, float g, float b, float a) {
		this.fogColorBuffer.clear();
		this.fogColorBuffer.put(r).put(g).put(b).put(a);
		this.fogColorBuffer.flip();
		return this.fogColorBuffer;
	}
}
