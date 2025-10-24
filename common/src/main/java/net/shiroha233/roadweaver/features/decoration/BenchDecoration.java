package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.config.DecorationConfig;
import net.shiroha233.roadweaver.features.roadlogic.Road;
import net.shiroha233.roadweaver.features.biome.BiomeConnectionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 长椅装饰实现
 * 提供多种风格的长椅，包括公园长椅、现代长椅和乡村长椅
 */
public class BenchDecoration implements RoadDecoration {
    
    private final Random random;
    private final DecorationConfig config;
    
    public BenchDecoration(Random random, DecorationConfig config) {
        this.random = random;
        this.config = config;
    }
    
    @Override
    public String getName() {
        return "bench";
    }
    
    @Override
    public DecorationType getType() {
        return DecorationType.STREET_FURNITURE;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, BiomeConnectionStrategy biomeStrategy) {
        // 检查位置是否适合放置长椅
        if (!level.getBlockState(pos.below()).isSolid()) {
            return false;
        }
        
        // 检查周围空间是否足够
        BlockPos checkPos1 = pos.offset(1, 0, 0);
        BlockPos checkPos2 = pos.offset(-1, 0, 0);
        BlockPos checkPos3 = pos.offset(0, 0, 1);
        BlockPos checkPos4 = pos.offset(0, 0, -1);
        
        // 至少需要一侧有足够的空间
        boolean hasSpace = level.isEmptyBlock(checkPos1) && level.isEmptyBlock(checkPos1.above()) ||
                          level.isEmptyBlock(checkPos2) && level.isEmptyBlock(checkPos2.above()) ||
                          level.isEmptyBlock(checkPos3) && level.isEmptyBlock(checkPos3.above()) ||
                          level.isEmptyBlock(checkPos4) && level.isEmptyBlock(checkPos4.above());
        
        return hasSpace;
    }
    
    @Override
    public List<BlockPlacement> generateDecoration(LevelAccessor level, BlockPos pos, Road road, BiomeConnectionStrategy biomeStrategy) {
        List<BlockPlacement> placements = new ArrayList<>();
        
        // 根据生物群系和道路等级选择长椅风格
        BenchStyle style = selectBenchStyle(biomeStrategy, road.getWidthLevel());
        
        // 生成长椅
        generateBenchStructure(placements, level, pos, style, road.getDirection());
        
        // 添加周围装饰
        addSurroundingDecorations(placements, level, pos, style);
        
        return placements;
    }
    
    @Override
    public double getPlacementProbability(Road road, BiomeConnectionStrategy biomeStrategy) {
        double baseProbability = config.getDecorationDensity(getName());
        
        // 根据道路等级调整概率
        switch (road.getWidthLevel()) {
            case HIGHWAY:
                return baseProbability * 0.1; // 高速公路很少放置长椅
            case MAIN_ROAD:
                return baseProbability * 0.3; // 主干道少量放置
            case SECONDARY_ROAD:
                return baseProbability * 0.7; // 次干道适中放置
            case RESIDENTIAL_ROAD:
                return baseProbability * 1.2; // 居民区道路较多放置
            case NATURAL_PATH:
                return baseProbability * 0.8; // 自然路径适中放置
            default:
                return baseProbability;
        }
    }
    
    @Override
    public int getMinDistance() {
        return 8; // 长椅之间的最小距离
    }
    
    @Override
    public boolean isCompatibleWith(RoadDecoration other) {
        // 长椅可以与大多数装饰兼容，但不与其他长椅太近
        if (other instanceof BenchDecoration) {
            return false;
        }
        return true;
    }
    
    /**
     * 选择长椅风格
     */
    private BenchStyle selectBenchStyle(BiomeConnectionStrategy biomeStrategy, Road.WidthLevel widthLevel) {
        String biomeType = biomeStrategy.getBiomeType().toLowerCase();
        double rand = random.nextDouble();
        
        if (widthLevel == Road.WidthLevel.RESIDENTIAL_ROAD || widthLevel == Road.WidthLevel.NATURAL_PATH) {
            // 居民区或自然路径倾向于乡村风格
            if (rand < 0.6) {
                return BenchStyle.RUSTIC;
            } else if (rand < 0.8) {
                return BenchStyle.PARK;
            } else {
                return BenchStyle.MODERN;
            }
        } else if (widthLevel == Road.WidthLevel.MAIN_ROAD || widthLevel == Road.WidthLevel.SECONDARY_ROAD) {
            // 主干道和次干道倾向于现代或公园风格
            if (rand < 0.4) {
                return BenchStyle.MODERN;
            } else if (rand < 0.7) {
                return BenchStyle.PARK;
            } else {
                return BenchStyle.RUSTIC;
            }
        } else {
            // 高速公路或其他道路使用现代风格
            return BenchStyle.MODERN;
        }
    }
    
    /**
     * 生成长椅结构
     */
    private void generateBenchStructure(List<BlockPlacement> placements, LevelAccessor level, BlockPos pos, BenchStyle style, Road.Direction direction) {
        BlockPos basePos = pos;
        
        // 根据方向确定长椅朝向
        boolean isHorizontal = direction == Road.Direction.EAST_WEST || direction == Road.Direction.WEST_EAST;
        
        // 生成长椅支撑
        generateBenchSupports(placements, basePos, style, isHorizontal);
        
        // 生成长椅座位
        generateBenchSeat(placements, basePos, style, isHorizontal);
        
        // 生成长椅靠背（如果有）
        if (style.hasBackrest()) {
            generateBenchBackrest(placements, basePos, style, isHorizontal);
        }
    }
    
