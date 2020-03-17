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

package com.terraforged.mod.chunk.test;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.region.RegionCache;
import com.terraforged.core.region.RegionGenerator;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.heightmap.WorldHeightmap;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.biome.provider.BiomeProvider;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class TestChunkGenerator extends TerraChunkGenerator {

    private final BiomeProvider biomeProvider;

    public TestChunkGenerator(TerraContext context, BiomeProvider biomeProvider, ChunkGeneratorConfig settings) {
        super(context, biomeProvider, settings);
        this.biomeProvider = new TestBiomeProvider(context);
    }

    @Override
    protected RegionCache createRegionCache(TerraContext context) {
        return RegionGenerator.builder()
                .factory(new WorldGeneratorFactory(context, new TestHeightMap(context)))
                .pool(ThreadPool.getFixed())
                .size(3, 2)
                .build()
                .toCache(true);
    }

    @Override
    public BiomeProvider getBiomeSource() {
        return biomeProvider;
    }

    private static class TestHeightMap extends WorldHeightmap {

        private final Populator populator;

        public TestHeightMap(GeneratorContext context) {
            super(context);
            this.populator = getPopulator(Test.getTerrainType(context.terrain));
        }

        @Override
        public void apply(Cell<Terrain> cell, float x, float y) {
            super.apply(cell, x, y);
            populator.apply(cell, x, y);
        }

        @Override
        public void tag(Cell<Terrain> cell, float x, float y) {
            populator.tag(cell, x, y);
        }
    }
}
