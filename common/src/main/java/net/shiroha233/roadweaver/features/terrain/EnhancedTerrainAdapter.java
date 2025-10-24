package net.shiroha233.roadweaver.features.terrain;

import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.shiroha233.roadweaver.helpers.Records;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 增强型地形适配器
 * 
 * 提供智能的地形处理功能：
 * - 高低差台阶替换算法
 * - 智能坡度适应
 * - 地形稳定性分析
 * - 生物群系边界检测
 */
public class EnhancedTerrainAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    
    // 地形适配常量
    private static final int MAX_STEP_HEIGHT = 3; // 最大台阶高度
    private static final int SLOPE_ANALYSIS_RADIUS = 5; // 坡度分析半径
    private static final double MAX_SLOPE_GRADIENT = 0.5; // 最大坡度梯度
    
    // 台阶替换材料
    private static final List<BlockState> STEP_MATERIALS = Arrays.asList(
        Blocks.STONE_BRICKS.defaultBlockState(),
        Blocks.COBBLESTONE.defaultBlockState(),
        Blocks.ANDESITE.defaultBlockState()
    );
    
    /**
     * 分析地形并生成台阶替换方案
     * 
     * @param level 世界
     * @param startPos 起始位置
     * @param endPos 结束位置
     * @param roadWidth 道路宽度
     * @return 台阶替换方案列表
     */
    public static List<Records.TerrainStep> analyzeTerrainSteps(ServerLevel level, BlockPos startPos, BlockPos endPos, int roadWidth) {
        List<Records.TerrainStep> steps = new ArrayList<>();
        IModConfig config = ConfigProvider.get();
        
        // 计算路径上的关键点
        List<BlockPos> pathPoints = calculatePathPoints(startPos, endPos, roadWidth);
        
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            BlockPos current = pathPoints.get(i);
            BlockPos next = pathPoints.get(i + 1);
            
            int currentHeight = getSurfaceHeight(level, current);
            int nextHeight = getSurfaceHeight(level, next);
            int heightDiff = Math.abs(currentHeight - nextHeight);
            
            // 如果高度差超过阈值，需要台阶处理
            if (heightDiff > MAX_STEP_HEIGHT) {
                Records.TerrainStep step = createStepSolution(level, current, next, currentHeight, nextHeight, roadWidth);
                if (step != null) {
                    steps.add(step);
                    LOGGER.debug("检测到需要台阶处理的位置: {} -> {} (高度差: {})", current, next, heightDiff);
                }
            }
        }
        
        return steps;
    }
    
    /**
     * 应用台阶替换方案
     * 
     * @param level 世界
     * @param steps 台阶方案列表
     * @param materials 道路材料
     */
    public static void applyTerrainSteps(ServerLevel level, List<Records.TerrainStep> steps, List<BlockState> materials) {
        for (Records.TerrainStep step : steps) {
            applySingleStep(level, step, materials);
        }
    }
    
    /**
     * 分析坡度并生成适应方案
     * 
     * @param level 世界
     * @param position 分析位置
     * @param roadWidth 道路宽度
     * @return 坡度适应方案
     */
    public static Records.SlopeAdaptation analyzeSlope(ServerLevel level, BlockPos position, int roadWidth) {
        double slopeGradient = calculateSlopeGradient(level, position);
        boolean needsAdaptation = slopeGradient > MAX_SLOPE_GRADIENT;
        
        List<BlockPos> affectedPositions = new ArrayList<>();
        if (needsAdaptation) {
            affectedPositions = getSlopeAffectedPositions(level, position, roadWidth);
        }
        
        return new Records.SlopeAdaptation(
            position,
            slopeGradient,
            needsAdaptation,
            affectedPositions,
            getAdaptationStrategy(slopeGradient)
        );
    }
    
    /**
     * 应用坡度适应方案
     * 
     * @param level 世界
     * @param adaptation 适应方案
     * @param materials 道路材料
     */
    public static void applySlopeAdaptation(ServerLevel level, Records.SlopeAdaptation adaptation, List<BlockState> materials) {
        if (!adaptation.needsAdaptation()) {
            return;
        }
        
        for (BlockPos pos : adaptation.affectedPositions()) {
            int surfaceHeight = getSurfaceHeight(level, pos);
            BlockPos surfacePos = new BlockPos(pos.getX(), surfaceHeight, pos.getZ());
            
            // 根据坡度策略调整道路放置
            switch (adaptation.strategy()) {
                case "terracing":
                    applyTerracing(level, surfacePos, materials);
                    break;
                case "grading":
                    applyGrading(level, surfacePos, materials);
                    break;
                case "retaining_wall":
                    applyRetainingWall(level, surfacePos, materials);
                    break;
            }
        }
    }
    
    /**
     * 检测生物群系边界
     * 
     * @param level 世界
     * @param startPos 起始位置
     * @param endPos 结束位置
     * @param roadWidth 道路宽度
     * @return 生物群系边界列表
     */
    public static List<Records.BiomeBoundary> detectBiomeBoundaries(ServerLevel level, BlockPos startPos, BlockPos endPos, int roadWidth) {
        List<Records.BiomeBoundary> boundaries = new ArrayList<>();
        List<BlockPos> pathPoints = calculatePathPoints(startPos, endPos, roadWidth);
        
        String previousBiome = getBiomeId(level, pathPoints.get(0));
        
        for (int i = 1; i < pathPoints.size(); i++) {
            BlockPos current = pathPoints.get(i);
            String currentBiome = getBiomeId(level, current);
            
            if (!currentBiome.equals(previousBiome)) {
                // 检测到生物群系边界
                Records.BiomeBoundary boundary = new Records.BiomeBoundary(
                    current,
                    previousBiome,
                    currentBiome,
                    calculateTransitionWidth(level, current, previousBiome, currentBiome)
                );
                boundaries.add(boundary);
                previousBiome = currentBiome;
            }
        }
        
        return boundaries;
    }
    
    // ========== 私有辅助方法 ==========
    
    private static List<BlockPos> calculatePathPoints(BlockPos start, BlockPos end, int width) {
        List<BlockPos> points = new ArrayList<>();
        int steps = (int) Math.sqrt(start.distSqr(end)) / 4; // 每4格一个采样点
        
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) (start.getX() + t * (end.getX() - start.getX()));
            int z = (int) (start.getZ() + t * (end.getZ() - start.getZ()));
            
            // 考虑道路宽度，在宽度方向上采样多个点
            for (int w = -width/2; w <= width/2; w += 2) {
                points.add(new BlockPos(x + w, 0, z));
            }
        }
        
        return points;
    }
    
    private static int getSurfaceHeight(ServerLevel level, BlockPos pos) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
    }
    
    private static Records.TerrainStep createStepSolution(ServerLevel level, BlockPos from, BlockPos to, 
                                                         int fromHeight, int toHeight, int roadWidth) {
        int heightDiff = Math.abs(fromHeight - toHeight);
        int stepCount = (int) Math.ceil((double) heightDiff / MAX_STEP_HEIGHT);
        int stepHeight = heightDiff / stepCount;
        
        List<BlockPos> stepPositions = new ArrayList<>();
        Direction direction = getDirection(from, to);
        
        // 计算台阶位置
        for (int i = 1; i < stepCount; i++) {
            BlockPos stepPos = from.relative(direction, i * 4); // 每4格一个台阶
            stepPositions.add(stepPos);
        }
        
        return new Records.TerrainStep(
            from, to, fromHeight, toHeight, stepCount, stepHeight, stepPositions
        );
    }
    
    private static void applySingleStep(ServerLevel level, Records.TerrainStep step, List<BlockState> materials) {
        for (BlockPos stepPos : step.stepPositions()) {
            int surfaceHeight = getSurfaceHeight(level, stepPos);
            BlockPos placementPos = new BlockPos(stepPos.getX(), surfaceHeight, stepPos.getZ());
            
            // 放置台阶材料
            BlockState stepMaterial = STEP_MATERIALS.get(new Random().nextInt(STEP_MATERIALS.size()));
            level.setBlock(placementPos.below(), stepMaterial, 3);
            
            // 清理台阶上方的方块
            for (int i = 0; i < 2; i++) {
                BlockPos abovePos = placementPos.above(i);
                if (!level.getBlockState(abovePos).isAir()) {
                    level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }
    
    private static double calculateSlopeGradient(ServerLevel level, BlockPos center) {
        double totalSlope = 0;
        int sampleCount = 0;
        
        for (int dx = -SLOPE_ANALYSIS_RADIUS; dx <= SLOPE_ANALYSIS_RADIUS; dx++) {
            for (int dz = -SLOPE_ANALYSIS_RADIUS; dz <= SLOPE_ANALYSIS_RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                BlockPos samplePos = center.offset(dx, 0, dz);
                int centerHeight = getSurfaceHeight(level, center);
                int sampleHeight = getSurfaceHeight(level, samplePos);
                
                double distance = Math.sqrt(dx * dx + dz * dz);
                double slope = Math.abs(sampleHeight - centerHeight) / distance;
                
                totalSlope += slope;
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? totalSlope / sampleCount : 0;
    }
    
    private static List<BlockPos> getSlopeAffectedPositions(ServerLevel level, BlockPos center, int roadWidth) {
        List<BlockPos> positions = new ArrayList<>();
        int halfWidth = roadWidth / 2;
        
        for (int dx = -halfWidth; dx <= halfWidth; dx++) {
            for (int dz = -halfWidth; dz <= halfWidth; dz++) {
                positions.add(center.offset(dx, 0, dz));
            }
        }
        
        return positions;
    }
    
    private static String getAdaptationStrategy(double slopeGradient) {
        if (slopeGradient > 0.8) {
            return "retaining_wall";
        } else if (slopeGradient > 0.5) {
            return "terracing";
        } else {
            return "grading";
        }
    }
    
    private static void applyTerracing(ServerLevel level, BlockPos pos, List<BlockState> materials) {
        // 创建梯田式平台
        int terraceWidth = 3;
        for (int dx = -terraceWidth; dx <= terraceWidth; dx++) {
            for (int dz = -terraceWidth; dz <= terraceWidth; dz++) {
                BlockPos terracePos = pos.offset(dx, 0, dz);
                int terraceHeight = getSurfaceHeight(level, terracePos);
                BlockPos placementPos = new BlockPos(terracePos.getX(), terraceHeight, terracePos.getZ());
                
                // 使用道路材料创建平台
                BlockState material = materials.get(new Random().nextInt(materials.size()));
                level.setBlock(placementPos.below(), material, 3);
            }
        }
    }
    
    private static void applyGrading(ServerLevel level, BlockPos pos, List<BlockState> materials) {
        // 简单的坡度平整
        BlockState material = materials.get(new Random().nextInt(materials.size()));
        level.setBlock(pos.below(), material, 3);
    }
    
    private static void applyRetainingWall(ServerLevel level, BlockPos pos, List<BlockState> materials) {
        // 创建挡土墙
        int wallHeight = 3;
        for (int dy = 0; dy < wallHeight; dy++) {
            BlockPos wallPos = pos.below(dy);
            BlockState wallMaterial = STEP_MATERIALS.get(new Random().nextInt(STEP_MATERIALS.size()));
            level.setBlock(wallPos, wallMaterial, 3);
        }
    }
    
    private static Direction getDirection(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
    
    private static String getBiomeId(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey()
            .map(key -> key.location().toString())
            .orElse("unknown");
    }
    
    private static int calculateTransitionWidth(ServerLevel level, BlockPos boundaryPos, String fromBiome, String toBiome) {
        // 根据生物群系类型计算过渡宽度
        // 这里可以根据生物群系特性返回不同的过渡宽度
        return 8; // 默认8格过渡宽度
    }
    
    // ========== RoadFeature集成方法 ==========
    
    /**
     * 适配地形 - 用于RoadFeature集成
     * 
     * @param level 世界
     * @param currentPos 当前位置
     * @param prevPos 前一个位置
     * @param nextPos 下一个位置
     * @return 适配后的位置
     */
    public BlockPos adaptToTerrain(net.minecraft.world.level.WorldGenLevel level, BlockPos currentPos, BlockPos prevPos, BlockPos nextPos) {
        // 分析当前地形坡度
        double slopeGradient = calculateSlopeGradientForWorldGenLevel(level, currentPos);
        
        // 如果坡度较大，进行高度调整
        if (slopeGradient > MAX_SLOPE_GRADIENT) {
            // 计算周围地形平均高度
            int totalHeight = 0;
            int count = 0;
            
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos samplePos = currentPos.offset(dx, 0, dz);
                    int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, samplePos.getX(), samplePos.getZ());
                    totalHeight += height;
                    count++;
                }
            }
            
            int averageHeight = totalHeight / count;
            return new BlockPos(currentPos.getX(), averageHeight, currentPos.getZ());
        }
        
        return currentPos;
    }
    
    /**
     * 获取周围位置用于坡度分析
     * 
     * @param center 中心位置
     * @param radius 半径
     * @return 周围位置数组
     */
    public BlockPos[] getSurroundingPositions(BlockPos center, int radius) {
        List<BlockPos> positions = new ArrayList<>();
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                positions.add(center.offset(dx, 0, dz));
            }
        }
        
        return positions.toArray(new BlockPos[0]);
    }
    
    /**
     * 应用坡度适应 - 用于RoadFeature集成
     * 
     * @param level 世界
     * @param position 位置
     * @param surroundingPositions 周围位置
     * @return 适应后的位置
     */
    public BlockPos applySlopeAdaptation(net.minecraft.world.level.WorldGenLevel level, BlockPos position, BlockPos[] surroundingPositions) {
        double slopeGradient = calculateSlopeGradientForWorldGenLevel(level, position);
        
        // 如果坡度较大，调整到平均高度
        if (slopeGradient > MAX_SLOPE_GRADIENT) {
            int totalHeight = 0;
            int count = 0;
            
            for (BlockPos pos : surroundingPositions) {
                int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
                totalHeight += height;
                count++;
            }
            
            if (count > 0) {
                int averageHeight = totalHeight / count;
                return new BlockPos(position.getX(), averageHeight, position.getZ());
            }
        }
        
        return position;
    }
    
    /**
     * 应用台阶替换算法 - 用于RoadFeature集成
     * 
     * @param level 世界
     * @param position 位置
     * @param materials 道路材料
     * @param random 随机源
     */
    public void applyStepReplacement(net.minecraft.world.level.WorldGenLevel level, BlockPos position, List<BlockState> materials, net.minecraft.util.RandomSource random) {
        // 检查周围地形高度差
        BlockPos[] surrounding = getSurroundingPositions(position, 2);
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        
        for (BlockPos pos : surrounding) {
            int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
            minHeight = Math.min(minHeight, height);
            maxHeight = Math.max(maxHeight, height);
        }
        
        int heightDiff = maxHeight - minHeight;
        
        // 如果高度差超过阈值，创建台阶
        if (heightDiff > MAX_STEP_HEIGHT) {
            LOGGER.debug("在位置 {} 应用台阶替换 (高度差: {})", position, heightDiff);
            
            // 创建台阶
            for (int i = 0; i <= heightDiff; i++) {
                int stepHeight = minHeight + i;
                BlockPos stepPos = new BlockPos(position.getX(), stepHeight, position.getZ());
                
                // 使用台阶材料
                BlockState stepMaterial = STEP_MATERIALS.get(random.nextInt(STEP_MATERIALS.size()));
                level.setBlock(stepPos.below(), stepMaterial, 3);
                
                // 清理台阶上方的方块
                for (int j = 0; j < 2; j++) {
                    BlockPos abovePos = stepPos.above(j);
                    if (!level.getBlockState(abovePos).isAir()) {
                        level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        } else {
            // 正常放置道路
            BlockState material = materials.get(random.nextInt(materials.size()));
            level.setBlock(position.below(), material, 3);
        }
    }
    
    // ========== 缺失的类和方法 ==========
    
    /**
     * 地形分析结果类
     */
    public static class TerrainAnalysis {
        private final BlockPos position;
        private final double slopeGradient;
        private final double elevation;
        private final double roughness;
        private final boolean isSteep;
        private final boolean isFlat;
        private final List<BlockPos> problematicAreas;
        
        public TerrainAnalysis(BlockPos position, double slopeGradient, double elevation, 
                              double roughness, boolean isSteep, boolean isFlat, 
                              List<BlockPos> problematicAreas) {
            this.position = position;
            this.slopeGradient = slopeGradient;
            this.elevation = elevation;
            this.roughness = roughness;
            this.isSteep = isSteep;
            this.isFlat = isFlat;
            this.problematicAreas = problematicAreas;
        }
        
        public BlockPos getPosition() { return position; }
        public double getSlopeGradient() { return slopeGradient; }
        public double getElevation() { return elevation; }
        public double getRoughness() { return roughness; }
        public boolean isSteep() { return isSteep; }
        public boolean isFlat() { return isFlat; }
        public List<BlockPos> getProblematicAreas() { return problematicAreas; }
    }
    
    /**
     * 分析地形 - 用于BiomeConnectionStrategy集成
     * 
     * @param level 世界
     * @param position 分析位置
     * @param radius 分析半径
     * @return 地形分析结果
     */
    public TerrainAnalysis analyzeTerrain(LevelAccessor level, BlockPos position, int radius) {
        // 对于LevelAccessor，直接使用它而不是转换为ServerLevel
        // 计算坡度梯度
        double slopeGradient = calculateSlopeGradientForLevelAccessor(level, position);
        
        // 计算平均海拔
        double elevation = getSurfaceHeightForLevelAccessor(level, position);
        
        // 计算地形粗糙度
        double roughness = calculateTerrainRoughnessForLevelAccessor(level, position, radius);
        
        // 判断地形类型
        boolean isSteep = slopeGradient > MAX_SLOPE_GRADIENT;
        boolean isFlat = slopeGradient < 0.1;
        
        // 检测问题区域
        List<BlockPos> problematicAreas = detectProblematicAreasForLevelAccessor(level, position, radius);
        
        return new TerrainAnalysis(
            position, slopeGradient, elevation, roughness, isSteep, isFlat, problematicAreas
        );
    }
    
    /**
     * 计算地形粗糙度
     * 
     * @param level 世界
     * @param center 中心位置
     * @param radius 半径
     * @return 地形粗糙度值
     */
    private static double calculateTerrainRoughness(ServerLevel level, BlockPos center, int radius) {
        double totalVariation = 0;
        int sampleCount = 0;
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                BlockPos samplePos = center.offset(dx, 0, dz);
                int centerHeight = getSurfaceHeight(level, center);
                int sampleHeight = getSurfaceHeight(level, samplePos);
                
                double heightDiff = Math.abs(sampleHeight - centerHeight);
                totalVariation += heightDiff;
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? totalVariation / sampleCount : 0;
    }
    
    /**
     * 检测问题区域
     * 
     * @param level 世界
     * @param center 中心位置
     * @param radius 半径
     * @return 问题区域列表
     */
    private static List<BlockPos> detectProblematicAreas(ServerLevel level, BlockPos center, int radius) {
        List<BlockPos> problematicAreas = new ArrayList<>();
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos samplePos = center.offset(dx, 0, dz);
                double slopeGradient = calculateSlopeGradient(level, samplePos);
                
                // 如果坡度过大或地形过于崎岖，标记为问题区域
                if (slopeGradient > MAX_SLOPE_GRADIENT * 1.5) {
                    problematicAreas.add(samplePos);
                }
            }
        }
        
        return problematicAreas;
    }
    
    // ========== LevelAccessor兼容方法 ==========
    
    /**
     * 为LevelAccessor计算坡度梯度
     */
    private static double calculateSlopeGradientForLevelAccessor(LevelAccessor level, BlockPos center) {
        double totalSlope = 0;
        int sampleCount = 0;
        
        for (int dx = -SLOPE_ANALYSIS_RADIUS; dx <= SLOPE_ANALYSIS_RADIUS; dx++) {
            for (int dz = -SLOPE_ANALYSIS_RADIUS; dz <= SLOPE_ANALYSIS_RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                BlockPos samplePos = center.offset(dx, 0, dz);
                int centerHeight = getSurfaceHeightForLevelAccessor(level, center);
                int sampleHeight = getSurfaceHeightForLevelAccessor(level, samplePos);
                
                double distance = Math.sqrt(dx * dx + dz * dz);
                double slope = Math.abs(sampleHeight - centerHeight) / distance;
                
                totalSlope += slope;
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? totalSlope / sampleCount : 0;
    }
    
    /**
     * 为LevelAccessor获取地表高度
     */
    private static int getSurfaceHeightForLevelAccessor(LevelAccessor level, BlockPos pos) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
    }
    
    /**
     * 为LevelAccessor计算地形粗糙度
     */
    private static double calculateTerrainRoughnessForLevelAccessor(LevelAccessor level, BlockPos center, int radius) {
        double totalVariation = 0;
        int sampleCount = 0;
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                BlockPos samplePos = center.offset(dx, 0, dz);
                int centerHeight = getSurfaceHeightForLevelAccessor(level, center);
                int sampleHeight = getSurfaceHeightForLevelAccessor(level, samplePos);
                
                double heightDiff = Math.abs(sampleHeight - centerHeight);
                totalVariation += heightDiff;
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? totalVariation / sampleCount : 0;
    }
    
    /**
     * 为LevelAccessor检测问题区域
     */
    private static List<BlockPos> detectProblematicAreasForLevelAccessor(LevelAccessor level, BlockPos center, int radius) {
        List<BlockPos> problematicAreas = new ArrayList<>();
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos samplePos = center.offset(dx, 0, dz);
                double slopeGradient = calculateSlopeGradientForLevelAccessor(level, samplePos);
                
                // 如果坡度过大或地形过于崎岖，标记为问题区域
                if (slopeGradient > MAX_SLOPE_GRADIENT * 1.5) {
                    problematicAreas.add(samplePos);
                }
            }
        }
        
        return problematicAreas;
    }
    
    // ========== 缺失的方法 ==========
    
    /**
     * 应用地形适应 - 用于BiomeConnectionStrategy集成
     * 
     * @param level 世界
     * @param analysis 地形分析结果
     * @param materials 道路材料
     */
    public void applyTerrainAdaptation(LevelAccessor level, TerrainAnalysis analysis, List<BlockState> materials) {
        if (analysis.isSteep()) {
            // 对于陡峭地形，应用坡度适应
            applySlopeAdaptationForLevelAccessor(level, analysis.getPosition(), materials);
        }
        
        // 处理问题区域
        for (BlockPos problemPos : analysis.getProblematicAreas()) {
            applyProblemAreaAdaptation(level, problemPos, materials);
        }
    }
    
    /**
     * 获取高度差 - 用于TerrainAnalysis类
     * 
     * @param analysis 地形分析结果
     * @param otherPos 其他位置
     * @return 高度差
     */
    public double getHeightDifference(TerrainAnalysis analysis, BlockPos otherPos) {
        // 简化实现，返回分析位置和目标位置之间的Y坐标差
        return Math.abs(analysis.getPosition().getY() - otherPos.getY());
    }
    
    /**
     * 为LevelAccessor应用坡度适应
     */
    private static void applySlopeAdaptationForLevelAccessor(LevelAccessor level, BlockPos position, List<BlockState> materials) {
        // 简单的坡度平整
        BlockState material = materials.get(new Random().nextInt(materials.size()));
        level.setBlock(position.below(), material, 3);
    }
    
    /**
     * 处理问题区域适应
     */
    private static void applyProblemAreaAdaptation(LevelAccessor level, BlockPos position, List<BlockState> materials) {
        // 对于问题区域，使用更坚固的材料
        BlockState material = STEP_MATERIALS.get(new Random().nextInt(STEP_MATERIALS.size()));
        level.setBlock(position.below(), material, 3);
    }
    
    /**
     * 获取地表高度的重载方法
     */
    private static int getSurfaceHeightForLevelAccessor(BlockPos center, BlockPos otherPos) {
        // 简化实现，实际中需要访问LevelAccessor
        // 这里返回一个默认值
        return center.getY();
    }
    
    // ========== BiomeConnectionStrategy集成方法 ==========
    
    /**
     * 应用地形适应 - 用于BiomeConnectionStrategy集成
     * 
     * @param level 世界
     * @param position 位置
     * @param analysis 地形分析结果
     * @return 适应后的位置列表
     */
    public List<BlockPos> applyTerrainAdaptation(LevelAccessor level, BlockPos position, TerrainAnalysis analysis) {
        List<BlockPos> adaptedPositions = new ArrayList<>();
        
        // 根据地形分析结果应用适应
        if (analysis.isSteep()) {
            // 对于陡峭地形，创建台阶或平台
            adaptedPositions.addAll(createTerracingForSteepTerrain(level, position, analysis));
        } else if (analysis.isFlat()) {
            // 对于平坦地形，直接使用原位置
            adaptedPositions.add(position);
        } else {
            // 对于一般地形，进行轻微调整
            adaptedPositions.addAll(createGradedAdaptation(level, position, analysis));
        }
        
        // 处理问题区域
        for (BlockPos problemPos : analysis.getProblematicAreas()) {
            adaptedPositions.addAll(adaptProblematicArea(level, problemPos));
        }
        
        return adaptedPositions;
    }
    
    /**
     * 获取高度差 - 用于TerrainAnalysis类
     * 
     * @param analysis 地形分析结果
     * @return 最大高度差
     */
    public double getHeightDifference(TerrainAnalysis analysis) {
        // 计算分析区域内的高度差
        double maxHeight = analysis.getElevation();
        double minHeight = maxHeight;
        
        // 通过问题区域估算高度变化
        for (BlockPos problemPos : analysis.getProblematicAreas()) {
            double problemHeight = getSurfaceHeightForLevelAccessor(problemPos, problemPos);
            maxHeight = Math.max(maxHeight, problemHeight);
            minHeight = Math.min(minHeight, problemHeight);
        }
        
        return maxHeight - minHeight;
    }
    
    /**
     * 为陡峭地形创建梯田
     */
    private List<BlockPos> createTerracingForSteepTerrain(LevelAccessor level, BlockPos position, TerrainAnalysis analysis) {
        List<BlockPos> terracePositions = new ArrayList<>();
        int terraceWidth = 3;
        
        // 创建梯田式平台
        for (int dx = -terraceWidth; dx <= terraceWidth; dx++) {
            for (int dz = -terraceWidth; dz <= terraceWidth; dz++) {
                BlockPos terracePos = position.offset(dx, 0, dz);
                int terraceHeight = getSurfaceHeightForLevelAccessor(level, terracePos);
                BlockPos placementPos = new BlockPos(terracePos.getX(), terraceHeight, terracePos.getZ());
                
                terracePositions.add(placementPos);
            }
        }
        
        return terracePositions;
    }
    
    /**
     * 创建分级适应
     */
    private List<BlockPos> createGradedAdaptation(LevelAccessor level, BlockPos position, TerrainAnalysis analysis) {
        List<BlockPos> gradedPositions = new ArrayList<>();
        
        // 根据坡度梯度创建分级适应
        double slope = analysis.getSlopeGradient();
        int adaptationRadius = (int) (slope * 2); // 坡度越大，适应范围越广
        
        for (int dx = -adaptationRadius; dx <= adaptationRadius; dx++) {
            for (int dz = -adaptationRadius; dz <= adaptationRadius; dz++) {
                BlockPos adaptedPos = position.offset(dx, 0, dz);
                int adaptedHeight = getSurfaceHeightForLevelAccessor(level, adaptedPos);
                BlockPos placementPos = new BlockPos(adaptedPos.getX(), adaptedHeight, adaptedPos.getZ());
                
                gradedPositions.add(placementPos);
            }
        }
        
        return gradedPositions;
    }
    
    /**
     * 适应问题区域
     */
    private List<BlockPos> adaptProblematicArea(LevelAccessor level, BlockPos problemPos) {
        List<BlockPos> adaptedPositions = new ArrayList<>();
        
        // 在问题区域周围创建支撑结构
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos supportPos = problemPos.offset(dx, 0, dz);
                int supportHeight = getSurfaceHeightForLevelAccessor(level, supportPos);
                BlockPos placementPos = new BlockPos(supportPos.getX(), supportHeight, supportPos.getZ());
                
                adaptedPositions.add(placementPos);
            }
        }
        
        return adaptedPositions;
    }
}
