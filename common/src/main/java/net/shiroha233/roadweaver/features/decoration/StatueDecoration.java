package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.decoration.config.DecorationType;
import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.Random;

/**
 * 雕像装饰实现
 * 在道路旁生成各种雕像，增加艺术和文化氛围
 */
public class StatueDecoration implements Decoration {
    
    @Override
    public DecorationType getType() {
        return DecorationType.STATUE;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 雕像需要平坦的地面，并且不能太靠近其他大型结构
        return checkFlatArea(level, pos, 3, 3) && 
               !isTooCloseToLargeStructure(level, pos, 8);
    }
    
    @Override
    public void generate(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 选择雕像类型
        StatueType statueType = StatueType.values()[random.nextInt(StatueType.values().length)];
        
        // 生成雕像基座
        generateStatueBase(level, pos, random);
        
        // 根据类型生成雕像主体
        switch (statueType) {
            case PILLAR:
                generatePillarStatue(level, pos, random);
                break;
            case OBELISK:
                generateObeliskStatue(level, pos, random);
                break;
            case FIGURE:
                generateFigureStatue(level, pos, random);
                break;
            case ANIMAL:
                generateAnimalStatue(level, pos, random);
                break;
        }
        
        // 生成雕像周围的装饰
        generateStatueSurroundings(level, pos, random);
    }
    
    /**
     * 雕像类型枚举
     */
    private enum StatueType {
        PILLAR,     // 石柱雕像
        OBELISK,    // 方尖碑
        FIGURE,     // 人物雕像
        ANIMAL      // 动物雕像
    }
    
