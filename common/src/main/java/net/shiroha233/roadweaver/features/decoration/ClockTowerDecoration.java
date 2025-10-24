package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.decoration.config.DecorationType;
import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.Random;

/**
 * 钟楼装饰实现
 * 在道路旁生成小型钟楼，作为地标性建筑
 */
public class ClockTowerDecoration implements Decoration {
    
    @Override
    public DecorationType getType() {
        return DecorationType.CLOCK_TOWER;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 钟楼需要较大的平坦区域，并且不能太靠近其他大型结构
        return checkFlatArea(level, pos, 5, 5) && 
               !isTooCloseToLargeStructure(level, pos, 10) &&
               hasGoodVisibility(level, pos);
    }
    
    @Override
    public void generate(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 选择钟楼风格
        TowerStyle towerStyle = TowerStyle.values()[random.nextInt(TowerStyle.values().length)];
        
        // 生成钟楼基础
        generateTowerFoundation(level, pos, random);
        
        // 根据风格生成钟楼主体
        switch (towerStyle) {
            case CLASSIC:
                generateClassicTower(level, pos, random);
                break;
            case MODERN:
                generateModernTower(level, pos, random);
                break;
            case MEDIEVAL:
                generateMedievalTower(level, pos, random);
                break;
        }
        
        // 生成钟楼顶部的钟
        generateClock(level, pos, random);
        
        // 生成钟楼周围的装饰
        generateTowerSurroundings(level, pos, random);
    }
    
    /**
     * 钟楼风格枚举
     */
    private enum TowerStyle {
        CLASSIC,   // 经典风格
        MODERN,    // 现代风格
        MEDIEVAL   // 中世纪风格
    }
    
