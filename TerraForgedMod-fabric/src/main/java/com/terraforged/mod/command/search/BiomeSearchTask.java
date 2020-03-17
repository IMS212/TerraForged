package com.terraforged.mod.command.search;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

public class BiomeSearchTask extends Search {

    private final Biome biome;
    private final WorldView reader;

    public BiomeSearchTask(BlockPos center, WorldView reader, Biome biome) {
        super(center, 128);
        this.reader = reader;
        this.biome = biome;
    }

    @Override
    public int getSpacing() {
        return 10;
    }

    @Override
    public boolean test(BlockPos pos) {
        return reader.getGeneratorStoredBiome(pos.getX() >> 2, pos.getY(), pos.getZ() >> 2) == biome;
    }
}
