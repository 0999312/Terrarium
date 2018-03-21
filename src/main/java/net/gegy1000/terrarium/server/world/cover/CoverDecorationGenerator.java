package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class CoverDecorationGenerator extends CoverGenerator {
    private final Set<BlockPos> intersectionPoints = new HashSet<>();
    private int intersectionRange;

    protected CoverDecorationGenerator(CoverGenerationContext context, CoverType coverType) {
        super(context, coverType);
    }

    public abstract void decorate(int originX, int originZ, Random random);

    protected void decorateScatter(Random random, int originX, int originZ, int count, ScatterDecorateConsumer decorator) {
        World world = this.context.getWorld();
        CoverRasterTileAccess coverRaster = this.context.getCoverRaster();

        for (int i = 0; i < count; i++) {
            int scatterX = random.nextInt(16);
            int scatterZ = random.nextInt(16);

            if (coverRaster.get(scatterX, scatterZ) == this.coverType) {
                this.mutablePos.setPos(originX + scatterX, 0, originZ + scatterZ);

                if (this.tryPlace(random, this.mutablePos, scatterX, scatterZ)) {
                    BlockPos topBlock = world.getHeight(this.mutablePos);
                    if (!world.isAirBlock(topBlock)) {
                        world.setBlockToAir(topBlock);
                    }
                    decorator.handlePoint(topBlock, scatterX, scatterZ);
                }
            }
        }
    }

    private boolean tryPlace(Random random, BlockPos pos, int localX, int localZ) {
        if (this.intersectionRange > 0) {
            if (this.checkHorizontalIntersection(pos)) {
                return false;
            }
            this.intersectionPoints.add(pos.toImmutable());
        }
        int slope = this.context.getSlopeRaster().getUnsigned(localX, localZ);
        return slope < MOUNTAINOUS_SLOPE || random.nextInt(2) == 0;
    }

    private boolean checkHorizontalIntersection(BlockPos pos) {
        int range = this.intersectionRange;
        for (BlockPos intersectionPoint : this.intersectionPoints) {
            int deltaX = Math.abs(intersectionPoint.getX() - pos.getX());
            int deltaZ = Math.abs(intersectionPoint.getZ() - pos.getZ());
            if (deltaX <= range && deltaZ <= range) {
                return true;
            }
        }
        return false;
    }

    protected final void preventIntersection(int range) {
        this.intersectionRange = range;
    }

    protected final void stopIntersectionPrevention() {
        this.intersectionRange = -1;
        this.intersectionPoints.clear();
    }

    protected interface ScatterDecorateConsumer {
        void handlePoint(BlockPos pos, int localX, int localZ);
    }

    public static class Empty extends CoverDecorationGenerator {
        public Empty(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
        }
    }
}
