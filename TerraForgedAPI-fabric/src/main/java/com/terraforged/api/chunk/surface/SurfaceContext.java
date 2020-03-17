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

package com.terraforged.api.chunk.surface;

import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrains;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class SurfaceContext extends DecoratorContext {

    public final BlockState solid;
    public final BlockState fluid;
    public final int seaLevel;
    public final long seed;
    public final CachedSurface cached = new CachedSurface();

    public double noise;

    public SurfaceContext(Chunk chunk, Levels levels, Terrains terrain, Climate climate, ChunkGeneratorConfig settings, long seed) {
        super(chunk, levels, terrain, climate);
        this.solid = settings.getDefaultBlock();
        this.fluid = settings.getDefaultFluid();
        this.seed = seed;
        this.seaLevel = levels.waterLevel;
    }
}
