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

package com.terraforged.mod.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.terraforged.mod.material.Materials;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.util.Collection;

public class WorldGenBlocks extends DataGen {

    public static void genBlockTags(File dataDir) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            Materials materials = new Materials();
            printMaterials(dataDir, "stone", materials.getStone());
            printMaterials(dataDir, "dirt", materials.getDirt());
            printMaterials(dataDir, "clay", materials.getClay());
            printMaterials(dataDir, "sediment",materials.getSediment());
            printMaterials(dataDir, "ore", materials.getOre());
        }
    }

    private static void printMaterials(File dir, String name, Collection<Block> blocks) {
        String path = getJsonPath("tags/blocks", new Identifier("terraforged", name));
        write(new File(dir, path), writer -> {
            JsonObject root = new JsonObject();
            JsonArray values = new JsonArray();
            root.addProperty("replace", false);
            root.add("values", values);
            for (Block block : blocks) {
                values.add(String.valueOf(Registry.BLOCK.getId(block)));
            }
            write(root, writer);
        });
    }
}
