package com.terraforged.mod.command.search;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.WorldGenerator;
import com.terraforged.core.world.terrain.Terrain;
import net.minecraft.util.math.BlockPos;

public class TerrainSearchTask extends Search {

    private final Terrain type;
    private final WorldGenerator generator;
    private final Cell<Terrain> cell = new Cell<>();

    public TerrainSearchTask(BlockPos center, WorldGenerator generator, Terrain type) {
        super(center, 256);
        this.type = type;
        this.generator = generator;
    }

    @Override
    public int getSpacing() {
        return 20;
    }

    @Override
    public boolean test(BlockPos pos) {
        generator.getHeightmap().apply(cell, pos.getX(), pos.getZ());
        return cell.tag == type;
    }
}
