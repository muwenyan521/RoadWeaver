package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.decoration.config.DecorationType;
import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.Random;

/**
 * 小喷泉装饰实现
 * 在道路旁生成小型喷泉，提供视觉美感和休闲氛围
 */
public class SmallFountainDecoration implements Decoration {
    
    @Override
    public DecorationType getType() {
        return DecorationType.SMALL_FOUNTAIN;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 小喷泉需要平坦的地面，并且最好靠近道路
        return checkFlatArea(level, pos, 3, 3) && 
               isCloseToRoad(level, pos, road, 5);
    }
    
    @Override
    public void generate(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 选择喷泉类型
        FountainType fountainType = FountainType.values()[random.nextInt(FountainType.values().length)];
        
        // 生成喷泉基础
        generateFountainBase(level, pos, random);
        
        // 根据类型生成喷泉主体
        switch (fountainType) {
            case CIRCULAR:
                generateCircularFountain(level, pos, random);
                break;
            case SQUARE:
                generateSquareFountain(level, pos, random);
                break;
            case TIERED:
                generateTieredFountain(level, pos, random);
                break;
        }
        
        // 生成喷泉周围的装饰
        generateFountainSurroundings(level, pos, random);
    }
    
    /**
     * 喷泉类型枚举
     */
    private enum FountainType {
        CIRCULAR,   // 圆形喷泉
        SQUARE,     // 方形喷泉
        TIERED      // 分层喷泉
    }
    
