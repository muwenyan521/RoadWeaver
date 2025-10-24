package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.decoration.config.DecorationType;
import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.Random;

/**
 * 公交站装饰实现
 * 在道路旁生成公交站，包含候车亭、座椅和信息牌
 */
public class BusStopDecoration implements Decoration {
    
    @Override
    public DecorationType getType() {
        return DecorationType.BUS_STOP;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 公交站需要靠近道路，并且有足够的空间
        return isCloseToRoad(pos, road) && 
               checkFlatArea(level, pos, 3, 2) &&
               !isTooCloseToOtherBusStop(level, pos, 15);
    }
    
    @Override
    public void generate(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 选择公交站风格
        BusStopStyle busStopStyle = BusStopStyle.values()[random.nextInt(BusStopStyle.values().length)];
        
        // 生成公交站基础
        generateBusStopFoundation(level, pos, random);
        
        // 根据风格生成公交站
        switch (busStopStyle) {
            case MODERN:
                generateModernBusStop(level, pos, random);
                break;
            case CLASSIC:
                generateClassicBusStop(level, pos, random);
                break;
            case RUSTIC:
                generateRusticBusStop(level, pos, random);
                break;
        }
        
        // 生成公交站座椅
        generateBusStopSeats(level, pos, random);
        
        // 生成公交站信息牌
        generateBusStopSign(level, pos, random);
        
        // 生成公交站周围的装饰
        generateBusStopSurroundings(level, pos, random);
    }
    
    /**
     * 公交站风格枚举
     */
    private enum BusStopStyle {
        MODERN,   // 现代风格
        CLASSIC,  // 经典风格
        RUSTIC    // 乡村风格
    }
    
