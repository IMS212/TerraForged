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

package com.terraforged.mod.util.setup;

import com.terraforged.api.biome.modifier.ModifierManager;
import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorManager;
import com.terraforged.api.chunk.surface.SurfaceManager;
import com.terraforged.api.event.SetupEvent;
import com.terraforged.api.material.geology.GeologyManager;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.terrain.provider.TerrainProvider;
import com.terraforged.feature.modifier.FeatureModifiers;

import java.util.List;

public class SetupHooks {

    public static <T extends TerrainProvider> T setup(T provider, GeneratorContext context) {
        SetupEvent.TERRAIN.invoker().handle(provider, context);
        return provider;
    }

    public static <T extends SurfaceManager> T setup(T manager, GeneratorContext context) {
        SetupEvent.SURFACE.invoker().handle(manager, context);
        return manager;
    }

    public static <T extends ModifierManager> T setup(T manager, GeneratorContext context) {
        SetupEvent.BIOME_MODIFIER.invoker().handle(manager, context);
        return manager;
    }

    public static <T extends LayerManager> T setup(T manager, GeneratorContext context) {
        SetupEvent.LAYERS.invoker().handle(manager, context);
        return manager;
    }

    public static <T extends GeologyManager> T setup(T manager, GeneratorContext context) {
        SetupEvent.GEOLOGY.invoker().handle(manager, context);
        return manager;
    }

    public static <T extends FeatureModifiers> T setup(T manager, GeneratorContext context) {
        SetupEvent.FEATURES.invoker().handle(manager, context);
        return manager;
    }

    public static void setup(List<ColumnDecorator> base, List<ColumnDecorator> feature, GeneratorContext context) {
        SetupEvent.DECORATORS.invoker().handle(new DecoratorManager(base, feature), context);
    }
}
