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
        s.add(new Spot("toha-gate", Toha.CX + 0.5, 64, Toha.CZ + 3780.5, 180F, "at the southern valley mouth, facing the Toha spire"));
        s.add(new Spot("toha-south", Toha.CX + 0.5, 64, Toha.CZ + 2500.5, 180F, "on the southern valley causeway, looking toward the spire"));
        s.add(new Spot("toha-north", Toha.CX + 0.5, 64, Toha.CZ - 3200.5, 0F, "at the far valley causeway, facing south toward Toha"));
        s.add(new Spot("toha-east", Toha.CX + 3000.5, 455, Toha.CZ + 0.5, 90F, "on the eastern cliff catwalk, facing the spire"));
        s.add(new Spot("toha-west", Toha.CX - 3000.5, 455, Toha.CZ + 0.5, 270F, "on the western cliff catwalk, facing the spire"));
        s.add(new Spot("toha-center", Toha.CX + 0.5, 70, Toha.CZ + 610.5, 180F, "on the base apron beside the spire entrance"));
        return s;
    }

    @Override
    public String getName() {
        return "blame";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/blame tp <destination|x y z> | /blame toha [part] | /blame destinations";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    private static boolean isTeleportVerb(String verb) {
        return "tp".equalsIgnoreCase(verb)
                || "teleport".equalsIgnoreCase(verb)
                || "goto".equalsIgnoreCase(verb);
    }

    private static boolean isListVerb(String verb) {
        return "list".equalsIgnoreCase(verb)
                || "destinations".equalsIgnoreCase(verb)
                || "landmarks".equalsIgnoreCase(verb);
    }

    /**
     * Accept concise destination names and the full names printed by /blame destinations.
     * "toha" alone means the main gate; /blame tp toha <part> selects a specific edge or core.
     */
    private static Spot findSpot(String destination) {
        String key = destination.toLowerCase(java.util.Locale.ROOT).replace('_', '-');
        if (key.startsWith("toha-")) key = key.substring("toha-".length());

        if ("toha".equals(key) || "gate".equals(key) || "entrance".equals(key)
                || "portal".equals(key) || "main-gate".equals(key)) {
            key = "toha-gate";
        } else if ("spawn".equals(key) || "arrival".equals(key) || "shore".equals(key)) {
            key = "beach";
        } else if ("south".equals(key) || "south-wall".equals(key)
                || "approach".equals(key) || "precinct".equals(key)) {
            key = "toha-south";
        } else if ("north".equals(key) || "north-wall".equals(key)) {
            key = "toha-north";
        } else if ("east".equals(key) || "east-wall".equals(key)) {
            key = "toha-east";
        } else if ("west".equals(key) || "west-wall".equals(key)) {
            key = "toha-west";
        } else if ("center".equals(key) || "centre".equals(key) || "core".equals(key)
                || "engine".equals(key) || "furnace".equals(key) || "interior".equals(key)
                || "main-chamber".equals(key)) {
            key = "toha-center";
        } else if ("gate".equals(key)) {
            key = "toha-gate";
        }

        for (Spot spot : spots()) {
            if (spot.name.equalsIgnoreCase(key)) return spot;
        }
        return null;
    }

    private static void listSpots(ICommandSender sender) {
        reply(sender, "Destinations — use /blame tp <name> or /blame tp toha <part>:");
        for (Spot spot : spots()) {
            reply(sender, spot.name + "  " + (int) spot.x + " " + (int) spot.y + " " + (int) spot.z
                    + "  - " + spot.blurb);
        }
        reply(sender, "Shortcuts: gate/entrance, center/core, south, north, east, west, beach/spawn");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            reply(sender, getUsage(sender));
            reply(sender, "Examples: /blame tp gate | /blame tp toha core | /blame goto center | /blame tp 0 100 -3800");
            return;
        }
        if (isListVerb(args[0])) {
            listSpots(sender);
            return;
        }

        boolean directToha = "toha".equalsIgnoreCase(args[0]);
        if (!directToha && !isTeleportVerb(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }
        if (directToha && args.length > 2) throw new WrongUsageException(getUsage(sender));
        if (!directToha && args.length < 2) throw new WrongUsageException(getUsage(sender));

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        World world = player.world;
        if (!WorldEvents.isBlame(world)) throw new CommandException("/blame only works in the BLAME overworld");

        double x, y, z;
        float yaw = player.rotationYaw, pitch = 0F;
        String label;
        if (!directToha && args.length == 4) {
            x = parseDouble(args[1]);
            y = parseDouble(args[2]);
            z = parseDouble(args[3]);
            label = (int) x + " " + (int) y + " " + (int) z;
        } else {
            String destination;
            if (directToha) {
                destination = args.length == 1 ? "gate" : "toha-" + args[1];
            } else if (args.length == 2) {
                destination = args[1];
            } else if (args.length == 3 && "toha".equalsIgnoreCase(args[1])) {
                destination = args[2];
            } else {
                throw new WrongUsageException(getUsage(sender));
            }

            Spot found = findSpot(destination);
            if (found == null) {
                throw new CommandException("Unknown destination '" + destination
                        + "'. Try /blame destinations, or use gate, center, north, south, east, or west.");
            }
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
        reply(sender, "Teleported to " + label
                + (stand == null ? " (no floor found nearby, placed as given)" : ""));
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
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args,
                    Arrays.asList("help", "list", "destinations", "tp", "teleport", "goto", "toha"));
        }
        if (args.length == 2 && "toha".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args,
                    Arrays.asList("gate", "entrance", "south", "north", "east", "west", "center", "core"));
        }
        if (args.length == 2 && isTeleportVerb(args[0])) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList(
                    "beach", "spawn", "gate", "entrance", "toha", "toha-gate",
                    "south", "north", "east", "west", "center", "centre", "core",
                    "engine", "furnace", "interior", "toha-south", "toha-north",
                    "toha-east", "toha-west", "toha-center"));
        }
        if (args.length == 3 && isTeleportVerb(args[0])
                && "toha".equalsIgnoreCase(args[1])) {
            return getListOfStringsMatchingLastWord(args,
                    Arrays.asList("gate", "entrance", "south", "north", "east", "west", "center", "core"));
        }
        return new ArrayList<String>();
    }
}
