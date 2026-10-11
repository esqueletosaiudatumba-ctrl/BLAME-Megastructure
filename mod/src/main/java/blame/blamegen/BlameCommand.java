package blame.blamegen;

import blame.blamegen.core.Toha;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /blame list                    lists the landmarks with coordinates
 * /blame tp &lt;landmark&gt;          teleports to a landmark
 * /blame tp &lt;x&gt; &lt;y&gt; &lt;z&gt;        teleports to a coordinate, snapping to nearby floor
 *
 * Available to every player (permission level 0) because BLAME worlds are mostly singleplayer
 * with cheats off and the structure is far too large to cross on foot.
 */
public class BlameCommand extends CommandBase {

    private static final class Spot {
        final String name;
        final double x, y, z;
        final float yaw;
        final String blurb;

        Spot(String name, double x, double y, double z, float yaw, String blurb) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.blurb = blurb;
        }
    }

    /** Toha floors are at y 63, so feet at 64; the others come from the shared spawn height. */
    private static List<Spot> spots() {
        List<Spot> s = new ArrayList<Spot>();
        s.add(new Spot("beach", 0.5, WorldEvents.SPAWN_Y, 202.5, 180F, "the arrival beach (world spawn)"));
        s.add(new Spot("toha-gate", Toha.CX + 0.5, 64, Toha.CZ + 2732.5, 180F, "inside the monumental south gate tunnel"));
        s.add(new Spot("toha-south", Toha.CX + 0.5, 64, Toha.CZ + 2600.5, 180F, "inside the precinct, facing the monumental approach"));
        s.add(new Spot("toha-north", Toha.CX + 0.5, 64, Toha.CZ - 2600.5, 0F, "inside the precinct at the north wall"));
        s.add(new Spot("toha-east", Toha.CX + 2660.5, 64, Toha.CZ + 0.5, 90F, "inside the precinct at the east wall"));
        s.add(new Spot("toha-west", Toha.CX - 2660.5, 64, Toha.CZ + 0.5, 270F, "inside the precinct at the west wall"));
        s.add(new Spot("toha-center", Toha.CX + 0.5, 130, Toha.CZ + 800.5, 180F, "inside the principal cave mouth, facing the core furnace"));
        return s;
    }

    @Override
    public String getName() {
        return "blame";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/blame list | /blame tp <landmark> | /blame tp <x> <y> <z>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) throw new WrongUsageException(getUsage(sender));
        if (args[0].equalsIgnoreCase("list")) {
            for (Spot sp : spots()) {
                reply(sender, sp.name + "  " + (int) sp.x + " " + (int) sp.y + " " + (int) sp.z + "  - " + sp.blurb);
            }
            return;
        }
        if (!args[0].equalsIgnoreCase("tp") || args.length < 2) throw new WrongUsageException(getUsage(sender));

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        World world = player.world;
        if (!WorldEvents.isBlame(world)) throw new CommandException("/blame only works in the BLAME overworld");

        double x, y, z;
        float yaw = player.rotationYaw, pitch = 0F;
        String label;
        if (args.length >= 4) {
            x = parseDouble(args[1]);
            y = parseDouble(args[2]);
            z = parseDouble(args[3]);
            label = (int) x + " " + (int) y + " " + (int) z;
        } else {
            Spot found = null;
            for (Spot sp : spots()) if (sp.name.equalsIgnoreCase(args[1])) found = sp;
            if (found == null) throw new CommandException("Unknown landmark '" + args[1] + "'. Try /blame list");
            x = found.x;
            y = found.y;
            z = found.z;
            yaw = found.yaw;
            pitch = -4F;
            label = found.name;
        }

        BlockPos stand = findStand(world, (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        double ty = stand != null ? stand.getY() : y;
        player.dismountRidingEntity();
        player.connection.setPlayerLocation(x, ty, z, yaw, pitch);
        player.fallDistance = 0F;
        reply(sender, "Teleported to " + label + (stand == null ? " (no floor found nearby, placed as given)" : ""));
    }

    /**
     * Looks for a spot with a solid block under the feet and two blocks of headroom, searching
     * outward from the guessed height. Reading the blocks makes the cubic world generate them.
     */
    private static BlockPos findStand(World w, int x, int guessY, int z) {
        for (int d = 0; d <= 48; d++) {
            for (int s = (d == 0 ? 1 : -1); s <= 1; s += 2) {
                int y = guessY + s * d;
                BlockPos feet = new BlockPos(x, y, z);
                if (w.getBlockState(feet.down()).getMaterial().blocksMovement()
                        && !w.getBlockState(feet).getMaterial().blocksMovement()
                        && !w.getBlockState(feet.up()).getMaterial().blocksMovement()) {
                    return feet;
                }
            }
        }
        return null;
    }

    private static void reply(ICommandSender sender, String msg) {
        sender.sendMessage(new TextComponentString(msg));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, Arrays.asList("list", "tp"));
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            List<String> names = new ArrayList<String>();
            for (Spot sp : spots()) names.add(sp.name);
            return getListOfStringsMatchingLastWord(args, names);
        }
        return new ArrayList<String>();
    }
}