    /**
     * 生成喷泉基础
     */
    private void generateFountainBase(LevelAccessor level, BlockPos pos, Random random) {
        // 生成3x3基础平台
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
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
     * 生成圆形喷泉
     */
    private void generateCircularFountain(LevelAccessor level, BlockPos pos, Random random) {
        // 生成圆形水池
        generateCircularPool(level, pos, 3, random);
        
        // 生成中心喷泉柱
        BlockPos centerPos = pos;
        for (int y = 1; y <= 2; y++) {
            BlockPos columnPos = centerPos.above(y);
            if (y == 1) {
                level.setBlock(columnPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            } else {
                level.setBlock(columnPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            }
        }
        
        // 在喷泉柱顶部生成水
        BlockPos waterPos = centerPos.above(3);
        level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
        
        // 40%概率在喷泉周围生成小装饰
        if (random.nextFloat() < 0.4f) {
            generateCircularDecorations(level, pos, random);
        }
    }
    
    /**
     * 生成方形喷泉
     */
    private void generateSquareFountain(LevelAccessor level, BlockPos pos, Random random) {
        // 生成方形水池
        generateSquarePool(level, pos, 3, random);
        
        // 生成方形喷泉结构
        BlockPos centerPos = pos;
        for (int y = 1; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) == 1 || Math.abs(z) == 1) { // 只生成边框
                        BlockPos structurePos = centerPos.offset(x, y, z);
                        if (y < 3) {
                            level.setBlock(structurePos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                        } else {
                            level.setBlock(structurePos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        
        // 在喷泉中心生成水
        BlockPos waterPos = centerPos.above(1);
        level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
        
        // 在顶层生成水
        BlockPos topWaterPos = centerPos.above(3);
        level.setBlock(topWaterPos, Blocks.WATER.defaultBlockState(), 3);
    }
    
    /**
     * 生成分层喷泉
     */
    private void generateTieredFountain(LevelAccessor level, BlockPos pos, Random random) {
        // 生成底层水池
        generateCircularPool(level, pos, 4, random);
        
        // 生成第一层喷泉
        BlockPos firstTierPos = pos.above(1);
        generateCircularPool(level, firstTierPos, 2, random);
        
        // 生成第二层喷泉
        BlockPos secondTierPos = pos.above(2);
        generateCircularPool(level, secondTierPos, 1, random);
        
        // 在顶层生成水柱
        BlockPos topPos = pos.above(3);
        level.setBlock(topPos, Blocks.WATER.defaultBlockState(), 3);
        
        // 连接各层的水流
        connectFountainTiers(level, pos, random);
    }
    
    /**
     * 生成圆形水池
     */
    private void generateCircularPool(LevelAccessor level, BlockPos centerPos, int radius, Random random) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x*x + z*z);
                if (distance <= radius) {
                    BlockPos poolPos = centerPos.offset(x, 0, z);
                    
                    if (distance <= radius - 0.5) {
                        // 水池内部 - 水
                        level.setBlock(poolPos, Blocks.WATER.defaultBlockState(), 3);
                    } else {
                        // 水池边缘 - 石头
                        if (random.nextFloat() < 0.7f) {
                            level.setBlock(poolPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                        } else {
                            level.setBlock(poolPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 生成方形水池
     */
    private void generateSquarePool(LevelAccessor level, BlockPos centerPos, int size, Random random) {
        int halfSize = size / 2;
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                BlockPos poolPos = centerPos.offset(x, 0, z);
                
                if (Math.abs(x) < halfSize && Math.abs(z) < halfSize) {
                    // 水池内部 - 水
                    level.setBlock(poolPos, Blocks.WATER.defaultBlockState(), 3);
                } else {
                    // 水池边缘 - 石头
                    if (random.nextFloat() < 0.7f) {
                        level.setBlock(poolPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(poolPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * 生成圆形装饰
     */
    private void generateCircularDecorations(LevelAccessor level, BlockPos centerPos, Random random) {
        int decorationRadius = 4;
        for (int x = -decorationRadius; x <= decorationRadius; x++) {
            for (int z = -decorationRadius; z <= decorationRadius; z++) {
                double distance = Math.sqrt(x*x + z*z);
                if (Math.abs(distance - decorationRadius) < 0.5) {
                    BlockPos decorationPos = centerPos.offset(x, 0, z);
                    
                    // 30%概率生成装饰性方块
                    if (random.nextFloat() < 0.3f) {
                        if (random.nextBoolean()) {
                            level.setBlock(decorationPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                        } else {
                            level.setBlock(decorationPos, Blocks.POLISHED_ANDESITE_SLAB.defaultBlockState(), 3);
                        }
                    }
                    
                    // 20%概率生成花坛
                    if (random.nextFloat() < 0.2f) {
                        generateSmallFlowerBed(level, decorationPos, random);
                    }
                }
            }
        }
    }
    
    /**
     * 连接喷泉分层
     */
    private void connectFountainTiers(LevelAccessor level, BlockPos centerPos, Random random) {
        // 在分层喷泉的边缘创建水流连接
        int[] tierRadii = {4, 2, 1};
        
        for (int tier = 0; tier < tierRadii.length - 1; tier++) {
            int currentRadius = tierRadii[tier];
            int nextRadius = tierRadii[tier + 1];
            
            // 在四个主要方向创建水流通道
            for (int direction = 0; direction < 4; direction++) {
                int xOffset = 0, zOffset = 0;
                switch (direction) {
                    case 0: xOffset = 1; break;  // 东
                    case 1: xOffset = -1; break; // 西
                    case 2: zOffset = 1; break;  // 南
                    case 3: zOffset = -1; break; // 北
                }
                
                // 创建水流通道
                for (int step = nextRadius + 1; step <= currentRadius; step++) {
                    BlockPos waterPos = centerPos.offset(xOffset * step, tier + 1, zOffset * step);
                    level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 生成喷泉周围的装饰
     */
    private void generateFountainSurroundings(LevelAccessor level, BlockPos pos, Random random) {
        // 50%概率在喷泉周围生成座椅
        if (random.nextBoolean()) {
            generateFountainSeats(level, pos, random);
        }
        
        // 40%概率在喷泉旁生成灯笼
        if (random.nextFloat() < 0.4f) {
            BlockPos[] lanternPositions = {
                pos.offset(4, 0, 0),
                pos.offset(-4, 0, 0),
                pos.offset(0, 0, 4),
                pos.offset(0, 0, -4)
            };
            
            for (BlockPos lanternPos : lanternPositions) {
                if (random.nextFloat() < 0.5f) {
                    level.setBlock(lanternPos.above(), Blocks.LANTERN.defaultBlockState(), 3);
                }
            }
        }
        
        // 60%概率在喷泉周围生成花坛
        if (random.nextFloat() < 0.6f) {
            generateSurroundingFlowerBeds(level, pos, random);
        }
    }
    
    /**
     * 生成喷泉座椅
     */
    private void generateFountainSeats(LevelAccessor level, BlockPos pos, Random random) {
        BlockPos[] seatPositions = {
            pos.offset(3, 0, 3),
            pos.offset(-3, 0, 3),
            pos.offset(3, 0, -3),
            pos.offset(-3, 0, -3)
        };
        
        for (BlockPos seatPos : seatPositions) {
            if (random.nextFloat() < 0.7f) {
                // 生成座椅（楼梯方块）
                if (random.nextBoolean()) {
                    level.setBlock(seatPos, Blocks.SPRUCE_STAIRS.defaultBlockState(), 3);
                } else {
                    level.setBlock(seatPos, Blocks.OAK_STAIRS.defaultBlockState(), 3);
                }
                
                // 生成座椅支撑
                BlockPos supportPos = seatPos.below();
                level.setBlock(supportPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成周围花坛
     */
    private void generateSurroundingFlowerBeds(LevelAccessor level, BlockPos pos, Random random) {
        BlockPos[] flowerBedPositions = {
            pos.offset(5, 0, 0),
            pos.offset(-5, 0, 0),
            pos.offset(0, 0, 5),
            pos.offset(0, 0, -5),
            pos.offset(4, 0, 4),
            pos.offset(-4, 0, 4),
            pos.offset(4, 0, -4),
            pos.offset(-4, 0, -4)
        };
        
        for (BlockPos flowerBedPos : flowerBedPositions) {
            if (random.nextFloat() < 0.4f) {
                generateSmallFlowerBed(level, flowerBedPos, random);
            }
        }
    }
    
    /**
     * 生成小花坛
     */
    private void generateSmallFlowerBed(LevelAccessor level, BlockPos pos, Random random) {
        // 2x2花坛
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                BlockPos flowerPos = pos.offset(x, 0, z);
                level.setBlock(flowerPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                
                // 在花坛上放置花朵
                if (random.nextFloat() < 0.6f) {
                    BlockPos flowerTopPos = flowerPos.above();
                    level.setBlock(flowerTopPos, getRandomFlower(random), 3);
                }
            }
        }
    }
    
    /**
     * 获取随机花朵
     */
    private net.minecraft.world.level.block.state.BlockState getRandomFlower(Random random) {
        net.minecraft.world.level.block.Block[] flowers = {
            Blocks.POPPY,
            Blocks.DANDELION,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP
        };
        return flowers[random.nextInt(flowers.length)].defaultBlockState();
    }
    
    /**
     * 检查平坦区域
     */
    private boolean checkFlatArea(LevelAccessor level, BlockPos pos, int width, int depth) {
        int baseY = pos.getY();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (Math.abs(level.getHeight(checkPos.getX(), checkPos.getZ()) - baseY) > 1) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 检查是否靠近道路
     */
    private boolean isCloseToRoad(LevelAccessor level, BlockPos pos, Road road, int maxDistance) {
        // 检查位置是否在道路附近
        return road != null && road.getDistanceToRoad(pos) <= maxDistance;
    }
}