    /**
     * 生成钟楼基础
     */
    private void generateTowerFoundation(LevelAccessor level, BlockPos pos, Random random) {
        // 生成5x5基础平台
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos basePos = pos.offset(x, 0, z);
                if (random.nextFloat() < 0.8f) {
                    level.setBlock(basePos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                } else {
                    level.setBlock(basePos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                }
            }
        }
        
        // 生成基础支撑柱
        BlockPos[] supportPositions = {
            pos.offset(-2, 0, -2),
            pos.offset(2, 0, -2),
            pos.offset(-2, 0, 2),
            pos.offset(2, 0, 2)
        };
        
        for (BlockPos supportPos : supportPositions) {
            for (int y = 1; y <= 2; y++) {
                BlockPos currentPos = supportPos.above(y);
                level.setBlock(currentPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成经典风格钟楼
     */
    private void generateClassicTower(LevelAccessor level, BlockPos pos, Random random) {
        int towerHeight = 8 + random.nextInt(4); // 8-11格高
        
        // 生成钟楼主体
        for (int y = 3; y < 3 + towerHeight; y++) {
            // 生成钟楼墙壁
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) == 1 || Math.abs(z) == 1) { // 只生成边框
                        BlockPos wallPos = pos.offset(x, y, z);
                        if (random.nextFloat() < 0.9f) {
                            level.setBlock(wallPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                        } else {
                            level.setBlock(wallPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                        }
                    }
                }
            }
            
            // 每3层生成窗户
            if (y % 3 == 0 && y < 3 + towerHeight - 2) {
                generateTowerWindows(level, pos, y, random);
            }
        }
        
        // 生成钟楼顶部装饰
        generateClassicTowerTop(level, pos, 3 + towerHeight, random);
    }
    
    /**
     * 生成现代风格钟楼
     */
    private void generateModernTower(LevelAccessor level, BlockPos pos, Random random) {
        int towerHeight = 10 + random.nextInt(6); // 10-15格高
        
        // 生成现代风格钟楼（更细更高）
        for (int y = 3; y < 3 + towerHeight; y++) {
            // 生成3x3核心结构
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos corePos = pos.offset(x, y, z);
                    if (random.nextFloat() < 0.7f) {
                        level.setBlock(corePos, Blocks.QUARTZ_BLOCK.defaultBlockState(), 3);
                    } else {
                        level.setBlock(corePos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
                    }
                }
            }
            
            // 每4层生成横向装饰
            if (y % 4 == 0) {
                generateModernTier(level, pos, y, random);
            }
        }
        
        // 生成现代风格顶部
        generateModernTowerTop(level, pos, 3 + towerHeight, random);
    }
    
    /**
     * 生成中世纪风格钟楼
     */
    private void generateMedievalTower(LevelAccessor level, BlockPos pos, Random random) {
        int towerHeight = 7 + random.nextInt(3); // 7-9格高
        
        // 生成中世纪风格钟楼（石质，有垛口）
        for (int y = 3; y < 3 + towerHeight; y++) {
            // 生成3x3石质结构
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos stonePos = pos.offset(x, y, z);
                    if (random.nextFloat() < 0.8f) {
                        level.setBlock(stonePos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(stonePos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
                    }
                }
            }
            
            // 在顶层生成垛口
            if (y == 3 + towerHeight - 1) {
                generateBattlements(level, pos, y, random);
            }
        }
        
        // 生成中世纪风格顶部
        generateMedievalTowerTop(level, pos, 3 + towerHeight, random);
    }
    
    /**
     * 生成钟楼窗户
     */
    private void generateTowerWindows(LevelAccessor level, BlockPos pos, int yLevel, Random random) {
        // 在四个方向生成窗户
        BlockPos[] windowPositions = {
            pos.offset(1, yLevel, 0),
            pos.offset(-1, yLevel, 0),
            pos.offset(0, yLevel, 1),
            pos.offset(0, yLevel, -1)
        };
        
        for (BlockPos windowPos : windowPositions) {
            if (random.nextFloat() < 0.7f) {
                level.setBlock(windowPos, Blocks.GLASS_PANE.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成现代风格分层
     */
    private void generateModernTier(LevelAccessor level, BlockPos pos, int yLevel, Random random) {
        // 在钟楼周围生成横向装饰层
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    BlockPos tierPos = pos.offset(x, yLevel, z);
                    if (random.nextBoolean()) {
                        level.setBlock(tierPos, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), 3);
                    } else {
                        level.setBlock(tierPos, Blocks.QUARTZ_SLAB.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * 生成垛口
     */
    private void generateBattlements(LevelAccessor level, BlockPos pos, int yLevel, Random random) {
        // 在钟楼顶部生成垛口
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    BlockPos battlementPos = pos.offset(x, yLevel, z);
                    // 每隔一个位置生成垛口
                    if ((Math.abs(x) + Math.abs(z)) % 2 == 0) {
                        level.setBlock(battlementPos, Blocks.COBBLESTONE_WALL.defaultBlockState(), 3);
                    } else {
                        level.setBlock(battlementPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
    
    /**
     * 生成经典风格钟楼顶部
     */
    private void generateClassicTowerTop(LevelAccessor level, BlockPos pos, int baseY, Random random) {
        // 生成钟楼尖顶
        for (int y = 0; y < 3; y++) {
            BlockPos spirePos = pos.above(baseY + y);
            if (y < 2) {
                level.setBlock(spirePos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            } else {
                level.setBlock(spirePos, Blocks.SMOOTH_STONE_STAIRS.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成现代风格钟楼顶部
     */
    private void generateModernTowerTop(LevelAccessor level, BlockPos pos, int baseY, Random random) {
        // 生成现代风格平顶
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos topPos = pos.offset(x, baseY, z);
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    level.setBlock(topPos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
                } else {
                    level.setBlock(topPos, Blocks.QUARTZ_SLAB.defaultBlockState(), 3);
                }
            }
        }
        
        // 在顶部中心生成天线
        BlockPos antennaPos = pos.above(baseY + 1);
        level.setBlock(antennaPos, Blocks.IRON_BARS.defaultBlockState(), 3);
    }
    
    /**
     * 生成中世纪风格钟楼顶部
     */
    private void generateMedievalTowerTop(LevelAccessor level, BlockPos pos, int baseY, Random random) {
        // 生成锥形屋顶
        for (int layer = 0; layer < 3; layer++) {
            int radius = 2 - layer;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) <= radius && Math.abs(z) <= radius) {
                        BlockPos roofPos = pos.offset(x, baseY + layer, z);
                        if (random.nextFloat() < 0.8f) {
                            level.setBlock(roofPos, Blocks.SPRUCE_STAIRS.defaultBlockState(), 3);
                        } else {
                            level.setBlock(roofPos, Blocks.OAK_STAIRS.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 生成钟
     */
    private void generateClock(LevelAccessor level, BlockPos pos, Random random) {
        // 在钟楼适当高度生成钟
        int clockHeight = 6 + random.nextInt(4); // 6-9格高
        BlockPos clockPos = pos.above(clockHeight);
        
        // 生成钟的框架
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) == 1 || Math.abs(z) == 1) {
                    BlockPos framePos = clockPos.offset(x, 0, z);
                    level.setBlock(framePos, Blocks.IRON_BARS.defaultBlockState(), 3);
                }
            }
        }
        
        // 生成钟（使用铃铛方块）
        level.setBlock(clockPos, Blocks.BELL.defaultBlockState(), 3);
        
        // 60%概率在钟周围生成装饰性灯笼
        if (random.nextFloat() < 0.6f) {
            BlockPos[] lanternPositions = {
                clockPos.offset(2, 0, 0),
                clockPos.offset(-2, 0, 0),
                clockPos.offset(0, 0, 2),
                clockPos.offset(0, 0, -2)
            };
            
            for (BlockPos lanternPos : lanternPositions) {
                if (random.nextFloat() < 0.5f) {
                    level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 生成钟楼周围的装饰
     */
    private void generateTowerSurroundings(LevelAccessor level, BlockPos pos, Random random) {
        // 生成钟楼入口
        generateTowerEntrance(level, pos, random);
        
        // 50%概率在钟楼周围生成小广场
        if (random.nextBoolean()) {
            generateTowerPlaza(level, pos, random);
        }
        
        // 40%概率在钟楼旁生成花坛
        if (random.nextFloat() < 0.4f) {
            generateTowerFlowerBeds(level, pos, random);
        }
        
        // 30%概率在钟楼周围生成矮墙
        if (random.nextFloat() < 0.3f) {
            generateTowerWall(level, pos, random);
        }
    }
    
    /**
     * 生成钟楼入口
     */
    private void generateTowerEntrance(LevelAccessor level, BlockPos pos, Random random) {
        // 在钟楼南侧生成入口
        BlockPos entrancePos = pos.offset(0, 1, -2);
        level.setBlock(entrancePos, Blocks.OAK_DOOR.defaultBlockState(), 3);
        
        // 生成入口台阶
        for (int z = -1; z >= -3; z--) {
            BlockPos stepPos = pos.offset(0, 0, z);
            level.setBlock(stepPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
        }
        
        // 生成入口两侧的灯笼
        BlockPos[] entranceLanterns = {
            pos.offset(1, 1, -2),
            pos.offset(-1, 1, -2)
        };
        
        for (BlockPos lanternPos : entranceLanterns) {
            if (random.nextFloat() < 0.7f) {
                level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 生成钟楼广场
     */
    private void generateTowerPlaza(LevelAccessor level, BlockPos pos, Random random) {
        int plazaRadius = 6;
        
        // 生成圆形广场地面
        for (int x = -plazaRadius; x <= plazaRadius; x++) {
            for (int z = -plazaRadius; z <= plazaRadius; z++) {
                double distance = Math.sqrt(x*x + z*z);
                if (distance <= plazaRadius) {
                    BlockPos plazaPos = pos.offset(x, 0, z);
                    
                    if (distance <= plazaRadius - 1) {
                        // 广场内部 - 平滑石头
                        if (random.nextFloat() < 0.9f) {
                            level.setBlock(plazaPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                        } else {
                            level.setBlock(plazaPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                        }
                    } else {
                        // 广场边缘 - 石板
                        level.setBlock(plazaPos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // 在广场周围生成座椅
        generatePlazaSeats(level, pos, random);
    }
    
    /**
     * 生成广场座椅
     */
    private void generatePlazaSeats(LevelAccessor level, BlockPos pos, Random random) {
        int seatRadius = 5;
        
        // 在四个主要方向生成座椅
        for (int direction = 0; direction < 4; direction++) {
            int xOffset = 0, zOffset = 0;
            switch (direction) {
                case 0: // 北
                    zOffset = -seatRadius;
                    break;
                case 1: // 东
                    xOffset = seatRadius;
                    break;
                case 2: // 南
                    zOffset = seatRadius;
                    break;
                case 3: // 西
                    xOffset = -seatRadius;
                    break;
            }
            
            // 生成座椅（使用楼梯方块）
            BlockPos seatPos = pos.offset(xOffset, 0, zOffset);
            if (random.nextFloat() < 0.8f) {
                level.setBlock(seatPos, Blocks.OAK_STAIRS.defaultBlockState(), 3);
                
                // 在座椅旁生成小桌子
                if (random.nextFloat() < 0.5f) {
                    BlockPos tablePos = seatPos.offset(xOffset != 0 ? 0 : 1, 0, zOffset != 0 ? 0 : 1);
                    level.setBlock(tablePos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                    level.setBlock(tablePos.above(), Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 生成钟楼花坛
     */
    private void generateTowerFlowerBeds(LevelAccessor level, BlockPos pos, Random random) {
        // 在钟楼四个角落生成花坛
        BlockPos[] flowerBedPositions = {
            pos.offset(4, 0, 4),
            pos.offset(-4, 0, 4),
            pos.offset(4, 0, -4),
            pos.offset(-4, 0, -4)
        };
        
        for (BlockPos flowerBedPos : flowerBedPositions) {
            if (random.nextFloat() < 0.7f) {
                // 生成花坛基础（泥土）
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos dirtPos = flowerBedPos.offset(x, 0, z);
                        level.setBlock(dirtPos, Blocks.DIRT.defaultBlockState(), 3);
                    }
                }
                
                // 生成花朵
                BlockPos flowerPos = flowerBedPos;
                if (random.nextFloat() < 0.6f) {
                    // 随机选择花朵类型
                    switch (random.nextInt(4)) {
                        case 0:
                            level.setBlock(flowerPos, Blocks.POPPY.defaultBlockState(), 3);
                            break;
                        case 1:
                            level.setBlock(flowerPos, Blocks.DANDELION.defaultBlockState(), 3);
                            break;
                        case 2:
                            level.setBlock(flowerPos, Blocks.BLUE_ORCHID.defaultBlockState(), 3);
                            break;
                        case 3:
                            level.setBlock(flowerPos, Blocks.ALLIUM.defaultBlockState(), 3);
                            break;
                    }
                }
                
                // 在花坛周围生成矮树篱
                if (random.nextFloat() < 0.4f) {
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                                BlockPos hedgePos = flowerBedPos.offset(x, 0, z);
                                level.setBlock(hedgePos, Blocks.OAK_LEAVES.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 生成钟楼围墙
     */
    private void generateTowerWall(LevelAccessor level, BlockPos pos, Random random) {
        int wallRadius = 7;
        
        // 生成圆形围墙
        for (int x = -wallRadius; x <= wallRadius; x++) {
            for (int z = -wallRadius; z <= wallRadius; z++) {
                double distance = Math.sqrt(x*x + z*z);
                if (Math.abs(distance - wallRadius) < 0.5) {
                    BlockPos wallPos = pos.offset(x, 0, z);
                    
                    // 生成围墙基础
                    level.setBlock(wallPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                    
                    // 生成围墙顶部（栅栏）
                    BlockPos fencePos = wallPos.above();
                    if (random.nextFloat() < 0.8f) {
                        level.setBlock(fencePos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                    }
                    
                    // 在围墙入口处生成门
                    if ((x == 0 && z == -wallRadius) || (x == 0 && z == wallRadius) ||
                        (x == -wallRadius && z == 0) || (x == wallRadius && z == 0)) {
                        level.setBlock(fencePos, Blocks.OAK_FENCE_GATE.defaultBlockState(), 3);
                    }
                }
            }
        }
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
                
                if (Math.abs(terrainY - baseY) > 2) {
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
        // 这里可以集成现有的结构检测系统
        // 暂时返回false，假设没有冲突
        return false;
    }
    
    /**
     * 检查视野是否良好
     */
    private boolean hasGoodVisibility(LevelAccessor level, BlockPos pos) {
        // 检查周围是否有足够的开放空间
        int openSpaceCount = 0;
        int totalChecks = 0;
        
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (Math.abs(x) > 1 || Math.abs(z) > 1) { // 排除钟楼本身位置
                    BlockPos checkPos = pos.offset(x, 0, z);
                    if (!level.getBlockState(checkPos).isSolid()) {
                        openSpaceCount++;
                    }
                    totalChecks++;
                }
            }
        }
        
        return (float)openSpaceCount / totalChecks > 0.6f;
    }
}
