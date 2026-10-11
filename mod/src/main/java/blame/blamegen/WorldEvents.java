package blame.blamegen;

import blame.blamegen.core.Arrival;
import blame.blamegen.core.Megastructure;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * Server-side world setup for BLAME worlds: fixed beach spawn, locked clear night (midnight), no mobs,
 * and a first-join placement facing the gate.
 */
public class WorldEvents {

    /** Arrival geometry does not depend on the seed, so any instance gives the spawn height. */
    static final int SPAWN_Y = new Megastructure(0L).spawnY();
    private static final String ARRIVED = "blamegen_arrived";
    /** Minecraft ticks: 18000 is midnight, so the sky stays dark and starlit forever. */
    static final long NIGHT_TIME = 18000L;

    static boolean isBlame(World world) {
        return world.getWorldType() == BlameGen.WORLD_TYPE && world.provider.getDimension() == 0;
    }

    @SubscribeEvent
    public void onCreateSpawn(WorldEvent.CreateSpawnPosition event) {
        World world = event.getWorld();
        if (!isBlame(world)) return;
        WorldInfo info = world.getWorldInfo();
        info.setSpawn(new BlockPos(Arrival.SPAWN_X, SPAWN_Y, Arrival.SPAWN_Z));
        GameRules rules = world.getGameRules();
        rules.setOrCreateGameRule("doDaylightCycle", "false");
        rules.setOrCreateGameRule("doWeatherCycle", "false");
        rules.setOrCreateGameRule("doMobSpawning", "false");
        rules.setOrCreateGameRule("spawnRadius", "0");
        info.setWorldTime(NIGHT_TIME);
        info.setRaining(false);
        info.setThundering(false);
        info.setCleanWeatherTime(1000000);
        info.setDifficulty(EnumDifficulty.PEACEFUL);
        event.setCanceled(true);
        BlameGen.LOGGER.info("BLAME world created: spawn at {} {} {}, clear night locked", Arrival.SPAWN_X, SPAWN_Y, Arrival.SPAWN_Z);
    }

    /** Existing saves keep their old clock, so re-lock the night every time the world loads. */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote || !isBlame(world)) return;
        world.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        world.getWorldInfo().setWorldTime(NIGHT_TIME);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (!(player instanceof EntityPlayerMP) || !isBlame(player.world)) return;
        NBTTagCompound persisted = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(ARRIVED)) return;
        persisted.setBoolean(ARRIVED, true);
        player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        // Stand on the beach looking north across the bay toward the gate.
        ((EntityPlayerMP) player).connection.setPlayerLocation(Arrival.SPAWN_X + 0.5, SPAWN_Y, Arrival.SPAWN_Z + 0.5, 180.0F, -8.0F);
    }
}
