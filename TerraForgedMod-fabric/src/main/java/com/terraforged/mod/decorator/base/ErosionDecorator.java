/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.decorator.base;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.material.state.States;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.material.Materials;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;

public class ErosionDecorator implements ColumnDecorator {

    private static final int ROCK_VAR = 30;
    private static final int ROCK_MIN = 140;

    private static final int DIRT_VAR = 40;
    private static final int DIRT_MIN = 95;

    public static final float ROCK_STEEPNESS = 0.65F;
    private static final float DIRT_STEEPNESS = 0.475F;
    private static final float SCREE_STEEPNESS = 0.4F;

    public static final float HEIGHT_MODIFIER = 6F / 255F;
    public static final float SLOPE_MODIFIER = 3F / 255F;

    private static final float SEDIMENT_MODIFIER = 256;
    private static final float SEDIMENT_NOISE = 3F / 255F;
    private static final float SCREE_VALUE = 0.55F;

    private final int seed1;
    private final int seed2;
    private final int seed3;
    private final float minY;
    private final Terrains terrain;
    private final Materials materials;

    public ErosionDecorator(TerraContext context) {
        this.terrain = context.terrain;
        this.seed1 = context.seed.next();
        this.seed2 = context.seed.next();
        this.seed3 = context.seed.next();
        this.minY = context.levels.ground(4);
        this.materials = context.materials;
    }

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        if (context.cell.value < minY || context.cell.tag == terrain.river || context.cell.tag == terrain.riverBanks) {
            return;
        }

        if (context.cell.tag == terrain.volcanoPipe) {
            return;
        }

        int topY = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, x, z);
        if (topY - 1 > y) {
            y = topY;
        }

        ISurfaceBuilderConfig config = context.biome.getSurfaceBuilderConfig();
        BlockState top = config.getTop();
        BlockState middle = config.getUnder();

        if (materials.isErodible(top.getBlock())) {
            BlockState material = getMaterial(x, z, context, top, middle);
            if (material != top) {
                if (materials.isStone(material.getBlock())) {
                    erodeRock(context, chunk, x, y, z);
                    return;
                } else {
                    fillDown(context, chunk, x, z, y, y - 4, material);
                }
            }
            placeScree(chunk, context, x, y, z);
        }
    }

    protected void erodeRock(DecoratorContext context, IChunk chunk, int dx, int y, int dz) {
        int depth = 32;
        BlockState material = Blocks.GRAVEL.getDefaultState();
        // find the uppermost layer of rock & record it's depth
        for (int dy = 3; dy < 32; dy++) {
            context.pos.setY(y - dy);
            BlockState state = chunk.getBlockState(context.pos);
            if (materials.isStone(state.getBlock())) {
                material = state;
                depth = dy + 1;
                break;
            }
        }

        // fill downwards to the first rock layer
        for (int dy = 0; dy < depth; dy++) {
            context.pos.setY(y - dy);
            chunk.setBlockState(context.pos, material, false);
        }
    }

    protected void placeScree(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        float steepness = context.cell.steepness + context.climate.getRand().getValue(x, z, seed2) * SLOPE_MODIFIER;
        if (steepness < SCREE_STEEPNESS) {
            return;
        }

        float sediment = context.cell.sediment * SEDIMENT_MODIFIER;
        float noise = context.climate.getRand().getValue(x, z, seed3) * SEDIMENT_NOISE;
        if (sediment + noise > SCREE_VALUE) {
            fillDown(context, chunk, x, z, y, y - 2, States.GRAVEL.get());
        }
    }

    public boolean erodeRock(float x, float z, float steepness, float height) {
        return steepness > ROCK_STEEPNESS || height > ColumnDecorator.getNoise(x, z, seed1, ROCK_VAR, ROCK_MIN);
    }

    public boolean erodeDirt(float x, float z, float steepness, float height) {
        return steepness > DIRT_STEEPNESS && height > ColumnDecorator.getNoise(x, z, seed2, DIRT_VAR, DIRT_MIN);
    }

    private BlockState getMaterial(float x, float z, DecoratorContext context, BlockState top, BlockState middle) {
        float height = context.cell.value + context.climate.getRand().getValue(x, z, seed1) * HEIGHT_MODIFIER;
        float steepness = context.cell.steepness + context.climate.getRand().getValue(x, z, seed2) * SLOPE_MODIFIER;

        if (steepness > ROCK_STEEPNESS || height > ColumnDecorator.getNoise(x, z, seed1, ROCK_VAR, ROCK_MIN)) {
            return rock(middle);
        }

        if (steepness > DIRT_STEEPNESS && height > ColumnDecorator.getNoise(x, z, seed2, DIRT_VAR, DIRT_MIN)) {
            return ground(top);
        }

        return top;
    }

    private static BlockState rock(BlockState state) {
        if (state.getMaterial() == Material.ROCK) {
            return state;
        }
        return States.STONE.get();
    }

    private static BlockState ground(BlockState state) {
        if (state.getMaterial() == Material.ORGANIC) {
            return States.DIRT.get();
        }
        if (state.getMaterial() == Material.ROCK) {
            return States.GRAVEL.get();
        }
        if (state.getMaterial() == Material.EARTH) {
            return state;
        }
        if (state.getMaterial() == Material.SAND) {
            if (state.getBlock() == Blocks.SAND) {
                return States.SANDSTONE.get();
            }
            if (state.getBlock() == Blocks.RED_SAND) {
                return States.RED_SANDSTONE.get();
            }
        }
        return States.COARSE_DIRT.get();
    }
}
