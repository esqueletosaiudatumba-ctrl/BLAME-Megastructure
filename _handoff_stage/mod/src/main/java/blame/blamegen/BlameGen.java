package blame.blamegen;

import blame.blamegen.Tags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:cubicchunks", acceptedMinecraftVersions = "[1.12.2]")
public class BlameGen {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static BlameWorldType WORLD_TYPE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Constructing a WorldType registers it in WorldType.WORLD_TYPES under its name.
        WORLD_TYPE = new BlameWorldType();
        MinecraftForge.EVENT_BUS.register(new WorldEvents());
        LOGGER.info("Registered world type '{}' (generator version {})", WORLD_TYPE.getName(),
                blame.blamegen.core.Megastructure.GENERATOR_VERSION);
    }
}
