package diarr.caveuberhaul2;

import diarr.caveuberhaul2.gen.chunk.TempChunkData;
import diarr.caveuberhaul2.particles.ParticleVoidFox;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.ParticleHelper;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;
import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.toml.Toml;

public class CaveUberhaul implements ModInitializer, RecipeEntrypoint, GameStartEntrypoint {
    public static final String MOD_ID = "caveuberhaul2";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static TomlConfigHandler CFG;
	private static final Toml TOML = new Toml("A comment");

	static{
		TOML.addCategory("IDs")
			.addEntry("Starting_item_id", 20000)
			.addEntry("Starting_block_id", 3000)
			.addEntry("Starting_entity_id",468);
			//.addEntry("Vanilla_caves",true);
		CFG = new TomlConfigHandler(MOD_ID, TOML);
	}
    @Override
    public void onInitialize() {
        LOGGER.info("Caving out world.");
    }

	@Override
	public void onRecipesReady() {

	}

	@Override
	public void initNamespaces() {

	}

	@Override
	public void beforeGameStart() {
		new TempChunkData();
		ParticleHelper.createParticle("voidFog", (world, x, y, z, motionX, motionY, motionZ, data) -> new ParticleVoidFox(world, x, y, z, motionX, motionY, motionZ));
	}

	@Override
	public void afterGameStart() {

	}
}