    /**
     * 生成公交站基础
     */
    private void generateBusStopFoundation(LevelAccessor level, BlockPos pos, Random random) {
        // 生成3x2基础平台
        for (int x = -1; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                BlockPos basePos = pos.offset(x, 0, z);
                if (random.nextFloat() < 0.8f) {
                    level.setBlock(basePos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                } else {
                    level.setBlock(basePos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 生成现代风格公交站
     */
    private void generateModernBusStop(LevelAccessor level, BlockPos pos, Random random) {
        // 生成现代风格顶棚
        for (int x = -1; x <= 1; x++) {
            BlockPos roofPos = pos.offset(x, 3, 1);
            if (random.nextFloat() < 0.7f) {
                level.setBlock(roofPos, Blocks.QUARTZ_SLAB.defaultBlockState(), 3);
            } else {
                level.setBlock(roofPos, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 3);
            }
        }
        
        // 生成现代风格支撑柱
        BlockPos[] supportPositions = {
            pos.offset(-1, 1, 1),
            pos.offset(1, 1, 1)
        };
        
        for (BlockPos supportPos : supportPositions) {
            for (int y = 1; y <= 2; y++) {
                BlockPos currentPos = supportPos.above(y);
                level.setBlock(currentPos, Blocks.QUARTZ_PILLAR.defaultBlockState(), 3);
            }
        }
        
        // 生成玻璃墙
        for (int y = 1; y <= 2; y++) {
            BlockPos glassPos = pos.offset(0, y, 1);
            level.setBlock(glassPos, Blocks.GLASS_PANE.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成经典风格公交站
     */
    private void generateClassicBusStop(LevelAccessor level, BlockPos pos, Random random) {
        // 生成经典风格顶棚
        for (int x = -1; x <= 1; x++) {
            BlockPos roofPos = pos.offset(x, 3, 1);
            level.setBlock(roofPos, Blocks.OAK_SLAB.defaultBlockState(), 3);
        }
        
        // 生成经典风格支撑柱
        BlockPos[] supportPositions = {
            pos.offset(-1, 1, 1),
            pos.offset(1, 1, 1)
        };
        
        for (BlockPos supportPos : supportPositions) {
            for (int y = 1; y <= 2; y++) {
                BlockPos currentPos = supportPos.above(y);
                level.setBlock(currentPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
            }
        }
        
        // 生成木制装饰
        for (int y = 1; y <= 2; y++) {
            BlockPos woodPos = pos.offset(0, y, 1);
            level.setBlock(woodPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成乡村风格公交站
     */
    private void generateRusticBusStop(LevelAccessor level, BlockPos pos, Random random) {
        // 生成乡村风格顶棚
        for (int x = -1; x <= 1; x++) {
            BlockPos roofPos = pos.offset(x, 3, 1);
            if (random.nextBoolean()) {
                level.setBlock(roofPos, Blocks.SPRUCE_SLAB.defaultBlockState(), 3);
            } else {
                level.setBlock(roofPos, Blocks.OAK_SLAB.defaultBlockState(), 3);
            }
        }
        
        // 生成乡村风格支撑柱
        BlockPos[] supportPositions = {
            pos.offset(-1, 1, 1),
            pos.offset(1, 1, 1)
        };
        
        for (BlockPos supportPos : supportPositions) {
            for (int y = 1; y <= 2; y++) {
                BlockPos currentPos = supportPos.above(y);
                if (random.nextFloat() < 0.7f) {
                    level.setBlock(currentPos, Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
                } else {
                    level.setBlock(currentPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                }
            }
        }
        
        // 生成乡村风格装饰
        for (int y = 1; y <= 2; y++) {
            BlockPos decorPos = pos.offset(0, y, 1);
            level.setBlock(decorPos, Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成公交站座椅
     */
    private void generateBusStopSeats(LevelAccessor level, BlockPos pos, Random random) {
        // 在公交站内生成座椅
        BlockPos[] seatPositions = {
            pos.offset(-1, 1, 0),
            pos.offset(1, 1, 0)
        };
        
        for (BlockPos seatPos : seatPositions) {
            if (random.nextFloat() < 0.8f) {
                level.setBlock(seatPos, Blocks.OAK_STAIRS.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成公交站信息牌
     */
    private void generateBusStopSign(LevelAccessor level, BlockPos pos, Random random) {
        // 生成信息牌支撑柱
        BlockPos signPostPos = pos.offset(0, 1, 0);
        level.setBlock(signPostPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
        
        // 生成信息牌
        BlockPos signPos = signPostPos.above();
        level.setBlock(signPos, Blocks.OAK_SIGN.defaultBlockState(), 3);
        
        // 在信息牌旁生成小灯笼
        if (random.nextFloat() < 0.6f) {
            BlockPos lanternPos = signPos.offset(1, 0, 0);
            level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成公交站周围的装饰
     */
    private void generateBusStopSurroundings(LevelAccessor level, BlockPos pos, Random random) {
        // 40%概率在公交站旁生成垃圾桶
        if (random.nextFloat() < 0.4f) {
            BlockPos trashPos = pos.offset(2, 0, 0);
            level.setBlock(trashPos, Blocks.CAULDRON.defaultBlockState(), 3);
        }
        
        // 30%概率在公交站旁生成小花坛
        if (random.nextFloat() < 0.3f) {
            BlockPos flowerPos = pos.offset(-2, 0, 0);
            level.setBlock(flowerPos, Blocks.FLOWER_POT.defaultBlockState(), 3);
            
            // 在花盆中随机放置花朵
            switch (random.nextInt(5)) {
                case 0:
                    level.setBlock(flowerPos.above(), Blocks.POPPY.defaultBlockState(), 3);
                    break;
                case 1:
                    level.setBlock(flowerPos.above(), Blocks.DANDELION.defaultBlockState(), 3);
                    break;
                case 2:
                    level.setBlock(flowerPos.above(), Blocks.BLUE_ORCHID.defaultBlockState(), 3);
                    break;
                case 3:
                    level.setBlock(flowerPos.above(), Blocks.ALLIUM.defaultBlockState(), 3);
                    break;
                case 4:
                    level.setBlock(flowerPos.above(), Blocks.AZURE_BLUET.defaultBlockState(), 3);
                    break;
            }
        }
    }
    
    /**
     * 检查是否靠近道路
     */
    private boolean isCloseToRoad(BlockPos pos, Road road) {
        // 检查位置是否在道路附近（距离道路中心不超过5格）
        return road.getDistanceToRoad(pos) <= 5;
    }
    
    /**
     * 检查平坦区域
     */
    private boolean checkFlatArea(LevelAccessor level, BlockPos pos, int width, int depth) {
        int baseY = pos.getY();
        
        for (int x = -width/2; x <= width/2; x++) {
            for (int z = -depth/2; z <= depth/2; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                int terrainY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, checkPos.getX(), checkPos.getZ());
                
                if (Math.abs(terrainY - baseY) > 1) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 检查是否太靠近其他公交站
     */
    private boolean isTooCloseToOtherBusStop(LevelAccessor level, BlockPos pos, int minDistance) {
        // 这里可以集成现有的装饰检测系统
        // 暂时返回false，假设没有冲突
        return false;
    }
}
