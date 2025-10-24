package net.shiroha233.roadweaver.features.landscape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.shiroha233.roadweaver.features.roadlogic.Road;
import net.shiroha233.roadweaver.features.biome.BiomeConnectionStrategy;
import net.shiroha233.roadweaver.features.terrain.EnhancedTerrainAdapter;
import net.shiroha233.roadweaver.features.config.DecorationConfig;

import java.util.Random;

/**
 * 景观建筑生成器 - 负责在关键位置生成特色建筑
 * 如凉亭、观景台、小桥等
 */
public class LandscapeArchitectureGenerator {
    
    private final LevelAccessor level;
    private final Random random;
    private final EnhancedTerrainAdapter terrainAdapter;
    private final BiomeConnectionStrategy biomeStrategy;
    
    public LandscapeArchitectureGenerator(LevelAccessor level, Random random, 
                                        EnhancedTerrainAdapter terrainAdapter,
                                        BiomeConnectionStrategy biomeStrategy) {
        this.level = level;
        this.random = random;
        this.terrainAdapter = terrainAdapter;
        this.biomeStrategy = biomeStrategy;
    }
    
    /**
     * 在道路关键位置生成景观建筑
     */
    public void generateLandscapeArchitecture(Road road, BlockPos roadPos) {
        // 检查是否应该在此位置生成景观建筑
        if (!shouldGenerateLandscapeArchitecture(road, roadPos)) {
            return;
        }
        
        // 根据道路类型和位置选择合适的景观建筑
        LandscapeArchitectureType architectureType = selectArchitectureType(road, roadPos);
        
        // 生成选定的景观建筑
        switch (architectureType) {
            case PAVILION:
                generatePavilion(roadPos);
                break;
            case VIEWING_PLATFORM:
                generateViewingPlatform(roadPos);
                break;
            case SMALL_BRIDGE:
                generateSmallBridge(roadPos);
                break;
            case GAZEBO:
                generateGazebo(roadPos);
                break;
            case FOUNTAIN_SQUARE:
                generateFountainSquare(roadPos);
                break;
            case REST_AREA:
                generateRestArea(roadPos);
                break;
        }
    }
    
    /**
     * 判断是否应该在当前位置生成景观建筑
     */
    private boolean shouldGenerateLandscapeArchitecture(Road road, BlockPos roadPos) {
        // 基于道路等级和位置特征决定
        double generationChance = getGenerationChance(road);
        
        // 检查地形特征 - 是否在特殊位置（如山顶、水边、交叉口等）
        boolean isSpecialLocation = isSpecialLocation(roadPos);
        
        // 增加特殊位置的生成概率
        if (isSpecialLocation) {
            generationChance *= 2.0;
        }
        
        return random.nextDouble() < generationChance;
    }
    
    /**
     * 根据道路等级获取景观建筑生成概率
     */
    private double getGenerationChance(Road road) {
        switch (road.getRoadType()) {
            case HIGHWAY:
                return 0.02; // 高速公路生成概率较低
            case MAIN_ROAD:
                return 0.05; // 主干道中等概率
            case SECONDARY_ROAD:
                return 0.08; // 次干道较高概率
            case RESIDENTIAL_ROAD:
                return 0.12; // 居民区道路高概率
            case NATURAL_PATH:
                return 0.15; // 自然路径最高概率
            default:
                return 0.05;
        }
    }
    
