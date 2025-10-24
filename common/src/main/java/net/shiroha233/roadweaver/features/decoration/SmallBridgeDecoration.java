package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.decoration.config.DecorationType;
import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.Random;

/**
 * 小桥装饰实现
 * 在道路跨越小溪或沟壑时生成小型桥梁
 */
public class SmallBridgeDecoration implements Decoration {
    
    @Override
    public DecorationType getType() {
        return DecorationType.SMALL_BRIDGE;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 小桥需要跨越水体或沟壑
        return isCrossingWater(level, pos) || isCrossingRavine(level, pos);
    }
    
    @Override
    public void generate(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 确定桥梁方向（与道路方向垂直）
        boolean bridgeDirection = random.nextBoolean(); // true = 东西向, false = 南北向
        
        // 生成桥梁基础
        generateBridgeFoundation(level, pos, bridgeDirection, random);
        
        // 生成桥面
        generateBridgeDeck(level, pos, bridgeDirection, random);
        
        // 生成栏杆
        generateBridgeRailings(level, pos, bridgeDirection, random);
        
        // 生成桥墩（如果需要）
        generateBridgePiers(level, pos, bridgeDirection, random);
        
        // 生成装饰性元素
        generateBridgeDecorations(level, pos, bridgeDirection, random);
    }
    