    /**
     * 生成长椅支撑
     */
    private void generateBenchSupports(List<BlockPlacement> placements, BlockPos basePos, BenchStyle style, boolean isHorizontal) {
        BlockPos support1, support2;
        
        if (isHorizontal) {
            // 水平方向的长椅
            support1 = basePos.offset(-1, 0, 0);
            support2 = basePos.offset(1, 0, 0);
        } else {
            // 垂直方向的长椅
            support1 = basePos.offset(0, 0, -1);
            support2 = basePos.offset(0, 0, 1);
        }
        
        // 放置支撑柱
        placements.add(new BlockPlacement(support1, style.getSupportBlock()));
        placements.add(new BlockPlacement(support2, style.getSupportBlock()));
        
        // 添加额外的支撑结构
        if (style == BenchStyle.RUSTIC) {
            // 乡村风格添加原木支撑
            placements.add(new BlockPlacement(support1.offset(0, 1, 0), style.getSupportBlock()));
            placements.add(new BlockPlacement(support2.offset(0, 1, 0), style.getSupportBlock()));
        }
    }
    
    /**
     * 生成长椅座位
     */
    private void generateBenchSeat(List<BlockPlacement> placements, BlockPos basePos, BenchStyle style, boolean isHorizontal) {
        BlockPos seatPos = basePos.above();
        
        if (isHorizontal) {
            // 水平方向的座位（3格长）
            for (int i = -1; i <= 1; i++) {
                placements.add(new BlockPlacement(seatPos.offset(i, 0, 0), style.getSeatBlock()));
            }
        } else {
            // 垂直方向的座位（3格长）
            for (int i = -1; i <= 1; i++) {
                placements.add(new BlockPlacement(seatPos.offset(0, 0, i), style.getSeatBlock()));
            }
        }
    }
    
    /**
     * 生成长椅靠背
     */
    private void generateBenchBackrest(List<BlockPlacement> placements, BlockPos basePos, BenchStyle style, boolean isHorizontal) {
        BlockPos backrestPos = basePos.above(2);
        
        if (isHorizontal) {
            // 水平方向的靠背
            for (int i = -1; i <= 1; i++) {
                placements.add(new BlockPlacement(backrestPos.offset(i, 0, 0), style.getBackrestBlock()));
            }
        } else {
            // 垂直方向的靠背
            for (int i = -1; i <= 1; i++) {
                placements.add(new BlockPlacement(backrestPos.offset(0, 0, i), style.getBackrestBlock()));
            }
        }
    }
    
    /**
     * 添加周围装饰
     */
    private void addSurroundingDecorations(List<BlockPlacement> placements, LevelAccessor level, BlockPos pos, BenchStyle style) {
        // 在长椅周围添加一些装饰性方块
        if (random.nextDouble() < 0.3) {
            // 30%概率在长椅一侧添加花坛
            BlockPos flowerPos = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
            if (level.isEmptyBlock(flowerPos) && level.getBlockState(flowerPos.below()).isSolid()) {
                placements.add(new BlockPlacement(flowerPos, Blocks.POTTED_POPPY.defaultBlockState()));
            }
        }
        
        if (random.nextDouble() < 0.2) {
            // 20%概率在长椅周围添加灯笼
            BlockPos lanternPos = pos.offset(random.nextInt(5) - 2, 1, random.nextInt(5) - 2);
            if (level.isEmptyBlock(lanternPos) && level.getBlockState(lanternPos.below()).isSolid()) {
                placements.add(new BlockPlacement(lanternPos, Blocks.LANTERN.defaultBlockState()));
            }
        }
    }
    
    /**
     * 长椅风格枚举
     */
    public enum BenchStyle {
        PARK(true, Blocks.OAK_PLANKS, Blocks.OAK_FENCE, Blocks.OAK_FENCE),
        MODERN(true, Blocks.SMOOTH_STONE, Blocks.IRON_BARS, Blocks.IRON_BARS),
        RUSTIC(false, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE);
        
        private final boolean hasBackrest;
        private final net.minecraft.world.level.block.Block seatBlock;
        private final net.minecraft.world.level.block.Block supportBlock;
        private final net.minecraft.world.level.block.Block backrestBlock;
        
        BenchStyle(boolean hasBackrest, net.minecraft.world.level.block.Block seatBlock, 
                  net.minecraft.world.level.block.Block supportBlock, net.minecraft.world.level.block.Block backrestBlock) {
            this.hasBackrest = hasBackrest;
            this.seatBlock = seatBlock;
            this.supportBlock = supportBlock;
            this.backrestBlock = backrestBlock;
        }
        
        public boolean hasBackrest() {
            return hasBackrest;
        }
        
        public net.minecraft.world.level.block.state.BlockState getSeatBlock() {
            return seatBlock.defaultBlockState();
        }
        
        public net.minecraft.world.level.block.state.BlockState getSupportBlock() {
            return supportBlock.defaultBlockState();
        }
        
        public net.minecraft.world.level.block.state.BlockState getBackrestBlock() {
            return backrestBlock.defaultBlockState();
        }
    }
}
