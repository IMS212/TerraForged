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

package com.terraforged.mod.gui.preview;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.RegionGenerator;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.mod.util.nbt.NBTHelper;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.nbt.CompoundNBT;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Preview extends Button {

    private static final int FACTOR = 4;
    private static final int BLOCK_SIZE = 256;//Size.chunkToBlock(1 << FACTOR);
    private static final float[] LEGEND_SCALES = {1, 0.9F, 0.75F, 0.6F};

    private final int offsetX;
    private final int offsetZ;
    private final Random random = new Random(System.currentTimeMillis());
    private final PreviewSettings previewSettings = new PreviewSettings();
    private final DynamicTexture texture = new DynamicTexture(new NativeImage(BLOCK_SIZE, BLOCK_SIZE, true));

    private int seed;
    private long lastUpdate = 0L;
    private Settings settings = new Settings();
    private Future<Region> task = null;
    private Region region = null;

    private String[] labels = {"Area: ", "Terrain: ", "Biome: "};
    private String[] values = {"", "", ""};

    public Preview() {
        super(0, 0, 0, 0, "", b -> {});
        this.seed = random.nextInt();
        this.offsetX = random.nextInt(50000) - 25000;
        this.offsetZ = random.nextInt(50000) - 25000;
    }

    public void regenerate() {
        this.seed = random.nextInt();
    }

    public void close() {
        texture.close();
    }

    @Override
    public void render(int mx, int my, float partialTicks) {
        preRender();

        texture.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableRescaleNormal();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        AbstractGui.blit(x, y, 0, 0, width, height, width, height);
        RenderSystem.disableRescaleNormal();

        updateLegend(mx, my);
        renderLegend(labels, values, x + 1, y + height + 2, 15, 0xFFFFFF);
    }

    public void update(Settings settings, CompoundNBT prevSettings) {
        long time = System.currentTimeMillis();
        if (time - lastUpdate < 50) {
            return;
        }
        lastUpdate = time;

        NBTHelper.deserialize(prevSettings, previewSettings);
        settings.generator.seed = seed;

        task = generate(settings, prevSettings);
    }

    private void preRender() {
        if (task != null && task.isDone()) {
            try {
                region = task.get();
                render(region);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            } finally {
                task = null;
            }
        }
    }

    private void render(Region region) {
        NativeImage image = texture.getTextureData();
        if (image == null) {
            return;
        }

        RenderMode renderer = previewSettings.mode;
        Terrains terrains = Terrains.create(settings);
        GeneratorContext context = new GeneratorContext(terrains, settings);

        int stroke = 2;
        int width = region.getBlockSize().size;
        region.iterate((cell, x, z) -> {
            if (x < stroke || z < stroke || x >= width - stroke || z >= width - stroke) {
                image.setPixelRGBA(x, z, Color.black.getRGB());
            } else {
                Color color = renderer.color(cell, context);
                image.setPixelRGBA(x, z, RenderMode.rgba(color));
            }
        });

        texture.updateDynamicTexture();
    }

    private Future<Region> generate(Settings settings, CompoundNBT prevSettings) {
        NBTHelper.deserialize(prevSettings, previewSettings);
        settings.generator.seed = seed;
        this.settings = settings;

        GeneratorContext context = new GeneratorContext(Terrains.create(settings), settings);

        RegionGenerator renderer = RegionGenerator.builder()
                .factory(new WorldGeneratorFactory(context))
                .pool(ThreadPool.getCommon())
                .size(FACTOR, 0)
                .build();

        return renderer.generate(offsetX, offsetZ, 101 - previewSettings.zoom, false);
    }

    private void updateLegend(int mx ,int my) {
        if (region != null) {
            int zoom = (101 - previewSettings.zoom);
            int width = Math.max(1, region.getBlockSize().size * zoom);
            int height = Math.max(1, region.getBlockSize().size * zoom);
            values[0] = width + "x" + height;

            if (mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height) {
                float fx = (mx - this.x) / (float) this.width;
                float fz = (my - this.y) / (float) this.height;
                int ix = NoiseUtil.round(fx * region.getBlockSize().size);
                int iz = NoiseUtil.round(fz * region.getBlockSize().size);
                Cell<Terrain> cell = region.getCell(ix, iz);
                values[1] = getTerrainName(cell);
                values[2] = getBiomeName(cell);
            }
        }
    }

    private float getLegendScale() {
        int index = Minecraft.getInstance().gameSettings.guiScale - 1;
        if (index < 0 || index >= LEGEND_SCALES.length) {
            // index=-1 == GuiScale(AUTO) which is the same as GuiScale(4)
            // values above 4 don't exist but who knows what mods might try set it to
            // in both cases use the smallest acceptable scale
            index = LEGEND_SCALES.length - 1;
        }
        return LEGEND_SCALES[index];
    }

    private void renderLegend(String[] labels, String[] values, int left, int top, int lineHeight, int color) {
        float scale = getLegendScale();
        lineHeight = Math.round(lineHeight * scale);

        RenderSystem.pushMatrix();
        RenderSystem.translatef(left, top, 0);
        RenderSystem.scalef(scale, scale, 1);

        FontRenderer renderer = Minecraft.getInstance().fontRenderer;
        int spacing = 0;
        for (String s : labels) {
            spacing = Math.max(spacing, renderer.getStringWidth(s));
        }

        int maxX = this.x + this.width;
        for (int i = 0; i < labels.length && i < values.length; i++) {
            String label = labels[i];
            String value = values[i];

            while (left + spacing + Minecraft.getInstance().fontRenderer.getStringWidth(value) > maxX) {
                value = value.substring(0, value.length() - 1);
            }

            drawString(renderer, label, 0, i * lineHeight, color);
            drawString(renderer, value, spacing, i * lineHeight, color);
        }

        RenderSystem.popMatrix();
    }

    private static String getTerrainName(Cell<Terrain> cell) {
        String terrain = cell.tag.getName().toLowerCase();
        if (terrain.contains("river")) {
            return "river";
        }
        return terrain;
    }

    private static String getBiomeName(Cell<Terrain> cell) {
        String terrain = cell.tag.getName().toLowerCase();
        if (terrain.contains("ocean")) {
            if (cell.temperature < 0.3) {
                return "cold_ocean";
            }
            if (cell.temperature > 0.6) {
                return "warm_ocean";
            }
            return "ocean";
        }
        if (terrain.contains("river")) {
            return "river";
        }
        return cell.biomeType.name().toLowerCase();
    }
}
