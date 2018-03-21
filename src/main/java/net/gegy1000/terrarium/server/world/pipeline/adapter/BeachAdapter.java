package net.gegy1000.terrarium.server.world.pipeline.adapter;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.CoverTypeRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.World;

public class BeachAdapter implements RegionAdapter {
    private final RegionComponentType<CoverRasterTileAccess> coverComponent;
    private final int beachSize;

    private final CoverType waterCover;
    private final CoverType beachCover;

    public BeachAdapter(RegionComponentType<CoverRasterTileAccess> coverComponent, int beachSize, CoverType waterCover, CoverType beachCover) {
        this.coverComponent = coverComponent;
        this.beachSize = beachSize;
        this.waterCover = waterCover;
        this.beachCover = beachCover;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        CoverRasterTileAccess coverTile = data.getOrExcept(this.coverComponent);
        if (this.beachSize <= 0) {
            return;
        }

        CoverType[] coverBuffer = coverTile.getData();

        for (int localY = 0; localY < height; localY++) {
            CoverType last = coverBuffer[localY * width];
            for (int localX = 1; localX < width; localX++) {
                CoverType cover = coverBuffer[localX + localY * width];
                if (last != cover && cover == this.waterCover || last == this.waterCover) {
                    this.spreadBeach(this.beachSize - 1, width, height, localX, localY, coverBuffer);
                }
                last = cover;
            }
        }
    }

    private void spreadBeach(int beachSize, int width, int height, int localX, int localY, CoverType[] coverBuffer) {
        for (int beachY = -beachSize; beachY <= beachSize; beachY++) {
            int globalY = localY + beachY;
            if (globalY >= 0 && globalY < height) {
                for (int beachX = -beachSize; beachX <= beachSize; beachX++) {
                    int globalX = localX + beachX;
                    if (globalX >= 0 && globalX < width) {
                        int beachIndex = globalX + globalY * width;
                        if (coverBuffer[beachIndex] != this.waterCover) {
                            coverBuffer[beachIndex] = this.beachCover;
                        }
                    }
                }
            }
        }
    }

    public static class Parser implements InstanceObjectParser<RegionAdapter> {
        @Override
        public RegionAdapter parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);
            int beachSize = valueParser.parseInteger(objectRoot, "beach_size");
            CoverType waterCover = valueParser.parseRegistryEntry(objectRoot, "water_cover", CoverTypeRegistry.getRegistry());
            CoverType beachCover = valueParser.parseRegistryEntry(objectRoot, "beach_cover", CoverTypeRegistry.getRegistry());
            return new BeachAdapter(coverComponent, beachSize, waterCover, beachCover);
        }
    }
}
