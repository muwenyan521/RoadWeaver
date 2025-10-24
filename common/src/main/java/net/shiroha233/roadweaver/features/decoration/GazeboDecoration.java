package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.decoration.config.DecorationType;
import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.Random;

/**
 * 凉亭装饰实现
 * 在道路旁生成优雅的凉亭结构，提供休息和观景功能
 */
public class GazeboDecoration implements Decoration {
    
    @Override
    public DecorationType getType() {
        return DecorationType.GAZEBO;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 凉亭需要较大的平坦区域
        return checkFlatArea(level, pos, 5, 5) && 
               hasGoodView(level, pos) &&
               !isTooCloseToWater(level, pos, 3);
    }
    
    @Override
    public void generate(LevelAccessor level, BlockPos pos, Road road, Random random) {
        BlockPos centerPos = pos.offset(2, 0, 2);
        
        // 生成凉亭基础平台 (5x5)
        generatePlatform(level, centerPos, 5, Blocks.SMOOTH_STONE_SLAB);
        
        // 生成凉亭柱子
        generatePillars(level, centerPos, random);
        
        // 生成凉亭屋顶
        generateRoof(level, centerPos, random);
        
        // 生成凉亭座椅
        generateSeats(level, centerPos, random);
        
        // 生成装饰性元素
        generateDecorations(level, centerPos, random);
    }
    
    /**
     * 生成凉亭柱子
     */
    private void generatePillars(LevelAccessor level, BlockPos centerPos, Random random) {
        // 四个角落柱子
        BlockPos[] pillarPositions = {
            centerPos.offset(-2, 0, -2),
            centerPos.offset(2, 0, -2),
            centerPos.offset(-2, 0, 2),
            centerPos.offset(2, 0, 2)
        };
        
        for (BlockPos pillarPos : pillarPositions) {
            // 生成3格高的柱子
            for (int y = 1; y <= 3; y++) {
                BlockPos currentPos = pillarPos.above(y);
                if (random.nextFloat() < 0.7f) {
                    level.setBlock(currentPos, Blocks.SPRUCE_LOG.defaultBlockState(), 3);
                } else {
                    level.setBlock(currentPos, Blocks.OAK_LOG.defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 生成凉亭屋顶
     */
    private void generateRoof(LevelAccessor level, BlockPos centerPos, Random random) {
        // 屋顶层1 (y=4)
        generateRoofLayer(level, centerPos, 4, 3, random);
        
        // 屋顶层2 (y=5)
        generateRoofLayer(level, centerPos, 5, 2, random);
        
        // 屋顶层3 (y=6)
        generateRoofLayer(level, centerPos, 6, 1, random);
        
        // 屋顶尖顶 (y=7)
        level.setBlock(centerPos.above(7), Blocks.SPRUCE_STAIRS.defaultBlockState(), 3);
    }
    
    /**
     * 生成屋顶层
     */
    private void generateRoofLayer(LevelAccessor level, BlockPos centerPos, int yLevel, int radius, Random random) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) + Math.abs(z) <= radius + 1) {
                    BlockPos roofPos = centerPos.offset(x, yLevel, z);
                    if (random.nextFloat() < 0.8f) {
                        level.setBlock(roofPos, Blocks.SPRUCE_STAIRS.defaultBlockState(), 3);
                    } else {
                        level.setBlock(roofPos, Blocks.OAK_STAIRS.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * 生成凉亭座椅
     */
    private void generateSeats(LevelAccessor level, BlockPos centerPos, Random random) {
        BlockPos[] seatPositions = {
            centerPos.offset(-1, 1, 0),
            centerPos.offset(1, 1, 0),
            centerPos.offset(0, 1, -1),
            centerPos.offset(0, 1, 1)
        };
        
        for (BlockPos seatPos : seatPositions) {
            level.setBlock(seatPos, Blocks.SPRUCE_STAIRS.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成装饰性元素
     */
    private void generateDecorations(LevelAccessor level, BlockPos centerPos, Random random) {
        // 50%概率生成悬挂灯笼
        if (random.nextBoolean()) {
            BlockPos lanternPos = centerPos.above(4);
            level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState(), 3);
        }
        
        // 30%概率生成花坛
        if (random.nextFloat() < 0.3f) {
            generateSmallFlowerBed(level, centerPos.offset(-3, 0, 0), random);
        }
        
        // 30%概率生成另一个花坛
        if (random.nextFloat() < 0.3f) {
            generateSmallFlowerBed(level, centerPos.offset(3, 0, 0), random);
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
     * 检查是否有良好视野
     */
    private boolean hasGoodView(LevelAccessor level, BlockPos pos) {
        // 检查周围是否有阻挡视野的高大结构
        int viewDistance = 10;
        for (int x = -viewDistance; x <= viewDistance; x += 5) {
            for (int z = -viewDistance; z <= viewDistance; z += 5) {
                if (x != 0 || z != 0) {
                    BlockPos checkPos = pos.offset(x, 0, z);
                    int terrainHeight = level.getHeight(checkPos.getX(), checkPos.getZ());
                    if (terrainHeight > pos.getY() + 5) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 检查是否离水太近
     */
    private boolean isTooCloseToWater(LevelAccessor level, BlockPos pos, int minDistance) {
        for (int x = -minDistance; x <= minDistance; x++) {
            for (int z = -minDistance; z <= minDistance; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (level.getBlockState(checkPos).getBlock() == Blocks.WATER) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 生成平台
     */
    private void generatePlatform(LevelAccessor level, BlockPos centerPos, int size, net.minecraft.world.level.block.Block block) {
        int halfSize = size / 2;
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                BlockPos platformPos = centerPos.offset(x, 0, z);
                level.setBlock(platformPos, block.defaultBlockState(), 3);
            }
        }
    }
}
