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

package com.terraforged.mod.chunk.fix;

import com.terraforged.api.chunk.ChunkDelegate;
import com.terraforged.api.material.state.States;
import com.terraforged.mod.material.MaterialHelper;
import com.terraforged.mod.material.Materials;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class ChunkCarverFix implements ChunkDelegate {

    private final Chunk delegate;
    private final Materials materials;

    public ChunkCarverFix(Chunk chunk, Materials materials) {
        this.delegate = chunk;
        this.materials = materials;
    }

    @Override
    public Chunk getDelegate() {
        return delegate;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = getDelegate().getBlockState(pos);
        if (MaterialHelper.isAir(state.getBlock())) {
            return state;
        }
        if (MaterialHelper.isGrass(state.getBlock())) {
            return States.GRASS_BLOCK.get();
        }
        if (materials.isStone(state.getBlock())) {
            return States.STONE.get();
        }
        if (materials.isEarth(state.getBlock())) {
            return States.DIRT.get();
        }
        if (materials.isClay(state.getBlock())) {
            return States.DIRT.get();
        }
        if (materials.isSediment(state.getBlock())) {
            return States.SAND.get();
        }
        return state;
    }
}
