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

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.material.MaterialHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Set;
import java.util.stream.Collectors;

// prevents deserts forming at high levels
public class SandBiomeModifier extends AbstractMaxHeightModifier {

    private final Set<Biome> biomes;

    public SandBiomeModifier(Seed seed, Climate climate, Levels levels) {
        super(seed, climate, 50, 2, levels.scale(8), levels.ground(5), levels.ground(25));
        this.biomes = Registry.BIOME.stream()
                .filter(biome -> MaterialHelper.isSand(biome.getSurfaceConfig().getTopMaterial().getBlock()))
                .collect(Collectors.toSet());
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public boolean test(Biome biome) {
        return biome.getCategory() == Biome.Category.DESERT || biomes.contains(biome);
    }

    @Override
    protected Biome getModifiedBiome(Biome in, Cell<Terrain> cell, int x, int z, float ox, float oz) {
        return Biomes.BADLANDS;
    }
}
