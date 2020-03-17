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

package com.terraforged.mod.biome.provider;

import com.terraforged.core.settings.BiomeSettings;
import com.terraforged.core.world.biome.BiomeData;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.map.BiomeMapBuilder;
import com.terraforged.mod.biome.map.BiomePredicate;
import me.dags.noise.util.Vec2f;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OceanRuinFeature;
import net.minecraft.world.gen.feature.OceanRuinFeatureConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BiomeHelper {

    private static final Map<BiomeType, BiomePredicate> PREDICATES = new HashMap<BiomeType, BiomePredicate>() {{
        put(BiomeType.TROPICAL_RAINFOREST, BiomePredicate.TROPICAL_RAINFOREST);
        put(BiomeType.SAVANNA,
                BiomePredicate.SAVANNA.or(BiomePredicate.MESA).not(BiomePredicate.DESERT).not(BiomePredicate.STEPPE).not(BiomePredicate.COAST)
                        .not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.DESERT,
                BiomePredicate.DESERT.or(BiomePredicate.MESA).not(BiomePredicate.COAST).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.TEMPERATE_RAINFOREST, BiomePredicate.TEMPERATE_RAINFOREST.not(BiomePredicate.COAST).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TEMPERATE_FOREST,
                BiomePredicate.TEMPERATE_FOREST.not(BiomePredicate.COAST).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.GRASSLAND, BiomePredicate.GRASSLAND.not(BiomePredicate.WETLAND).not(BiomePredicate.COAST).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.COLD_STEPPE, BiomePredicate.COLD_STEPPE.not(BiomePredicate.COAST).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.STEPPE, BiomePredicate.STEPPE.not(BiomePredicate.COAST).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TAIGA, BiomePredicate.TAIGA.not(BiomePredicate.TUNDRA).not(BiomePredicate.COLD_STEPPE).not(BiomePredicate.COAST)
                .not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TUNDRA, BiomePredicate.TUNDRA.not(BiomePredicate.TAIGA).not(BiomePredicate.COAST).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.ALPINE, BiomePredicate.MOUNTAIN);
    }};

    public static BiomeMap.Builder getBuilder(List<BiomeData> biomes) {
        return BiomeMapBuilder.basic(biomes);
    }

    public static BiomeMap getDefaultBiomeMap() {
        List<BiomeData> biomes = getAllBiomeData();
        BiomeMap.Builder builder = getBuilder(biomes);
        for (BiomeData data : biomes) {
            int weight = 10;
            Biome biome = (Biome) data.reference;

            if (biome.hasParent() && getId(biome).contains("hills")) {
                continue;
            }

            if (biome.getCategory() == Biome.Category.FOREST) {
                weight = 5;
            }

            if (biome.getCategory() == Biome.Category.MUSHROOM) {
                weight = 1;
            }

            // don't use BiomeDictionary with transient biomes todo detect rare
//            if (ForgeRegistries.BIOMES.containsKey(biome.getRegistryName())) {
//                if (BiomeDictionary.getTypes(biome).contains(BiomeDictionary.Type.RARE)) {
//                    weight = 1;
//                }
//            }

            if (biome.getCategory() == Biome.Category.OCEAN) {
                builder.addOcean(biome, weight);
            } else if (biome.getCategory() == Biome.Category.RIVER) {
                builder.addRiver(biome, weight);
            } else if (biome.getCategory() == Biome.Category.BEACH || biome == Biomes.STONE_SHORE) {
                builder.addBeach(biome, weight);
            } else if (biome.getCategory() == Biome.Category.SWAMP) {
                builder.addWetland(biome, weight);
            } else {
                Collection<BiomeType> types = getTypes(data, biome);
                for (BiomeType type : types) {
                    builder.addBiome(type, biome, weight);
                }
            }
        }

        builder.addBiome(BiomeType.TEMPERATE_RAINFOREST, Biomes.PLAINS, 10);
        builder.addBiome(BiomeType.TEMPERATE_FOREST, Biomes.FLOWER_FOREST, 3);
        builder.addBiome(BiomeType.TEMPERATE_FOREST, Biomes.PLAINS, 10);
        builder.addBiome(BiomeType.TUNDRA, ModBiomes.SNOWY_TAIGA_SCRUB, 5);
        builder.addBiome(BiomeType.TAIGA, ModBiomes.TAIGA_SCRUB, 5);

        return builder.build();
    }

    public static BiomeMap getBiomeMap(BiomeSettings settings) {
        List<BiomeData> biomes = getAllBiomeData();
        BiomeMap.Builder builder = getBuilder(biomes);
        Map<String, BiomeData> biomeMap = biomes.stream().collect(Collectors.toMap(d -> d.name, d -> d));
        Map<BiomeType, BiomeSettings.BiomeGroup> groupMap = settings.asMap();
        for (Map.Entry<BiomeType, BiomeSettings.BiomeGroup> e : groupMap.entrySet()) {
            for (BiomeSettings.BiomeWeight biomeWeight : e.getValue().biomes) {
                BiomeData data = biomeMap.get(biomeWeight.id);
                if (data == null) {
                    continue;
                }
                builder.addBiome(e.getKey(), (Biome) data.reference, biomeWeight.weight);
            }
        }
        for (BiomeData data : biomes) {
            Biome biome = (Biome) data.reference;
            if (biome.getCategory() == Biome.Category.OCEAN) {
                builder.addOcean(biome, 10);
            } else if (biome.getCategory() == Biome.Category.RIVER) {
                builder.addRiver(biome, 10);
            }
        }
        return builder.build();
    }

    public static Biome.TemperatureGroup getTemperatureGroup(Biome biome) {
        // vanilla ocean biome properties are not at all helpful for determining temperature
        if (biome.getCategory() == Biome.Category.OCEAN) {
            // warm & luke_warm oceans get OceanRuinStructure.Type.WARM
            OceanRuinFeatureConfig config = biome.getStructureFeatureConfig(Feature.OCEAN_RUIN);
            if (config != null) {
                if (config.biomeType == OceanRuinFeature.BiomeType.WARM) {
                    return Biome.TemperatureGroup.WARM;
                }
            }

            // if the id contains the world cold or frozen, assume it's cold
            if (getId(biome).contains("cold") || getId(biome).contains("frozen")) {
                return Biome.TemperatureGroup.COLD;
            }

            // the rest we categorize as medium
            return Biome.TemperatureGroup.MEDIUM;
        }
        // hopefully biomes otherwise have a sensible category
        return biome.getTemperatureGroup();
    }

    public static String getId(Biome biome) {
        Identifier name = Registry.BIOME.getId(biome);
        if (name == null) {
            return "unknown";
        }
        return name.toString();
    }

    public static Collection<BiomeType> getTypes(BiomeData data, Biome biome) {
        Set<BiomeType> types = new HashSet<>();
        for (Map.Entry<BiomeType, BiomePredicate> entry : PREDICATES.entrySet()) {
            if (entry.getValue().test(data, biome)) {
                types.add(entry.getKey());
            }
        }
        return types;
    }

    public static List<BiomeData> getAllBiomeData() {
        Collection<Biome> biomes = TerraBiomeRegistry.getInstance().getAll(BiomeHelper::filter);
        Vec2f tempRange = getRange(biomes, Biome::getTemperature);
        Vec2f moistRange = getRange(biomes, Biome::getRainfall);
        List<BiomeData> list = new LinkedList<>();
        for (Biome biome : biomes) {
            String name = getId(biome);
            float moisture = (biome.getRainfall() - moistRange.x) / (moistRange.y - moistRange.x);
            float temperature = (biome.getTemperature() - tempRange.x) / (tempRange.y - tempRange.x);
            int color = biome.getSurfaceConfig().getTopMaterial().getMaterial().getColor().color;
            list.add(new BiomeData(name, biome, color, moisture, temperature));
        }
        return list;
    }

    private static boolean filter(Biome biome) {
        if (biome.getCategory() == Biome.Category.NONE) {
            return true;
        }
        if (biome.getCategory() == Biome.Category.THEEND) {
            return true;
        }
        if (biome.getCategory() == Biome.Category.NETHER) {
            return true;
        }
        return false; //!BiomeDictionary.getTypes(biome).contains(BiomeDictionary.Type.OVERWORLD); todo dict
    }

    private static Vec2f getRange(Collection<Biome> biomes, Function<Biome, Float> getter) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (Biome biome : biomes) {
            float value = getter.apply(biome);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return new Vec2f(min, max);
    }
}
