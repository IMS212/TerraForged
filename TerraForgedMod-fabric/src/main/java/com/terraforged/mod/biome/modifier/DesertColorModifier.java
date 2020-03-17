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

package com.terraforged.mod.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.biome.provider.DesertBiomes;
import net.minecraft.world.biome.Biome;

public class DesertColorModifier implements BiomeModifier {

    private final DesertBiomes biomes;

    public DesertColorModifier(DesertBiomes biomes) {
        this.biomes = biomes;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean test(Biome biome) {
        return biome.getCategory() == Biome.Category.DESERT;
    }

    @Override
    public Biome modify(Biome in, Cell<Terrain> cell, int x, int z) {
        if (biomes.isRedDesert(in)) {
            if (cell.continent <= 0.5F) {
                return biomes.getWhiteDesert(cell.biome);
            }
        } else if (cell.continent > 0.5F) {
            return biomes.getRedDesert(cell.biome);
        }
        return in;
    }
}
