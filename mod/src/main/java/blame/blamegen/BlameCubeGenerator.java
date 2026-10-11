package blame.blamegen;

import blame.blamegen.core.Cube;
import blame.blamegen.core.Mat;
import blame.blamegen.core.Megastructure;
import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockEndRod;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Adapts the pure-Java {@link Megastructure} to CubicChunks' cube generator API. */
public class BlameCubeGenerator implements ICubeGenerator {

    private static final IBlockState[] STATES = buildStates();

    private final Megastructure megastructure;

    public BlameCubeGenerator(World world) {
        this.megastructure = new Megastructure(world.getSeed());
    }

    private static IBlockState[] buildStates() {
        IBlockState[] s = new IBlockState[Mat.COUNT];
        s[Mat.AIR] = Blocks.AIR.getDefaultState();
        s[Mat.CONC_GRAY] = Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
        s[Mat.CONC_BLACK] = Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK);
        s[Mat.CONC_LGRAY] = Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
        s[Mat.STONE] = Blocks.STONE.getDefaultState();
        s[Mat.STONEBRICK] = Blocks.STONEBRICK.getDefaultState();
        s[Mat.STONEBRICK_CRACKED] = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED);
        s[Mat.ANDESITE] = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE_SMOOTH);
        s[Mat.TERRA_GRAY] = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
        s[Mat.TERRA_BLACK] = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK);
        s[Mat.TERRA_LGRAY] = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
        s[Mat.COAL] = Blocks.COAL_BLOCK.getDefaultState();
        s[Mat.IRON_BLOCK] = Blocks.IRON_BLOCK.getDefaultState();
        s[Mat.IRON_BARS] = Blocks.IRON_BARS.getDefaultState();
        s[Mat.SEA_LANTERN] = Blocks.SEA_LANTERN.getDefaultState();
        s[Mat.GLOWSTONE] = Blocks.GLOWSTONE.getDefaultState();
        s[Mat.END_ROD_UP] = Blocks.END_ROD.getDefaultState().withProperty(BlockEndRod.FACING, EnumFacing.UP);
        s[Mat.END_ROD_DOWN] = Blocks.END_ROD.getDefaultState().withProperty(BlockEndRod.FACING, EnumFacing.DOWN);
        s[Mat.SAND] = Blocks.SAND.getDefaultState();
        s[Mat.SANDSTONE] = Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH);
        s[Mat.WATER] = Blocks.WATER.getDefaultState();
        s[Mat.LADDER_N] = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.NORTH);
        s[Mat.LADDER_S] = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH);
        s[Mat.LADDER_W] = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.WEST);
        s[Mat.LADDER_E] = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.EAST);
        IBlockState slab = Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.STONE);
        s[Mat.SLAB_BOTTOM] = slab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
        s[Mat.SLAB_TOP] = slab.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
        s[Mat.DOUBLE_SLAB] = Blocks.DOUBLE_STONE_SLAB.getDefaultState()
                .withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.STONE)
                .withProperty(BlockStoneSlab.SEAMLESS, Boolean.TRUE);
        s[Mat.GRAVEL] = Blocks.GRAVEL.getDefaultState();
        s[Mat.OBSIDIAN] = Blocks.OBSIDIAN.getDefaultState();
        s[Mat.GLASS_GRAY] = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
        // New materials
        s[Mat.LAMP_RED] = getLitRedstoneLamp();
        s[Mat.LAMP_AMBER] = Blocks.GLOWSTONE.getDefaultState(); // closest vanilla amber glow
        s[Mat.LAMP_GREEN] = Blocks.SEA_LANTERN.getDefaultState(); // cyan-green glow
        s[Mat.PIPE] = Blocks.PRISMARINE.getDefaultState(); // prismarine for plumbing
        s[Mat.COPPER_PATINA] = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN); // teal patina
        s[Mat.COPPER_RAW] = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE); // raw copper orange
        s[Mat.COPPER_WEATHERED] = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIME); // weathered green-brown
        s[Mat.CONC_RUST] = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED); // rust red
        s[Mat.BIO_CYAN] = Blocks.SEA_LANTERN.getDefaultState(); // bioluminescent cyan
        s[Mat.BIO_MAGENTA] = getLitRedstoneLamp(); // bioluminescent magenta
        s[Mat.CABLE] = Blocks.IRON_BARS.getDefaultState(); // cable bundles
        s[Mat.VENT] = Blocks.IRON_BARS.getDefaultState(); // ventilation grills
        s[Mat.REBAR] = Blocks.IRON_BARS.getDefaultState(); // exposed rebar
        s[Mat.GLASS_REINFORCED] = Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK); // dark reinforced
        for (int i = 0; i < s.length; i++) {
            if (s[i] == null) throw new IllegalStateException("No block state for material " + i);
        }
        return s;
    }

    private static IBlockState getLitRedstoneLamp() {
        // Metadata 1 creates the lit state without reflecting a private property field.
        return Blocks.REDSTONE_LAMP.getStateFromMeta(1);
    }

    public int spawnY() {
        return megastructure.spawnY();
    }

    @Override
    @Deprecated
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        return generateCube(cubeX, cubeY, cubeZ, new CubePrimer());
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ, CubePrimer primer) {
        Cube c = new Cube(cubeX, cubeY, cubeZ);
        megastructure.generate(c);
        byte[] b = c.b;
        for (int i = 0; i < 4096; i++) {
            // Write every block, including air, so a reused primer never leaks old contents.
            primer.setBlockState(i & 15, i >> 8, (i >> 4) & 15, STATES[b[i]]);
        }
        return primer;
    }

    @Override
    public void generateColumn(Chunk column) {
        // The Void biome has no rain and no mob spawn lists: the megastructure stays empty.
        byte[] biomes = column.getBiomeArray();
        Arrays.fill(biomes, (byte) Biome.getIdForBiome(Biomes.VOID));
    }

    @Override
    public void populate(ICube cube) {
    }

    @Override
    public Box getFullPopulationRequirements(ICube cube) {
        return ICubeGenerator.NO_REQUIREMENT;
    }

    @Override
    public Box getPopulationPregenerationRequirements(ICube cube) {
        return ICubeGenerator.NO_REQUIREMENT;
    }

    @Override
    public void recreateStructures(ICube cube) {
    }

    @Override
    public void recreateStructures(Chunk column) {
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        return null;
    }
}