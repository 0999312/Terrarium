package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.prime.PrimeChunk;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;

public class OceanFillSurfaceComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final IBlockState block;
    private final int oceanLevel;

    public OceanFillSurfaceComposer(RegionComponentType<ShortRasterTile> heightComponent, IBlockState block, int oceanLevel) {
        this.heightComponent = heightComponent;
        this.block = block;
        this.oceanLevel = oceanLevel;
    }

    @Override
    public void composeSurface(RegionGenerationHandler regionHandler, PrimeChunk chunk) {
        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        CubicPos pos = chunk.getPos();
        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = heightRaster.getShort(localX, localZ);
                if (height <= maxY && height < this.oceanLevel) {
                    int minOceanY = Math.max(height + 1, minY);
                    int maxOceanY = Math.min(this.oceanLevel, maxY);
                    for (int localY = minOceanY; localY <= maxOceanY; localY++) {
                        chunk.set(localX, localY, localZ, this.block);
                    }
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.heightComponent };
    }
}
