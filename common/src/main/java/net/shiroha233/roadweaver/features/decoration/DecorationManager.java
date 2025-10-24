package net.shiroha233.roadweaver.features.decoration;

import net.shiroha233.roadweaver.features.config.DecorationConfig;
import net.shiroha233.roadweaver.features.biome.BiomeConnectionStrategy;
import net.shiroha233.roadweaver.features.roadlogic.Road;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 装饰管理器 - 负责处理装饰的智能放置和组合规则
 * 使用新的RoadDecoration接口系统
 */
public class DecorationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    
    private final DecorationConfig config;
    private final RandomSource random;
    private final LevelAccessor level;
    private final BiomeConnectionStrategy biomeStrategy;
    
    // 装饰实例缓存
    private final Map<String, RoadDecoration> decorationCache;
    
    // 装饰放置历史记录，用于避免重复放置
    private final Map<String, Set<BlockPos>> placementHistory;
    
    public DecorationManager(LevelAccessor level, RandomSource random) {
        this.config = DecorationConfig.getDefault();
        this.random = random;
        this.level = level;
        this.biomeStrategy = new BiomeConnectionStrategy();
        this.decorationCache = new HashMap<>();
        this.placementHistory = new HashMap<>();
        
        // 初始化装饰缓存
        initializeDecorationCache();
    }
    
    /**
     * 初始化装饰缓存
     */
    private void initializeDecorationCache() {
        // 创建所有装饰类型的实例
        decorationCache.put("bench", new BenchDecoration(new Random(random.nextLong()), config));
        decorationCache.put("fountain", new FountainDecoration(new Random(random.nextLong()), config));
        decorationCache.put("street_sign", new StreetSignDecoration(new Random(random.nextLong()), config));
        decorationCache.put("flower_bed", new FlowerBedDecoration(new Random(random.nextLong()), config));
        decorationCache.put("viewing_platform", new ViewingPlatformDecoration(new Random(random.nextLong()), config));
        decorationCache.put("gazebo", new GazeboDecoration(new Random(random.nextLong()), config));
        decorationCache.put("small_bridge", new SmallBridgeDecoration(new Random(random.nextLong()), config));
        decorationCache.put("statue", new StatueDecoration(new Random(random.nextLong()), config));
        decorationCache.put("small_fountain", new SmallFountainDecoration(new Random(random.nextLong()), config));
        decorationCache.put("clock_tower", new ClockTowerDecoration(new Random(random.nextLong()), config));
        decorationCache.put("bus_stop", new BusStopDecoration(new Random(random.nextLong()), config));
        decorationCache.put("mailbox", new MailboxDecoration(new Random(random.nextLong()), config));
        
        // 初始化放置历史记录
        for (String decorationName : decorationCache.keySet()) {
            placementHistory.put(decorationName, new HashSet<>());
        }
    }
    
    /**
     * 基于道路等级和生物群系智能生成装饰
     */
    public List<BlockPlacement> generateDecorations(Road road, BlockPos segmentPos, int segmentIndex) {
        List<BlockPlacement> placements = new ArrayList<>();
        
        // 获取生物群系信息
        String biomeType = biomeStrategy.getBiomeType();
        
        // 计算装饰密度
        double density = calculateDecorationDensity(road, biomeType);
        
        // 基于密度决定是否放置装饰
        if (random.nextDouble() > density) {
            return placements;
        }
        
        // 获取适合当前条件的装饰类型
        List<String> availableDecorations = getAvailableDecorations(road, biomeType, segmentIndex);
        
        if (availableDecorations.isEmpty()) {
            return placements;
        }
        
        // 选择装饰类型
        String selectedDecoration = selectDecoration(availableDecorations, road, biomeType);
        
        // 计算放置位置
        BlockPos placementPos = calculatePlacementPosition(segmentPos, road);
        
        // 检查位置是否有效
        if (!isValidPlacementPosition(placementPos, selectedDecoration, road)) {
            return placements;
        }
        
        // 检查组合规则
        if (!checkCompositionRules(selectedDecoration, placementPos)) {
            return placements;
        }
        
        // 生成装饰
        RoadDecoration decoration = decorationCache.get(selectedDecoration);
        if (decoration != null && decoration.canPlaceAt(level, placementPos, road, biomeStrategy)) {
            List<BlockPlacement> decorationPlacements = decoration.generateDecoration(level, placementPos, road, biomeStrategy);
            placements.addAll(decorationPlacements);
            
            // 记录放置历史
            placementHistory.get(selectedDecoration).add(placementPos);
        }
        
        return placements;
    }
    
    /**
     * 计算装饰密度 - 基于道路等级、生物群系和道路宽度
     */
    private double calculateDecorationDensity(Road road, String biomeType) {
        double baseDensity = config.getBaseDensity();
        
        // 根据道路等级调整密度
        switch (road.getWidthLevel()) {
            case HIGHWAY:
                baseDensity *= 0.1; // 高速公路很少装饰
                break;
            case MAIN_ROAD:
                baseDensity *= 0.3; // 主干道少量装饰
                break;
            case SECONDARY_ROAD:
                baseDensity *= 0.7; // 次干道适中装饰
                break;
            case RESIDENTIAL_ROAD:
                baseDensity *= 1.2; // 居民区道路较多装饰
                break;
            case NATURAL_PATH:
                baseDensity *= 0.8; // 自然路径适中装饰
                break;
        }
        
        // 应用生物群系乘数
        double biomeMultiplier = config.getBiomeDensityMultiplier(biomeType);
        baseDensity *= biomeMultiplier;
        
        // 应用随机因子
        double randomFactor = 0.8 + (random.nextDouble() * 0.4); // 0.8-1.2
        
        return baseDensity * randomFactor;
    }
    
    /**
     * 获取适合当前条件的装饰类型
     */
    private List<String> getAvailableDecorations(Road road, String biomeType, int segmentIndex) {
        List<String> availableDecorations = new ArrayList<>();
        
        for (Map.Entry<String, RoadDecoration> entry : decorationCache.entrySet()) {
            String decorationName = entry.getKey();
            RoadDecoration decoration = entry.getValue();
            
            // 检查放置概率
            double probability = decoration.getPlacementProbability(road, biomeStrategy);
            if (random.nextDouble() > probability) {
                continue;
            }
            
            // 检查道路等级兼容性
            if (!isDecorationAllowedForRoadLevel(decoration, road.getWidthLevel())) {
                continue;
            }
            
            // 检查生物群系兼容性
            if (!isDecorationAllowedForBiome(decoration, biomeType)) {
                continue;
            }
            
            // 检查放置策略
            if (!checkPlacementStrategy(decorationName, segmentIndex)) {
                continue;
            }
            
            availableDecorations.add(decorationName);
        }
        
        return availableDecorations;
    }
    
    /**
     * 检查装饰是否允许在当前道路等级放置
     */
    private boolean isDecorationAllowedForRoadLevel(RoadDecoration decoration, Road.WidthLevel widthLevel) {
        // 根据装饰类型和道路等级决定是否允许
        DecorationType type = decoration.getType();
        
        switch (type) {
            case STREET_FURNITURE:
                // 街道家具在所有道路等级都允许，但概率不同
                return true;
            case LANDSCAPE_FEATURE:
                // 景观特征在居民区道路和自然路径更常见
                return widthLevel == Road.WidthLevel.RESIDENTIAL_ROAD || 
                       widthLevel == Road.WidthLevel.NATURAL_PATH;
            case ARCHITECTURAL:
                // 建筑装饰在主干道和次干道更常见
                return widthLevel == Road.WidthLevel.MAIN_ROAD || 
                       widthLevel == Road.WidthLevel.SECONDARY_ROAD;
            case UTILITY:
                // 实用设施在所有道路等级都允许
                return true;
            default:
                return true;
        }
    }
    
    /**
     * 检查装饰是否允许在当前生物群系放置
     */
    private boolean isDecorationAllowedForBiome(RoadDecoration decoration, String biomeType) {
        // 根据装饰类型和生物群系决定是否允许
        DecorationType type = decoration.getType();
        
        switch (type) {
            case STREET_FURNITURE:
                // 街道家具在所有生物群系都允许
                return true;
            case LANDSCAPE_FEATURE:
                // 景观特征在自然生物群系更常见
                return !biomeType.contains("urban") && !biomeType.contains("city");
            case ARCHITECTURAL:
                // 建筑装饰在城市生物群系更常见
                return biomeType.contains("urban") || biomeType.contains("city") || 
                       biomeType.contains("plains") || biomeType.contains("forest");
            case UTILITY:
                // 实用设施在所有生物群系都允许
                return true;
            default:
                return true;
        }
    }
    
    /**
     * 检查放置策略
     */
    private boolean checkPlacementStrategy(String decorationName, int segmentIndex) {
        // 简单的放置策略检查
        // 可以根据需要扩展为更复杂的策略
        
        // 避免在道路起点和终点放置大型装饰
        if (segmentIndex < 3) {
            RoadDecoration decoration = decorationCache.get(decorationName);
            if (decoration.getType() == DecorationType.ARCHITECTURAL) {
                return false;
            }
        }
        
        // 基于装饰类型的间距检查
        RoadDecoration decoration = decorationCache.get(decorationName);
        int minDistance = decoration.getMinDistance();
        
        // 检查与同类型装饰的最小距离
        Set<BlockPos> history = placementHistory.get(decorationName);
        for (BlockPos pos : history) {
            // 这里简化处理，实际应该检查与当前段的位置关系
            if (segmentIndex % minDistance == 0) {
                return true;
            }
        }
        
        return random.nextDouble() < 0.5; // 50% 概率
    }
    
    /**
     * 选择装饰类型
     */
    private String selectDecoration(List<String> availableDecorations, Road road, String biomeType) {
        // 基于权重选择
        Map<String, Double> weights = new HashMap<>();
        
        for (String decorationName : availableDecorations) {
            RoadDecoration decoration = decorationCache.get(decorationName);
            double weight = 1.0;
            
            // 根据道路等级调整权重
            switch (road.getWidthLevel()) {
                case HIGHWAY:
                    weight *= 0.1;
                    break;
                case MAIN_ROAD:
                    weight *= 0.5;
                    break;
                case SECONDARY_ROAD:
                    weight *= 0.8;
                    break;
                case RESIDENTIAL_ROAD:
                    weight *= 1.2;
                    break;
                case NATURAL_PATH:
                    weight *= 1.0;
                    break;
            }
            
            // 根据装饰类型调整权重
            switch (decoration.getType()) {
                case STREET_FURNITURE:
                    weight *= 1.0;
                    break;
                case LANDSCAPE_FEATURE:
                    weight *= 0.8;
                    break;
                case ARCHITECTURAL:
                    weight *= 0.6;
                    break;
                case UTILITY:
                    weight *= 1.2;
                    break;
            }
            
            weights.put(decorationName, weight);
        }
        
        // 基于权重随机选择
        return selectByWeight(weights);
    }
    
    /**
     * 基于权重随机选择
     */
    private String selectByWeight(Map<String, Double> weights) {
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = random.nextDouble() * totalWeight;
        
        double currentWeight = 0.0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }
        
        // 默认返回第一个（理论上不会执行到这里）
        return weights.keySet().iterator().next();
    }
    
    /**
     * 计算放置位置
     */
    private BlockPos calculatePlacementPosition(BlockPos segmentPos, Road road) {
        // 随机选择道路的哪一侧
        boolean leftSide = random.nextBoolean();
        
        // 计算偏移距离（基于道路宽度）
        int roadWidth = road.getWidth();
        int offsetDistance = roadWidth / 2 + 2 + random.nextInt(3);
        
        // 根据道路方向计算偏移向量
        BlockPos offsetPos;
        switch (road.getDirection()) {
            case NORTH_SOUTH:
            case SOUTH_NORTH:
                offsetPos = leftSide ? 
                    segmentPos.offset(-offsetDistance, 0, 0) : 
                    segmentPos.offset(offsetDistance, 0, 0);
                break;
            case EAST_WEST:
            case WEST_EAST:
                offsetPos = leftSide ? 
                    segmentPos.offset(0, 0, -offsetDistance) : 
                    segmentPos.offset(0, 0, offsetDistance);
                break;
            default:
                offsetPos = segmentPos;
        }
        
        // 获取地表高度
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, offsetPos.getX(), offsetPos.getZ());
        
        return new BlockPos(offsetPos.getX(), surfaceY, offsetPos.getZ());
    }
    
    /**
     * 检查位置是否有效
     */
    private boolean isValidPlacementPosition(BlockPos pos, String decorationName, Road road) {
        // 检查是否在水面或岩浆上
        if (level.getBlockState(pos.below()).getBlock().defaultBlockState().isLiquid()) {
            return false;
        }
        
        // 检查高度差是否过大
        BlockPos roadPos = pos.atY(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()));
        if (Math.abs(pos.getY() - roadPos.getY()) > 3) {
            return false;
        }
        
        // 检查是否在放置历史中
        if (placementHistory.get(decorationName).contains(pos)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查组合规则
     */
    private boolean checkCompositionRules(String newDecoration, BlockPos newPos) {
        RoadDecoration newDeco = decorationCache.get(newDecoration);
        
        // 检查与周围装饰的兼容性
        for (Map.Entry<String, Set<BlockPos>> entry : placementHistory.entrySet()) {
            String existingDecoration = entry.getKey();
            Set<BlockPos> existingPositions = entry.getValue();
            
            RoadDecoration existingDeco = decorationCache.get(existingDecoration);
            
            for (BlockPos existingPos : existingPositions) {
                // 检查距离
                double distance = Math.sqrt(newPos.distSqr(existingPos));
                int minDistance = Math.max(newDeco.getMinDistance(), existingDeco.getMinDistance());
                
                if (distance < minDistance) {
                    return false;
                }
                
                // 检查装饰兼容性
                if (!newDeco.isCompatibleWith(existingDeco)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 清理放置历史（用于长时间运行时的内存管理）
     */
    public void clearPlacementHistory() {
        for (Set<BlockPos> positions : placementHistory.values()) {
            positions.clear();
        }
    }
    
    /**
     * 获取放置统计信息（用于调试和监控）
     */
    public Map<String, Integer> getPlacementStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map.Entry<String, Set<BlockPos>> entry : placementHistory.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
}