    /**
     * 检查是否跨越水体
     */
    private boolean isCrossingWater(LevelAccessor level, BlockPos pos) {
        int waterCount = 0;
        // 检查周围5x5区域
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (level.getBlockState(checkPos).getBlock() == Blocks.WATER) {
                    waterCount++;
                }
            }
        }
        return waterCount >= 3; // 至少有3个水方块
    }
    
    /**
     * 检查是否跨越沟壑
     */
    private boolean isCrossingRavine(LevelAccessor level, BlockPos pos) {
        int heightVariation = 0;
        // 检查周围区域的高度变化
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos checkPos = pos.offset(x, 0, z);
                int terrainHeight = level.getHeight(checkPos.getX(), checkPos.getZ());
                heightVariation = Math.max(heightVariation, Math.abs(terrainHeight - pos.getY()));
            }
        }
        return heightVariation >= 3; // 至少有3格高度差
    }
    
    /**
     * 生成桥梁基础
     */
    private void generateBridgeFoundation(LevelAccessor level, BlockPos pos, boolean bridgeDirection, Random random) {
        int bridgeLength = 5 + random.nextInt(3); // 5-7格长
        
        for (int i = 0; i < bridgeLength; i++) {
            BlockPos foundationPos;
            if (bridgeDirection) {
                // 东西向桥梁
                foundationPos = pos.offset(i - bridgeLength/2, -1, 0);
            } else {
                // 南北向桥梁
                foundationPos = pos.offset(0, -1, i - bridgeLength/2);
            }
            
            // 生成桥墩基础
            level.setBlock(foundationPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
            
            // 如果在水上，生成额外的支撑
            if (level.getBlockState(foundationPos.above()).getBlock() == Blocks.WATER) {
                for (int y = -2; y <= -1; y++) {
                    BlockPos supportPos = foundationPos.above(y);
                    level.setBlock(supportPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 生成桥面
     */
    private void generateBridgeDeck(LevelAccessor level, BlockPos pos, boolean bridgeDirection, Random random) {
        int bridgeLength = 5 + random.nextInt(3); // 5-7格长
        int bridgeWidth = 3; // 3格宽
        
        for (int i = 0; i < bridgeLength; i++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos deckPos;
                if (bridgeDirection) {
                    // 东西向桥梁
                    deckPos = pos.offset(i - bridgeLength/2, 0, w);
                } else {
                    // 南北向桥梁
                    deckPos = pos.offset(w, 0, i - bridgeLength/2);
                }
                
                // 生成桥面
                if (random.nextFloat() < 0.8f) {
                    level.setBlock(deckPos, Blocks.OAK_PLANKS.defaultBlockState(), 3);
                } else {
                    level.setBlock(deckPos, Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                }
                
                // 清理桥面下的水
                BlockPos belowPos = deckPos.below();
                if (level.getBlockState(belowPos).getBlock() == Blocks.WATER) {
                    level.setBlock(belowPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 生成桥梁栏杆
     */
    private void generateBridgeRailings(LevelAccessor level, BlockPos pos, boolean bridgeDirection, Random random) {
        int bridgeLength = 5 + random.nextInt(3); // 5-7格长
        
        for (int i = 0; i < bridgeLength; i++) {
            // 两侧栏杆
            for (int side = -1; side <= 1; side += 2) {
                BlockPos railingPos;
                if (bridgeDirection) {
                    // 东西向桥梁
                    railingPos = pos.offset(i - bridgeLength/2, 1, side * 2);
                } else {
                    // 南北向桥梁
                    railingPos = pos.offset(side * 2, 1, i - bridgeLength/2);
                }
                
                // 生成栏杆柱
                if (random.nextFloat() < 0.7f) {
                    level.setBlock(railingPos, Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
                } else {
                    level.setBlock(railingPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                }
                
                // 生成栏杆横梁
                BlockPos beamPos = railingPos.below();
                if (bridgeDirection) {
                    // 东西向桥梁
                    beamPos = pos.offset(i - bridgeLength/2, 1, side);
                } else {
                    // 南北向桥梁
                    beamPos = pos.offset(side, 1, i - bridgeLength/2);
                }
                level.setBlock(beamPos, Blocks.SPRUCE_SLAB.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成桥墩
     */
    private void generateBridgePiers(LevelAccessor level, BlockPos pos, boolean bridgeDirection, Random random) {
        int bridgeLength = 5 + random.nextInt(3); // 5-7格长
        
        // 在桥梁中间生成桥墩
        int middleIndex = bridgeLength / 2;
        
        for (int side = -1; side <= 1; side += 2) {
            BlockPos pierPos;
            if (bridgeDirection) {
                // 东西向桥梁
                pierPos = pos.offset(middleIndex - bridgeLength/2, -1, side * 3);
            } else {
                // 南北向桥梁
                pierPos = pos.offset(side * 3, -1, middleIndex - bridgeLength/2);
            }
            
            // 检查是否需要桥墩（在水上或深沟中）
            if (needsPier(level, pierPos)) {
                // 生成桥墩
                for (int y = -3; y <= 0; y++) {
                    BlockPos currentPierPos = pierPos.above(y);
                    if (y == 0) {
                        level.setBlock(currentPierPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(currentPierPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否需要桥墩
     */
    private boolean needsPier(LevelAccessor level, BlockPos pos) {
        // 检查下方是否有足够深度需要桥墩支撑
        int depth = 0;
        for (int y = -1; y >= -5; y--) {
            BlockPos checkPos = pos.above(y);
            if (level.getBlockState(checkPos).getBlock() == Blocks.WATER || 
                level.isEmptyBlock(checkPos)) {
                depth++;
            } else {
                break;
            }
        }
        return depth >= 3; // 至少3格深度需要桥墩
    }
    
    /**
     * 生成桥梁装饰
     */
    private void generateBridgeDecorations(LevelAccessor level, BlockPos pos, boolean bridgeDirection, Random random) {
        int bridgeLength = 5 + random.nextInt(3); // 5-7格长
        
        // 20%概率在桥梁入口生成灯笼
        if (random.nextFloat() < 0.2f) {
            BlockPos[] entrancePositions = {
                pos.offset(-bridgeLength/2, 1, 0),
                pos.offset(bridgeLength/2, 1, 0)
            };
            
            for (BlockPos entrancePos : entrancePositions) {
                if (bridgeDirection) {
                    // 东西向桥梁
                    entrancePos = pos.offset(entrancePos.getX(), 1, 0);
                } else {
                    // 南北向桥梁
                    entrancePos = pos.offset(0, 1, entrancePos.getZ());
                }
                
                level.setBlock(entrancePos.above(), Blocks.LANTERN.defaultBlockState(), 3);
            }
        }
        
        // 30%概率在桥梁上生成花盆
        if (random.nextFloat() < 0.3f) {
            int flowerIndex = random.nextInt(bridgeLength);
            BlockPos flowerPos;
            if (bridgeDirection) {
                // 东西向桥梁
                flowerPos = pos.offset(flowerIndex - bridgeLength/2, 1, 0);
            } else {
                // 南北向桥梁
                flowerPos = pos.offset(0, 1, flowerIndex - bridgeLength/2);
            }
            
            level.setBlock(flowerPos.above(), Blocks.FLOWER_POT.defaultBlockState(), 3);
            // 在花盆中放置随机花朵
            BlockPos potPos = flowerPos.above();
            if (random.nextBoolean()) {
                level.setBlock(potPos, Blocks.POTTED_POPPY.defaultBlockState(), 3);
            } else {
                level.setBlock(potPos, Blocks.POTTED_DANDELION.defaultBlockState(), 3);
            }
        }
    }
}