    /**
     * 检查是否为特殊位置
     */
    private boolean isSpecialLocation(BlockPos pos) {
        // 检查是否在水边
        if (isNearWater(pos)) {
            return true;
        }
        
        // 检查是否在山顶
        if (isOnHilltop(pos)) {
            return true;
        }
        
        // 检查是否在道路交叉口
        if (isAtIntersection(pos)) {
            return true;
        }
        
        // 检查是否有良好视野
        if (hasGoodView(pos)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 选择景观建筑类型
     */
    private LandscapeArchitectureType selectArchitectureType(Road road, BlockPos pos) {
        // 基于位置特征选择建筑类型
        if (isNearWater(pos)) {
            return random.nextBoolean() ? LandscapeArchitectureType.SMALL_BRIDGE : 
                                          LandscapeArchitectureType.VIEWING_PLATFORM;
        }
        
        if (isOnHilltop(pos)) {
            return random.nextBoolean() ? LandscapeArchitectureType.VIEWING_PLATFORM : 
                                          LandscapeArchitectureType.PAVILION;
        }
        
        if (isAtIntersection(pos)) {
            return random.nextBoolean() ? LandscapeArchitectureType.FOUNTAIN_SQUARE : 
                                          LandscapeArchitectureType.REST_AREA;
        }
        
        // 默认选择
        LandscapeArchitectureType[] types = LandscapeArchitectureType.values();
        return types[random.nextInt(types.length)];
    }
    
    /**
     * 生成凉亭
     */
    private void generatePavilion(BlockPos centerPos) {
        BlockPos basePos = terrainAdapter.findSuitableGround(centerPos);
        
        // 凉亭基础平台 (5x5)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = basePos.offset(x, 0, z);
                BlockState platformBlock = getPlatformBlock();
                level.setBlock(pos, platformBlock, 3);
            }
        }
        
        // 凉亭柱子 (4个角落)
        BlockState pillarBlock = getPillarBlock();
        level.setBlock(basePos.offset(-2, 1, -2), pillarBlock, 3);
        level.setBlock(basePos.offset(2, 1, -2), pillarBlock, 3);
        level.setBlock(basePos.offset(-2, 1, 2), pillarBlock, 3);
        level.setBlock(basePos.offset(2, 1, 2), pillarBlock, 3);
        
        // 凉亭屋顶
        for (int y = 2; y <= 4; y++) {
            int radius = 5 - y;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) + Math.abs(z) <= radius + 1) {
                        BlockPos roofPos = basePos.offset(x, y, z);
                        BlockState roofBlock = getRoofBlock();
                        level.setBlock(roofPos, roofBlock, 3);
                    }
                }
            }
        }
        
        // 凉亭座椅
        BlockState benchBlock = getBenchBlock();
        level.setBlock(basePos.offset(-1, 1, 0), benchBlock, 3);
        level.setBlock(basePos.offset(1, 1, 0), benchBlock, 3);
    }
    
    /**
     * 生成观景台
     */
    private void generateViewingPlatform(BlockPos centerPos) {
        BlockPos basePos = terrainAdapter.findSuitableGround(centerPos);
        
        // 观景台平台 (7x7)
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos pos = basePos.offset(x, 0, z);
                BlockState platformBlock = getPlatformBlock();
                level.setBlock(pos, platformBlock, 3);
            }
        }
        
        // 观景台栏杆
        BlockState railingBlock = getRailingBlock();
        for (int x = -3; x <= 3; x++) {
            level.setBlock(basePos.offset(x, 1, -3), railingBlock, 3);
            level.setBlock(basePos.offset(x, 1, 3), railingBlock, 3);
        }
        for (int z = -2; z <= 2; z++) {
            level.setBlock(basePos.offset(-3, 1, z), railingBlock, 3);
            level.setBlock(basePos.offset(3, 1, z), railingBlock, 3);
        }
        
        // 观景台座椅
        BlockState benchBlock = getBenchBlock();
        level.setBlock(basePos.offset(-2, 1, -2), benchBlock, 3);
        level.setBlock(basePos.offset(2, 1, -2), benchBlock, 3);
        level.setBlock(basePos.offset(-2, 1, 2), benchBlock, 3);
        level.setBlock(basePos.offset(2, 1, 2), benchBlock, 3);
        
        // 观景台望远镜或装饰
        if (random.nextBoolean()) {
            BlockPos telescopePos = basePos.offset(0, 1, 0);
            BlockState telescopeBlock = getTelescopeBlock();
            level.setBlock(telescopePos, telescopeBlock, 3);
        }
    }
    
    /**
     * 生成小桥
     */
    private void generateSmallBridge(BlockPos centerPos) {
        // 寻找合适的水体位置
        BlockPos bridgeStart = findBridgeStartPosition(centerPos);
        if (bridgeStart == null) return;
        
        // 确定桥梁方向和长度
        int bridgeLength = 5 + random.nextInt(6); // 5-10格长度
        boolean bridgeDirection = random.nextBoolean(); // true为x方向，false为z方向
        
        // 生成桥梁
        for (int i = 0; i < bridgeLength; i++) {
            BlockPos bridgePos = bridgeDirection ? 
                bridgeStart.offset(i, 0, 0) : bridgeStart.offset(0, 0, i);
            
            // 桥梁基础
            BlockState bridgeBase = getBridgeBaseBlock();
            level.setBlock(bridgePos, bridgeBase, 3);
            
            // 桥梁栏杆
            if (i == 0 || i == bridgeLength - 1 || random.nextDouble() < 0.3) {
                BlockState railingBlock = getRailingBlock();
                if (bridgeDirection) {
                    level.setBlock(bridgePos.offset(0, 1, -1), railingBlock, 3);
                    level.setBlock(bridgePos.offset(0, 1, 1), railingBlock, 3);
                } else {
                    level.setBlock(bridgePos.offset(-1, 1, 0), railingBlock, 3);
                    level.setBlock(bridgePos.offset(1, 1, 0), railingBlock, 3);
                }
            }
        }
    }
    
    /**
     * 生成亭子
     */
    private void generateGazebo(BlockPos centerPos) {
        BlockPos basePos = terrainAdapter.findSuitableGround(centerPos);
        
        // 亭子圆形平台 (半径3)
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (x*x + z*z <= 9) { // 圆形检查
                    BlockPos pos = basePos.offset(x, 0, z);
                    BlockState platformBlock = getPlatformBlock();
                    level.setBlock(pos, platformBlock, 3);
                }
            }
        }
        
        // 亭子柱子 (6根)
        BlockState pillarBlock = getPillarBlock();
        double angleStep = Math.PI / 3; // 60度间隔
        for (int i = 0; i < 6; i++) {
            double angle = i * angleStep;
            int x = (int) Math.round(2 * Math.cos(angle));
            int z = (int) Math.round(2 * Math.sin(angle));
            level.setBlock(basePos.offset(x, 1, z), pillarBlock, 3);
            level.setBlock(basePos.offset(x, 2, z), pillarBlock, 3);
        }
        
        // 亭子锥形屋顶
        for (int y = 3; y <= 5; y++) {
            int radius = 6 - y;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + z*z <= radius*radius) {
                        BlockPos roofPos = basePos.offset(x, y, z);
                        BlockState roofBlock = getRoofBlock();
                        level.setBlock(roofPos, roofBlock, 3);
                    }
                }
            }
        }
    }
    
    /**
     * 生成喷泉广场
     */
    private void generateFountainSquare(BlockPos centerPos) {
        BlockPos basePos = terrainAdapter.findSuitableGround(centerPos);
        
        // 广场平台 (9x9)
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos pos = basePos.offset(x, 0, z);
                BlockState platformBlock = getPlatformBlock();
                level.setBlock(pos, platformBlock, 3);
            }
        }
        
        // 中心喷泉
        BlockPos fountainCenter = basePos.offset(0, 1, 0);
        
        // 喷泉底座
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = fountainCenter.offset(x, 0, z);
                BlockState fountainBase = getFountainBaseBlock();
                level.setBlock(pos, fountainBase, 3);
            }
        }
        
        // 喷泉中心柱
        BlockState fountainPillar = getFountainPillarBlock();
        for (int y = 1; y <= 3; y++) {
            level.setBlock(fountainCenter.offset(0, y, 0), fountainPillar, 3);
        }
        
        // 喷泉水
        level.setBlock(fountainCenter.offset(0, 4, 0), Blocks.WATER.defaultBlockState(), 3);
        
        // 广场周边座椅
        BlockState benchBlock = getBenchBlock();
        for (int i = -3; i <= 3; i += 2) {
            level.setBlock(basePos.offset(i, 1, -4), benchBlock, 3);
            level.setBlock(basePos.offset(i, 1, 4), benchBlock, 3);
            level.setBlock(basePos.offset(-4, 1, i), benchBlock, 3);
            level.setBlock(basePos.offset(4, 1, i), benchBlock, 3);
        }
    }
    
    /**
     * 生成休息区
     */
    private void generateRestArea(BlockPos centerPos) {
        BlockPos basePos = terrainAdapter.findSuitableGround(centerPos);
        
        // 休息区平台 (7x7)
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos pos = basePos.offset(x, 0, z);
                BlockState platformBlock = getPlatformBlock();
                level.setBlock(pos, platformBlock, 3);
            }
        }
        
        // 多个座椅
        BlockState benchBlock = getBenchBlock();
        level.setBlock(basePos.offset(-2, 1, -2), benchBlock, 3);
        level.setBlock(basePos.offset(2, 1, -2), benchBlock, 3);
        level.setBlock(basePos.offset(-2, 1, 2), benchBlock, 3);
        level.setBlock(basePos.offset(2, 1, 2), benchBlock, 3);
        
        // 中心桌子
        BlockState tableBlock = getTableBlock();
        level.setBlock(basePos.offset(0, 1, 0), tableBlock, 3);
        
        // 遮阳结构
        if (random.nextBoolean()) {
            BlockPos shadePos = basePos.offset(0, 2, 0);
            BlockState shadeBlock = getShadeBlock();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    level.setBlock(shadePos.offset(x, 0, z), shadeBlock, 3);
                }
            }
        }
        
        // 周边绿化
        generateRestAreaGreening(basePos);
    }
    
    /**
     * 生成休息区绿化
     */
    private void generateRestAreaGreening(BlockPos centerPos) {
        // 在休息区周边种植树木和花朵
        for (int i = 0; i < 4 + random.nextInt(3); i++) {
            int angle = random.nextInt(360);
            int distance = 5 + random.nextInt(3);
            int x = (int) (distance * Math.cos(Math.toRadians(angle)));
            int z = (int) (distance * Math.sin(Math.toRadians(angle)));
            
            BlockPos treePos = centerPos.offset(x, 0, z);
            BlockPos groundPos = terrainAdapter.findSuitableGround(treePos);
            
            // 随机选择种植树木或花朵
            if (random.nextBoolean()) {
                // 种植小树
                plantSmallTree(groundPos);
            } else {
                // 种植花丛
                plantFlowerCluster(groundPos);
            }
        }
    }
    
    // ===== 地形检查方法 =====
    
    /**
     * 检查是否在水边
     */
    private boolean isNearWater(BlockPos pos) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (level.getBlockState(checkPos).getBlock() == Blocks.WATER) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查是否在山顶
     */
    private boolean isOnHilltop(BlockPos pos) {
        // 检查周围地形高度变化
        int centerHeight = pos.getY();
        int lowerCount = 0;
        
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue;
                
                BlockPos checkPos = pos.offset(x, 0, z);
                int checkHeight = terrainAdapter.getTerrainHeight(checkPos.getX(), checkPos.getZ());
                
                if (checkHeight < centerHeight - 2) {
                    lowerCount++;
                }
            }
        }
        
        // 如果周围大部分位置都较低，则认为是山顶
        return lowerCount >= 12; // 至少12个周围位置较低
    }
    
    /**
     * 检查是否在道路交叉口
     */
    private boolean isAtIntersection(BlockPos pos) {
        // 简化实现 - 在实际项目中应该检查道路网络
        // 这里使用随机概率模拟交叉口检测
        return random.nextDouble() < 0.1;
    }
    
    /**
     * 检查是否有良好视野
     */
    private boolean hasGoodView(BlockPos pos) {
        // 检查周围是否有开阔区域
        int openAreaCount = 0;
        
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (Math.abs(x) + Math.abs(z) > 8) continue;
                
                BlockPos checkPos = pos.offset(x, 0, z);
                if (terrainAdapter.isFlatArea(checkPos, 3)) {
                    openAreaCount++;
                }
            }
        }
        
        return openAreaCount >= 15;
    }
    
    /**
     * 寻找桥梁起始位置
     */
    private BlockPos findBridgeStartPosition(BlockPos centerPos) {
        // 在中心位置周围寻找合适的水体边缘
        for (int radius = 1; radius <= 5; radius++) {
            for (int angle = 0; angle < 360; angle += 45) {
                int x = (int) (radius * Math.cos(Math.toRadians(angle)));
                int z = (int) (radius * Math.sin(Math.toRadians(angle)));
                
                BlockPos checkPos = centerPos.offset(x, 0, z);
                if (isSuitableBridgeStart(checkPos)) {
                    return checkPos;
                }
            }
        }
        return null;
    }
    
    /**
     * 检查是否为合适的桥梁起始位置
     */
    private boolean isSuitableBridgeStart(BlockPos pos) {
        // 检查是否在水体边缘（一侧是水，一侧是陆地）
        boolean hasWater = false;
        boolean hasLand = false;
        
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (level.getBlockState(checkPos).getBlock() == Blocks.WATER) {
                    hasWater = true;
                } else if (terrainAdapter.isSolidGround(checkPos)) {
                    hasLand = true;
                }
            }
        }
        
        return hasWater && hasLand;
    }
    
    // ===== 方块获取方法 =====
    
    /**
     * 获取平台方块
     */
    private BlockState getPlatformBlock() {
        // 根据生物群系选择合适的平台材料
        return biomeStrategy.getSuitableBlock(Blocks.STONE_BRICKS.defaultBlockState(), 
                                            Blocks.OAK_PLANKS.defaultBlockState());
    }
    
    /**
     * 获取柱子方块
     */
    private BlockState getPillarBlock() {
        return biomeStrategy.getSuitableBlock(Blocks.STONE_BRICKS.defaultBlockState(),
                                            Blocks.SPRUCE_LOG.defaultBlockState());
    }
    
    /**
     * 获取屋顶方块
     */
    private BlockState getRoofBlock() {
        return biomeStrategy.getSuitableBlock(Blocks.SPRUCE_PLANKS.defaultBlockState(),
                                            Blocks.OAK_PLANKS.defaultBlockState());
    }
    
    /**
     * 获取座椅方块
     */
    private BlockState getBenchBlock() {
        return biomeStrategy.getSuitableBlock(Blocks.SPRUCE_STAIRS.defaultBlockState(),
                                            Blocks.OAK_STAIRS.defaultBlockState());
    }
    
    /**
     * 获取栏杆方块
     */
    private BlockState getRailingBlock() {
        return biomeStrategy.getSuitableBlock(Blocks.STONE_BRICK_WALL.defaultBlockState(),
                                            Blocks.SPRUCE_FENCE.defaultBlockState());
    }
    
    /**
     * 获取望远镜方块
     */
    private BlockState getTelescopeBlock() {
        return Blocks.IRON_BARS.defaultBlockState();
    }
    
    /**
     * 获取桥梁基础方块
     */
    private BlockState getBridgeBaseBlock() {
        return biomeStrategy.getSuitableBlock(Blocks.STONE_BRICKS.defaultBlockState(),
                                            Blocks.OAK_PLANKS.defaultBlockState());
    }
    
    /**
     * 获取喷泉底座方块
     */
    private BlockState getFountainBaseBlock() {
        return Blocks.STONE_BRICKS.defaultBlockState();
    }
    
    /**
     * 获取喷泉柱子方块
     */
    private BlockState getFountainPillarBlock() {
        return Blocks.STONE_BRICKS.defaultBlockState();
    }
    
    /**
     * 获取桌子方块
     */
    private BlockState getTableBlock() {
        return biomeStrategy.getSuitableBlock(Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState(),
                                            Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
    }
    
    /**
     * 获取遮阳方块
     */
    private BlockState getShadeBlock() {
        return biomeStrategy.getSuitableBlock(Blocks.SPRUCE_LEAVES.defaultBlockState(),
                                            Blocks.OAK_LEAVES.defaultBlockState());
    }
    
    // ===== 植物种植方法 =====
    
    /**
     * 种植小树
     */
    private void plantSmallTree(BlockPos pos) {
        BlockPos groundPos = terrainAdapter.findSuitableGround(pos);
        if (groundPos == null) return;
        
        // 树苗
        level.setBlock(groundPos, getSaplingBlock(), 3);
        
        // 随机在周围种植花朵
        for (int i = 0; i < 2 + random.nextInt(3); i++) {
            int offsetX = random.nextInt(3) - 1;
            int offsetZ = random.nextInt(3) - 1;
            BlockPos flowerPos = groundPos.offset(offsetX, 0, offsetZ);
            
            if (terrainAdapter.isSolidGround(flowerPos)) {
                level.setBlock(flowerPos, getRandomFlowerBlock(), 3);
            }
        }
    }
    
    /**
     * 种植花丛
     */
    private void plantFlowerCluster(BlockPos centerPos) {
        BlockPos groundPos = terrainAdapter.findSuitableGround(centerPos);
        if (groundPos == null) return;
        
        // 种植3-6朵花
        int flowerCount = 3 + random.nextInt(4);
        for (int i = 0; i < flowerCount; i++) {
            int offsetX = random.nextInt(5) - 2;
            int offsetZ = random.nextInt(5) - 2;
            BlockPos flowerPos = groundPos.offset(offsetX, 0, offsetZ);
            
            if (terrainAdapter.isSolidGround(flowerPos)) {
                level.setBlock(flowerPos, getRandomFlowerBlock(), 3);
            }
        }
    }
    
    /**
     * 获取树苗方块
     */
    private BlockState getSaplingBlock() {
        BlockState[] saplings = {
            Blocks.OAK_SAPLING.defaultBlockState(),
            Blocks.SPRUCE_SAPLING.defaultBlockState(),
            Blocks.BIRCH_SAPLING.defaultBlockState(),
            Blocks.JUNGLE_SAPLING.defaultBlockState(),
            Blocks.ACACIA_SAPLING.defaultBlockState(),
            Blocks.DARK_OAK_SAPLING.defaultBlockState()
        };
        return saplings[random.nextInt(saplings.length)];
    }
    
    /**
     * 获取随机花朵方块
     */
    private BlockState getRandomFlowerBlock() {
        BlockState[] flowers = {
            Blocks.DANDELION.defaultBlockState(),
            Blocks.POPPY.defaultBlockState(),
            Blocks.BLUE_ORCHID.defaultBlockState(),
            Blocks.ALLIUM.defaultBlockState(),
            Blocks.AZURE_BLUET.defaultBlockState(),
            Blocks.RED_TULIP.defaultBlockState(),
            Blocks.ORANGE_TULIP.defaultBlockState(),
            Blocks.WHITE_TULIP.defaultBlockState(),
            Blocks.PINK_TULIP.defaultBlockState(),
            Blocks.OXEYE_DAISY.defaultBlockState(),
            Blocks.CORNFLOWER.defaultBlockState(),
            Blocks.LILY_OF_THE_VALLEY.defaultBlockState()
        };
        return flowers[random.nextInt(flowers.length)];
    }
    
    /**
     * 获取景观建筑生成配置
     */
    public LandscapeArchitectureConfig getConfig() {
        return new LandscapeArchitectureConfig();
    }
}
