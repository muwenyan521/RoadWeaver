package net.shiroha233.roadweaver.features;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 智能障碍物检测与绕行系统
 * 负责检测和处理道路生成过程中的各种障碍物
 */
public class ObstacleDetectionSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    
    // 障碍物类型枚举
    public enum ObstacleType {
        TREE,           // 树木
        WATER,          // 水流
        RAVINE,         // 峡谷
        CROPS,          // 庄稼
        STRUCTURE,      // 建筑结构
        NARROW_PASSAGE, // 狭窄通道
        CLIFF,          // 悬崖
        LAVA,           // 岩浆
        SNOW,           // 雪地特殊地形
        REDWOOD_FOREST  // 红杉林特殊地形
    }
    
    // 障碍物检测结果类
    public static class ObstacleDetectionResult {
        private final boolean hasObstacles;
        private final Set<ObstacleType> obstacleTypes;
        private final boolean shouldReroute;
        private final BlockPos obstaclePosition;
        private final double severity;
        
        public ObstacleDetectionResult(boolean hasObstacles, Set<ObstacleType> obstacleTypes, 
                                      boolean shouldReroute, BlockPos obstaclePosition, double severity) {
            this.hasObstacles = hasObstacles;
            this.obstacleTypes = obstacleTypes;
            this.shouldReroute = shouldReroute;
            this.obstaclePosition = obstaclePosition;
            this.severity = severity;
        }
        
        public boolean hasObstacles() { return hasObstacles; }
        public Set<ObstacleType> getObstacleTypes() { return obstacleTypes; }
        public boolean shouldReroute() { return shouldReroute; }
        public BlockPos getObstaclePosition() { return obstaclePosition; }
        public double getSeverity() { return severity; }
    }
    
    // 庄稼保护系统
    private static final Set<Block> CROP_BLOCKS = Set.of(
        Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
        Blocks.MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.SWEET_BERRY_BUSH
    );
    
    // 村庄相关方块
    private static final Set<Block> VILLAGE_BLOCKS = Set.of(
        Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR,
        Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.IRON_DOOR,
        Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE,
        Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, Blocks.NETHER_BRICK_FENCE,
        Blocks.GLASS_PANE, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE,
        Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
        Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE,
        Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE,
        Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE,
        Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE,
        Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE,
        Blocks.RED_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS_PANE
    );
    
    // 检测半径配置
    private static final int DETECTION_RADIUS = 3;
    private static final int CROP_PROTECTION_RADIUS = 5;
    private static final int VILLAGE_PROTECTION_RADIUS = 8;
    
    public ObstacleDetectionSystem() {
        LOGGER.debug("ObstacleDetectionSystem initialized");
    }
    
    /**
     * 检测指定位置的障碍物
     */
    public ObstacleDetectionResult detectObstacles(LevelAccessor level, BlockPos position, 
                                                  List<BlockState> materials, int roadType) {
        Set<ObstacleType> detectedObstacles = new HashSet<>();
        BlockPos obstaclePos = null;
        double severity = 0.0;
        
        // 检测树木
        ObstacleDetectionResult treeResult = detectTrees(level, position);
        if (treeResult.hasObstacles()) {
            detectedObstacles.addAll(treeResult.getObstacleTypes());
            severity = Math.max(severity, treeResult.getSeverity());
            if (obstaclePos == null) obstaclePos = treeResult.getObstaclePosition();
        }
        
        // 检测水流
        ObstacleDetectionResult waterResult = detectWater(level, position);
        if (waterResult.hasObstacles()) {
            detectedObstacles.addAll(waterResult.getObstacleTypes());
            severity = Math.max(severity, waterResult.getSeverity());
            if (obstaclePos == null) obstaclePos = waterResult.getObstaclePosition();
        }
        
        // 检测峡谷
        ObstacleDetectionResult ravineResult = detectRavines(level, position);
        if (ravineResult.hasObstacles()) {
            detectedObstacles.addAll(ravineResult.getObstacleTypes());
            severity = Math.max(severity, ravineResult.getSeverity());
            if (obstaclePos == null) obstaclePos = ravineResult.getObstaclePosition();
        }
        
        // 检测庄稼
        ObstacleDetectionResult cropResult = detectCrops(level, position);
        if (cropResult.hasObstacles()) {
            detectedObstacles.addAll(cropResult.getObstacleTypes());
            severity = Math.max(severity, cropResult.getSeverity());
            if (obstaclePos == null) obstaclePos = cropResult.getObstaclePosition();
        }
        
        // 检测狭窄区域
        ObstacleDetectionResult narrowResult = detectNarrowPassages(level, position);
        if (narrowResult.hasObstacles()) {
            detectedObstacles.addAll(narrowResult.getObstacleTypes());
            severity = Math.max(severity, narrowResult.getSeverity());
            if (obstaclePos == null) obstaclePos = narrowResult.getObstaclePosition();
        }
        
        // 检测特殊地形
        ObstacleDetectionResult terrainResult = detectSpecialTerrain(level, position);
        if (terrainResult.hasObstacles()) {
            detectedObstacles.addAll(terrainResult.getObstacleTypes());
            severity = Math.max(severity, terrainResult.getSeverity());
            if (obstaclePos == null) obstaclePos = terrainResult.getObstaclePosition();
        }
        
        // 决定是否需要绕行
        boolean shouldReroute = shouldRerouteForObstacles(detectedObstacles, severity, roadType);
        
        if (!detectedObstacles.isEmpty()) {
            LOGGER.debug("Detected obstacles at {}: {} (severity: {})", position, detectedObstacles, severity);
        }
        
        return new ObstacleDetectionResult(
            !detectedObstacles.isEmpty(), 
            detectedObstacles, 
            shouldReroute, 
            obstaclePos, 
            severity
        );
    }
    
    /**
     * 检测树木障碍物
     */
    private ObstacleDetectionResult detectTrees(LevelAccessor level, BlockPos position) {
        Set<ObstacleType> obstacles = new HashSet<>();
        BlockPos obstaclePos = null;
        double severity = 0.0;
        
        // 检测周围的树木
        for (int x = -DETECTION_RADIUS; x <= DETECTION_RADIUS; x++) {
            for (int z = -DETECTION_RADIUS; z <= DETECTION_RADIUS; z++) {
                BlockPos checkPos = position.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);
                
                // 检测原木和树叶
                if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                    obstacles.add(ObstacleType.TREE);
                    obstaclePos = checkPos;
                    severity = Math.max(severity, 0.7); // 树木严重性中等
                    
                    // 如果是大型树木（红杉），增加严重性
                    if (isRedwoodTree(level, checkPos)) {
                        obstacles.add(ObstacleType.REDWOOD_FOREST);
                        severity = 0.9;
                    }
                }
            }
        }
        
        return new ObstacleDetectionResult(!obstacles.isEmpty(), obstacles, 
                                          !obstacles.isEmpty(), obstaclePos, severity);
    }
    
    /**
     * 检测水流障碍物
     */
    private ObstacleDetectionResult detectWater(LevelAccessor level, BlockPos position) {
        Set<ObstacleType> obstacles = new HashSet<>();
        BlockPos obstaclePos = null;
        double severity = 0.0;
        
        // 检测周围的水流
        for (int x = -DETECTION_RADIUS; x <= DETECTION_RADIUS; x++) {
            for (int z = -DETECTION_RADIUS; z <= DETECTION_RADIUS; z++) {
                BlockPos checkPos = position.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);
                
                // 检测水和岩浆
                if (state.getFluidState().getType().isSame(Fluids.WATER)) {
                    obstacles.add(ObstacleType.WATER);
                    obstaclePos = checkPos;
                    severity = Math.max(severity, 0.6); // 水流严重性中等
                } else if (state.getFluidState().getType().isSame(Fluids.LAVA)) {
                    obstacles.add(ObstacleType.LAVA);
                    obstaclePos = checkPos;
                    severity = Math.max(severity, 0.9); // 岩浆严重性高
                }
            }
        }
        
        return new ObstacleDetectionResult(!obstacles.isEmpty(), obstacles, 
                                          !obstacles.isEmpty(), obstaclePos, severity);
    }
    
    /**
     * 检测峡谷障碍物
     */
    private ObstacleDetectionResult detectRavines(LevelAccessor level, BlockPos position) {
        Set<ObstacleType> obstacles = new HashSet<>();
        BlockPos obstaclePos = null;
        double severity = 0.0;
        
        // 检测周围的地形高度变化
        int currentHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, position.getX(), position.getZ());
        
        for (int x = -DETECTION_RADIUS; x <= DETECTION_RADIUS; x++) {
            for (int z = -DETECTION_RADIUS; z <= DETECTION_RADIUS; z++) {
                if (x == 0 && z == 0) continue;
                
                BlockPos checkPos = position.offset(x, 0, z);
                int checkHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, checkPos.getX(), checkPos.getZ());
                int heightDiff = Math.abs(checkHeight - currentHeight);
                
                // 如果高度差超过阈值，认为是峡谷或悬崖
                if (heightDiff > 5) {
                    obstacles.add(heightDiff > 10 ? ObstacleType.RAVINE : ObstacleType.CLIFF);
                    obstaclePos = checkPos;
                    severity = Math.max(severity, heightDiff > 10 ? 0.8 : 0.5);
                }
            }
        }
        
        return new ObstacleDetectionResult(!obstacles.isEmpty(), obstacles, 
                                          !obstacles.isEmpty(), obstaclePos, severity);
    }
    
    /**
     * 检测庄稼障碍物（庄稼保护系统）
     */
    private ObstacleDetectionResult detectCrops(LevelAccessor level, BlockPos position) {
        Set<ObstacleType> obstacles = new HashSet<>();
        BlockPos obstaclePos = null;
        double severity = 0.0;
        
        // 在庄稼保护半径内检测庄稼
        for (int x = -CROP_PROTECTION_RADIUS; x <= CROP_PROTECTION_RADIUS; x++) {
            for (int z = -CROP_PROTECTION_RADIUS; z <= CROP_PROTECTION_RADIUS; z++) {
                BlockPos checkPos = position.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);
                
                // 检测庄稼方块
                if (CROP_BLOCKS.contains(state.getBlock())) {
                    obstacles.add(ObstacleType.CROPS);
                    obstaclePos = checkPos;
                    severity = Math.max(severity, 0.8); // 庄稼保护严重性高
                    
                    // 检查是否是村庄农田
                    if (isVillageFarmland(level, checkPos)) {
                        severity = 0.9; // 村庄农田保护严重性更高
                    }
                }
            }
        }
        
        return new ObstacleDetectionResult(!obstacles.isEmpty(), obstacles, 
                                          !obstacles.isEmpty(), obstaclePos, severity);
    }
    
    /**
     * 检测狭窄通道
     */
    private ObstacleDetectionResult detectNarrowPassages(LevelAccessor level, BlockPos position) {
        Set<ObstacleType> obstacles = new HashSet<>();
        BlockPos obstaclePos = null;
        double severity = 0.0;
        
        // 检测周围的狭窄区域
        int passableWidth = calculatePassableWidth(level, position);
        
        // 如果可通行宽度小于阈值，认为是狭窄通道
        if (passableWidth < 3) {
            obstacles.add(ObstacleType.NARROW_PASSAGE);
            obstaclePos = position;
            severity = 0.6;
        }
        
        return new ObstacleDetectionResult(!obstacles.isEmpty(), obstacles, 
                                          !obstacles.isEmpty(), obstaclePos, severity);
    }
    
    /**
     * 检测特殊地形
     */
    private ObstacleDetectionResult detectSpecialTerrain(LevelAccessor level, BlockPos position) {
        Set<ObstacleType> obstacles = new HashSet<>();
        BlockPos obstaclePos = null;
        double severity = 0.0;
        
        // 检测雪原地形
        BlockState state = level.getBlockState(position);
        if (state.getBlock().equals(Blocks.SNOW_BLOCK) || state.getBlock().equals(Blocks.POWDER_SNOW)) {
            obstacles.add(ObstacleType.SNOW);
            obstaclePos = position;
            severity = 0.4; // 雪原地形严重性较低
        }
        
        return new ObstacleDetectionResult(!obstacles.isEmpty(), obstacles, 
                                          !obstacles.isEmpty(), obstaclePos, severity);
    }
    
    /**
     * 生成绕行路径
     */
    public List<BlockPos> generateReroutePath(LevelAccessor level, BlockPos currentPos, 
                                             BlockPos prevPos, BlockPos nextPos, 
                                             ObstacleDetectionResult obstacleResult) {
        List<BlockPos> reroutePath = new ArrayList<>();
        
        // 根据障碍物类型选择绕行策略
        Set<ObstacleType> obstacleTypes = obstacleResult.getObstacleTypes();
        BlockPos obstaclePos = obstacleResult.getObstaclePosition();
        
        if (obstacleTypes.contains(ObstacleType.TREE) || obstacleTypes.contains(ObstacleType.STRUCTURE)) {
            // 对于树木和结构，生成简单的绕行路径
            reroutePath = generateSimpleBypass(level, currentPos, prevPos, nextPos, obstaclePos);
        } else if (obstacleTypes.contains(ObstacleType.WATER) || obstacleTypes.contains(ObstacleType.RAVINE)) {
            // 对于水流和峡谷，生成桥梁路径
            reroutePath = generateBridgePath(level, currentPos, prevPos, nextPos, obstaclePos);
        } else if (obstacleTypes.contains(ObstacleType.CROPS)) {
            // 对于庄稼，生成保护性绕行路径
            reroutePath = generateCropProtectionPath(level, currentPos, prevPos, nextPos, obstaclePos);
        } else if (obstacleTypes.contains(ObstacleType.NARROW_PASSAGE)) {
            // 对于狭窄通道，生成宽度适配路径
            reroutePath = generateWidthAdaptationPath(level, currentPos, prevPos, nextPos);
        }
        
        LOGGER.debug("Generated reroute path with {} segments for obstacle at {}", reroutePath.size(), obstaclePos);
        return reroutePath;
    }
    
    /**
     * 生成简单绕行路径
     */
    private List<BlockPos> generateSimpleBypass(LevelAccessor level, BlockPos currentPos, 
                                               BlockPos prevPos, BlockPos nextPos, BlockPos obstaclePos) {
        List<BlockPos> path = new ArrayList<>();
        
        // 计算方向向量
        int dx = nextPos.getX() - prevPos.getX();
        int dz = nextPos.getZ() - prevPos.getZ();
        
        // 计算垂直方向向量
        int perpDx = -dz;
        int perpDz = dx;
        
        // 尝试两个方向的绕行
        BlockPos bypassPos1 = currentPos.offset(perpDx, 0, perpDz);
        BlockPos bypassPos2 = currentPos.offset(-perpDx, 0, -perpDz);
        
        // 选择更安全的绕行路径
        BlockPos chosenBypass = isPositionSafeForRoad(level, bypassPos1) ? bypassPos1 : bypassPos2;
        if (isPositionSafeForRoad(level, chosenBypass)) {
            path.add(chosenBypass);
        } else {
            // 如果两个方向都不安全，尝试更远的绕行
            chosenBypass = currentPos.offset(perpDx * 2, 0, perpDz * 2);
            if (isPositionSafeForRoad(level, chosenBypass)) {
                path.add(chosenBypass);
            }
        }
        
        return path;
    }
    
    /**
     * 生成桥梁路径
     */
    private List<BlockPos> generateBridgePath(LevelAccessor level, BlockPos currentPos, 
                                             BlockPos prevPos, BlockPos nextPos, BlockPos obstaclePos) {
        List<BlockPos> path = new ArrayList<>();
        
        // 计算方向向量
        int dx = nextPos.getX() - prevPos.getX();
        int dz = nextPos.getZ() - prevPos.getZ();
        
        // 生成桥梁支撑点
        int bridgeLength = 3; // 桥梁长度
        for (int i = 1; i <= bridgeLength; i++) {
            BlockPos bridgePos = currentPos.offset(dx * i / bridgeLength, 0, dz * i / bridgeLength);
            
            // 调整桥梁高度以适应地形
            int bridgeHeight = calculateBridgeHeight(level, bridgePos);
            BlockPos adjustedPos = bridgePos.atY(bridgeHeight);
            
            path.add(adjustedPos);
        }
        
        return path;
    }
    
    /**
     * 生成庄稼保护路径
     */
    private List<BlockPos> generateCropProtectionPath(LevelAccessor level, BlockPos currentPos, 
                                                     BlockPos prevPos, BlockPos nextPos, BlockPos obstaclePos) {
        List<BlockPos> path = new ArrayList<>();
        
        // 计算方向向量
        int dx = nextPos.getX() - prevPos.getX();
        int dz = nextPos.getZ() - prevPos.getZ();
        
        // 计算垂直方向向量
        int perpDx = -dz;
        int perpDz = dx;
        
        // 生成远离庄稼的绕行路径
        int protectionDistance = 6; // 保护距离
        BlockPos safePos = currentPos.offset(perpDx * protectionDistance, 0, perpDz * protectionDistance);
        
        if (isPositionSafeForRoad(level, safePos) && !isNearCrops(level, safePos)) {
            path.add(safePos);
        }
        
        return path;
    }
    
    /**
     * 生成宽度适配路径
     */
    private List<BlockPos> generateWidthAdaptationPath(LevelAccessor level, BlockPos currentPos, 
                                                      BlockPos prevPos, BlockPos nextPos) {
        List<BlockPos> path = new ArrayList<>();
        
        // 计算方向向量
        int dx = nextPos.getX() - prevPos.getX();
        int dz = nextPos.getZ() - prevPos.getZ();
        
        // 在狭窄区域生成单行道路
        path.add(currentPos);
        
        return path;
    }
    
    /**
     * 判断位置是否适合道路
     */
    private boolean isPositionSafeForRoad(LevelAccessor level, BlockPos position) {
        // 检查是否在水面或岩浆上
        BlockState state = level.getBlockState(position);
        if (state.getFluidState().getType().isSame(Fluids.WATER) || 
            state.getFluidState().getType().isSame(Fluids.LAVA)) {
            return false;
        }
        
        // 检查是否在树木或庄稼上
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || CROP_BLOCKS.contains(state.getBlock())) {
            return false;
        }
        
        // 检查地形稳定性
        int currentHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, position.getX(), position.getZ());
        return Math.abs(position.getY() - currentHeight) <= 2;
    }
    
    /**
     * 判断是否是红杉树
     */
    private boolean isRedwoodTree(LevelAccessor level, BlockPos position) {
        // 检测大型树木特征（高度和直径）
        int treeHeight = 0;
        BlockPos checkPos = position;
        
        // 向上检测树木高度
        while (level.getBlockState(checkPos).is(BlockTags.LOGS)) {
            treeHeight++;
            checkPos = checkPos.above();
        }
        
        // 如果树木高度超过阈值，认为是红杉
        return treeHeight > 8;
    }
    
    /**
     * 判断是否是村庄农田
     */
    private boolean isVillageFarmland(LevelAccessor level, BlockPos position) {
        // 检测周围的村庄特征
        for (int x = -VILLAGE_PROTECTION_RADIUS; x <= VILLAGE_PROTECTION_RADIUS; x++) {
            for (int z = -VILLAGE_PROTECTION_RADIUS; z <= VILLAGE_PROTECTION_RADIUS; z++) {
                BlockPos checkPos = position.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);
                
                // 检测村庄相关方块
                if (VILLAGE_BLOCKS.contains(state.getBlock())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 计算可通行宽度
     */
    private int calculatePassableWidth(LevelAccessor level, BlockPos position) {
        int passableWidth = 0;
        
        // 计算垂直方向的通行宽度
        int dx = 1; // 假设道路方向
        int dz = 0;
        
        int perpDx = -dz;
        int perpDz = dx;
        
        // 检测两侧的通行空间
        for (int offset = -2; offset <= 2; offset++) {
            BlockPos checkPos = position.offset(perpDx * offset, 0, perpDz * offset);
            if (isPositionSafeForRoad(level, checkPos)) {
                passableWidth++;
            }
        }
        
        return passableWidth;
    }
    
    /**
     * 计算桥梁高度
     */
    private int calculateBridgeHeight(LevelAccessor level, BlockPos position) {
        // 获取周围地形高度
        int maxHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, position.getX(), position.getZ());
        
        // 桥梁高度比周围地形高2-3格
        return maxHeight + 2;
    }
    
    /**
     * 判断是否靠近庄稼
     */
    private boolean isNearCrops(LevelAccessor level, BlockPos position) {
        for (int x = -CROP_PROTECTION_RADIUS; x <= CROP_PROTECTION_RADIUS; x++) {
            for (int z = -CROP_PROTECTION_RADIUS; z <= CROP_PROTECTION_RADIUS; z++) {
                BlockPos checkPos = position.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);
                
                if (CROP_BLOCKS.contains(state.getBlock())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 决定是否需要绕行
     */
    private boolean shouldRerouteForObstacles(Set<ObstacleType> obstacles, double severity, int roadType) {
        // 根据障碍物类型和严重性决定是否需要绕行
        if (severity >= 0.7) {
            return true; // 高严重性障碍物需要绕行
        }
        
        // 特定障碍物类型需要绕行
        for (ObstacleType type : obstacles) {
            if (type == ObstacleType.CROPS || type == ObstacleType.STRUCTURE || type == ObstacleType.RAVINE) {
                return true;
            }
        }
        
        // 高级道路类型可以处理更多障碍物
        if (roadType > 0 && severity < 0.5) {
            return false; // 高级道路可以处理低严重性障碍物
        }
        
        return severity >= 0.5; // 中等严重性障碍物需要绕行
    }
}
