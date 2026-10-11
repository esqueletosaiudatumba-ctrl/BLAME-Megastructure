package blame.blamegen;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import blame.blamegen.Tags;

/**
 * Fog follows skylight: bright daylight fog on the beach and in sunlit shafts, fading to
 * near-black inside the megastructure so distant geometry disappears into darkness instead
 * of into a sky-blue haze. Vanilla would also black out the fog below Y=32 even in daylight;
 * this replaces that with the skylight-based rule.
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MOD_ID)
public class ClientEvents {

    private static float brightness = 1.0F;
    private static long lastNanos = 0L;

    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        if (world == null || world.getWorldType() != BlameGen.WORLD_TYPE || world.provider.getDimension() != 0) return;
        Entity e = event.getEntity();
        float pt = (float) event.getRenderPartialTicks();
        BlockPos eye = new BlockPos(e.getPositionEyes(pt));
        float target = world.getLightFor(EnumSkyBlock.SKY, eye) / 15.0F;

        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 1.0F : Math.min(1.0F, (now - lastNanos) / 1.0e9F);
        lastNanos = now;
        brightness += (target - brightness) * Math.min(1.0F, dt * 1.5F);

        Vec3d sky = world.getSkyColor(e, pt);
        Vec3d fog = world.getFogColor(pt);
        float rd = mc.gameSettings.renderDistanceChunks / 32.0F;
        float k = 1.0F - (float) Math.pow(0.25F + 0.75F * rd, 0.25F);
        float r = (float) (fog.x + (sky.x - fog.x) * k);
        float g = (float) (fog.y + (sky.y - fog.y) * k);
        float b = (float) (fog.z + (sky.z - fog.z) * k);
        float lit = brightness * brightness;
        event.setRed(0.025F + (r - 0.025F) * lit);
        event.setGreen(0.028F + (g - 0.028F) * lit);
        event.setBlue(0.036F + (b - 0.036F) * lit);
    }
}