    /**
     * 生成雕像基座
     */
    private void generateStatueBase(LevelAccessor level, BlockPos pos, Random random) {
        // 3x3基座
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos basePos = pos.offset(x, 0, z);
                if (random.nextFloat() < 0.7f) {
                    level.setBlock(basePos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                } else {
                    level.setBlock(basePos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                }
            }
        }
        
        // 中心基座加高
        for (int y = 1; y <= 2; y++) {
            BlockPos centerBasePos = pos.above(y);
            if (y == 1) {
                level.setBlock(centerBasePos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            } else {
                level.setBlock(centerBasePos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成石柱雕像
     */
    private void generatePillarStatue(LevelAccessor level, BlockPos pos, Random random) {
        int pillarHeight = 4 + random.nextInt(3); // 4-6格高
        
        // 生成石柱主体
        for (int y = 3; y < 3 + pillarHeight; y++) {
            BlockPos pillarPos = pos.above(y);
            if (random.nextFloat() < 0.8f) {
                level.setBlock(pillarPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            } else {
                level.setBlock(pillarPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
            }
        }
        
        // 生成石柱顶部装饰
        BlockPos topPos = pos.above(3 + pillarHeight);
        if (random.nextBoolean()) {
            // 球体顶部
            level.setBlock(topPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
        } else {
            // 金字塔顶部
            level.setBlock(topPos, Blocks.SMOOTH_STONE_STAIRS.defaultBlockState(), 3);
        }
        
        // 40%概率在石柱上添加横向装饰
        if (random.nextFloat() < 0.4f) {
            int decorationHeight = 3 + random.nextInt(pillarHeight - 2);
            BlockPos decorationPos = pos.above(3 + decorationHeight);
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(z) == 1) { // 只生成十字形
                        BlockPos crossPos = decorationPos.offset(x, 0, z);
                        level.setBlock(crossPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * 生成方尖碑雕像
     */
    private void generateObeliskStatue(LevelAccessor level, BlockPos pos, Random random) {
        int obeliskHeight = 6 + random.nextInt(4); // 6-9格高
        
        // 生成方尖碑主体（逐渐变细）
        for (int y = 3; y < 3 + obeliskHeight; y++) {
            int layerWidth = Math.max(1, 3 - (y - 3) / 2); // 逐渐变细
            
            for (int x = -layerWidth; x <= layerWidth; x++) {
                for (int z = -layerWidth; z <= layerWidth; z++) {
                    if (Math.abs(x) <= layerWidth && Math.abs(z) <= layerWidth) {
                        BlockPos obeliskPos = pos.offset(x, y, z);
                        if (random.nextFloat() < 0.9f) {
                            level.setBlock(obeliskPos, Blocks.POLISHED_DIORITE.defaultBlockState(), 3);
                        } else {
                            level.setBlock(obeliskPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        
        // 生成方尖碑尖顶
        BlockPos spirePos = pos.above(3 + obeliskHeight);
        level.setBlock(spirePos, Blocks.POLISHED_DIORITE_STAIRS.defaultBlockState(), 3);
    }
    
    /**
     * 生成人物雕像
     */
    private void generateFigureStatue(LevelAccessor level, BlockPos pos, Random random) {
        // 雕像身体（3格高）
        for (int y = 3; y <= 5; y++) {
            BlockPos bodyPos = pos.above(y);
            if (y == 3) {
                // 身体底部
                level.setBlock(bodyPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            } else if (y == 4) {
                // 身体中部
                level.setBlock(bodyPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                // 添加手臂
                level.setBlock(bodyPos.offset(1, 0, 0), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                level.setBlock(bodyPos.offset(-1, 0, 0), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            } else {
                // 头部
                level.setBlock(bodyPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
        
        // 60%概率添加头部装饰
        if (random.nextFloat() < 0.6f) {
            BlockPos headPos = pos.above(6);
            if (random.nextBoolean()) {
                level.setBlock(headPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            } else {
                level.setBlock(headPos, Blocks.SMOOTH_STONE_STAIRS.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成动物雕像
     */
    private void generateAnimalStatue(LevelAccessor level, BlockPos pos, Random random) {
        // 动物身体（2格高）
        for (int y = 3; y <= 4; y++) {
            BlockPos bodyPos = pos.above(y);
            level.setBlock(bodyPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            
            // 在身体周围添加特征
            if (y == 3) {
                // 腿部/底座
                level.setBlock(bodyPos.offset(1, 0, 1), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                level.setBlock(bodyPos.offset(-1, 0, 1), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                level.setBlock(bodyPos.offset(1, 0, -1), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                level.setBlock(bodyPos.offset(-1, 0, -1), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
            } else {
                // 头部/特征
                if (random.nextBoolean()) {
                    // 向前延伸的头部
                    level.setBlock(bodyPos.offset(0, 0, 1), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                } else {
                    // 向上延伸的颈部
                    level.setBlock(bodyPos.above(), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                }
            }
        }
        
        // 30%概率添加尾巴
        if (random.nextFloat() < 0.3f) {
            BlockPos tailPos = pos.above(3).offset(0, 0, -1);
            level.setBlock(tailPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成雕像周围的装饰
     */
    private void generateStatueSurroundings(LevelAccessor level, BlockPos pos, Random random) {
        // 50%概率在雕像周围生成小花坛
        if (random.nextBoolean()) {
            BlockPos[] flowerBedPositions = {
                pos.offset(3, 0, 0),
                pos.offset(-3, 0, 0),
                pos.offset(0, 0, 3),
                pos.offset(0, 0, -3)
            };
            
            for (BlockPos flowerBedPos : flowerBedPositions) {
                if (random.nextFloat() < 0.5f) {
                    generateSmallFlowerBed(level, flowerBedPos, random);
                }
            }
        }
        
        // 30%概率在雕像旁生成灯笼
        if (random.nextFloat() < 0.3f) {
            BlockPos lanternPos = pos.offset(2, 0, 2);
            level.setBlock(lanternPos.above(), Blocks.LANTERN.defaultBlockState(), 3);
        }
        
        // 20%概率在雕像基座周围生成矮墙
        if (random.nextFloat() < 0.2f) {
            generateLowWall(level, pos, random);
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
                if (random.nextFloat() < 0.7f) {
                    BlockPos flowerTopPos = flowerPos.above();
                    level.setBlock(flowerTopPos, getRandomFlower(random), 3);
                }
            }
        }
    }
    
    /**
     * 生成矮墙
     */
    private void generateLowWall(LevelAccessor level, BlockPos pos, Random random) {
        // 在雕像周围生成矮墙
        int wallRadius = 4;
        for (int x = -wallRadius; x <= wallRadius; x++) {
            for (int z = -wallRadius; z <= wallRadius; z++) {
                if (Math.abs(x) == wallRadius || Math.abs(z) == wallRadius) {
                    BlockPos wallPos = pos.offset(x, 0, z);
                    level.setBlock(wallPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
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
     * 检查是否太靠近大型结构
     */
    private boolean isTooCloseToLargeStructure(LevelAccessor level, BlockPos pos, int minDistance) {
        // 检查周围是否有其他大型结构
        for (int x = -minDistance; x <= minDistance; x++) {
            for (int z = -minDistance; z <= minDistance; z++) {
                if (x != 0 || z != 0) {
                    BlockPos checkPos = pos.offset(x, 0, z);
                    // 检查是否有非自然方块（可能表示结构）
                    if (!level.getBlockState(checkPos).isAir() && 
                        !level.getBlockState(checkPos).getBlock().defaultBlockState().isAir()) {
                        // 如果发现非空气方块，检查是否是大规模结构的一部分
                        int structureSize = checkStructureSize(level, checkPos);
                        if (structureSize > 5) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 检查结构大小
     */
    private int checkStructureSize(LevelAccessor level, BlockPos startPos) {
        // 简单检查周围区域的结构大小
        int structureBlocks = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = startPos.offset(x, 0, z);
                if (!level.getBlockState(checkPos).isAir()) {
                    structureBlocks++;
                }
            }
        }
        return structureBlocks;
    }
}
