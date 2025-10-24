package net.shiroha233.roadweaver.features.config;

import java.util.*;

/**
 * 装饰系统配置
 * 支持可配置的装饰密度、分布算法和组合规则
 */
public class DecorationConfig {
    
    // 装饰类型枚举
    public enum DecorationType {
        BENCH("bench", "长椅", 1.0),
        FOUNTAIN("fountain", "喷泉", 0.3),
        STREET_SIGN("street_sign", "路牌", 0.8),
        FLOWER_BED("flower_bed", "花坛", 0.7),
        LAMPPOST("lamppost", "路灯", 1.0),
        FENCE("fence", "栏杆", 0.9),
        SWING("swing", "秋千", 0.4),
        GAZEBO("gazebo", "凉亭", 0.2),
        VIEWPOINT("viewpoint", "观景台", 0.2),
        BRIDGE("bridge", "小桥", 0.3),
        STATUE("statue", "雕像", 0.1),
        FOUNTAIN_SMALL("fountain_small", "小喷泉", 0.5),
        CLOCK_TOWER("clock_tower", "钟楼", 0.1),
        BUS_STOP("bus_stop", "公交站", 0.4),
        MAILBOX("mailbox", "邮箱", 0.6),
        TRASH_BIN("trash_bin", "垃圾桶", 0.7);
        
        private final String id;
        private final String displayName;
        private final double baseProbability;
        
        DecorationType(String id, String displayName, double baseProbability) {
            this.id = id;
            this.displayName = displayName;
            this.baseProbability = baseProbability;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public double getBaseProbability() { return baseProbability; }
        
        public static DecorationType fromId(String id) {
            for (DecorationType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return BENCH; // 默认
        }
    }
    
    // 道路等级
    public enum RoadLevel {
        HIGHWAY("highway", "高速公路", 0.3, 50, 100),
        MAIN_ROAD("main_road", "主干道", 0.6, 30, 60),
        SECONDARY_ROAD("secondary_road", "次干道", 0.8, 20, 40),
        RESIDENTIAL("residential", "居民区道路", 1.0, 10, 25),
        NATURAL("natural", "自然道路", 0.5, 5, 15);
        
        private final String id;
        private final String displayName;
        private final double decorationDensity;
        private final int minSpacing;
        private final int maxSpacing;
        
        RoadLevel(String id, String displayName, double decorationDensity, int minSpacing, int maxSpacing) {
            this.id = id;
            this.displayName = displayName;
            this.decorationDensity = decorationDensity;
            this.minSpacing = minSpacing;
            this.maxSpacing = maxSpacing;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public double getDecorationDensity() { return decorationDensity; }
        public int getMinSpacing() { return minSpacing; }
        public int getMaxSpacing() { return maxSpacing; }
        
        public static RoadLevel fromId(String id) {
            for (RoadLevel level : values()) {
                if (level.id.equals(id)) {
                    return level;
                }
            }
            return MAIN_ROAD; // 默认
        }
    }
    
    // 装饰放置策略
    public enum PlacementStrategy {
        RANDOM("random", "随机分布"),
        CLUSTERED("clustered", "集群分布"),
        LINEAR("linear", "线性分布"),
        PATTERNED("patterned", "模式分布"),
        BIOME_AWARE("biome_aware", "生物群系感知");
        
        private final String id;
        private final String displayName;
        
        PlacementStrategy(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static PlacementStrategy fromId(String id) {
            for (PlacementStrategy strategy : values()) {
                if (strategy.id.equals(id)) {
                    return strategy;
                }
            }
            return RANDOM; // 默认
        }
    }
    
    // 装饰配置
    private final Map<DecorationType, Boolean> enabledDecorations;
    private final Map<RoadLevel, Double> roadLevelDensity;
    private final PlacementStrategy placementStrategy;
    private final boolean enableBiomeAdaptation;
    private final boolean enableStyleCoordination;
    private final int maxDecorationsPerChunk;
    private final double clusterProbability;
    private final int clusterSize;
    
    // 私有构造函数
    private DecorationConfig(Builder builder) {
        this.enabledDecorations = builder.enabledDecorations;
        this.roadLevelDensity = builder.roadLevelDensity;
        this.placementStrategy = builder.placementStrategy;
        this.enableBiomeAdaptation = builder.enableBiomeAdaptation;
        this.enableStyleCoordination = builder.enableStyleCoordination;
        this.maxDecorationsPerChunk = builder.maxDecorationsPerChunk;
        this.clusterProbability = builder.clusterProbability;
        this.clusterSize = builder.clusterSize;
    }
    
    // Getters
    public Map<DecorationType, Boolean> getEnabledDecorations() { return enabledDecorations; }
    public Map<RoadLevel, Double> getRoadLevelDensity() { return roadLevelDensity; }
    public PlacementStrategy getPlacementStrategy() { return placementStrategy; }
    public boolean isEnableBiomeAdaptation() { return enableBiomeAdaptation; }
    public boolean isEnableStyleCoordination() { return enableStyleCoordination; }
    public int getMaxDecorationsPerChunk() { return maxDecorationsPerChunk; }
    public double getClusterProbability() { return clusterProbability; }
    public int getClusterSize() { return clusterSize; }
    
    /**
     * 检查特定装饰类型是否启用
     */
    public boolean isDecorationEnabled(DecorationType type) {
        return enabledDecorations.getOrDefault(type, true);
    }
    
    /**
     * 获取道路等级的装饰密度
     */
    public double getDensityForRoadLevel(RoadLevel level) {
        return roadLevelDensity.getOrDefault(level, 0.5);
    }
    
    /**
     * 获取默认配置
     */
    public static DecorationConfig getDefault() {
        return new Builder()
            .placementStrategy(PlacementStrategy.BIOME_AWARE)
            .enableBiomeAdaptation(true)
            .enableStyleCoordination(true)
            .maxDecorationsPerChunk(8)
            .clusterProbability(0.3)
            .clusterSize(3)
            .build();
    }
    
    /**
     * 构建器模式
     */
    public static class Builder {
        private Map<DecorationType, Boolean> enabledDecorations = new HashMap<>();
        private Map<RoadLevel, Double> roadLevelDensity = new HashMap<>();
        private PlacementStrategy placementStrategy = PlacementStrategy.BIOME_AWARE;
        private boolean enableBiomeAdaptation = true;
        private boolean enableStyleCoordination = true;
        private int maxDecorationsPerChunk = 8;
        private double clusterProbability = 0.3;
        private int clusterSize = 3;
        
        public Builder enabledDecorations(Map<DecorationType, Boolean> decorations) {
            this.enabledDecorations = new HashMap<>(decorations);
            return this;
        }
        
        public Builder enableDecoration(DecorationType type, boolean enabled) {
            this.enabledDecorations.put(type, enabled);
            return this;
        }
        
        public Builder roadLevelDensity(Map<RoadLevel, Double> density) {
            this.roadLevelDensity = new HashMap<>(density);
            return this;
        }
        
        public Builder roadLevelDensity(RoadLevel level, double density) {
            this.roadLevelDensity.put(level, density);
            return this;
        }
        
        public Builder placementStrategy(PlacementStrategy strategy) {
            this.placementStrategy = strategy;
            return this;
        }
        
        public Builder enableBiomeAdaptation(boolean enabled) {
            this.enableBiomeAdaptation = enabled;
            return this;
        }
        
        public Builder enableStyleCoordination(boolean enabled) {
            this.enableStyleCoordination = enabled;
            return this;
        }
        
        public Builder maxDecorationsPerChunk(int max) {
            this.maxDecorationsPerChunk = max;
            return this;
        }
        
        public Builder clusterProbability(double probability) {
            this.clusterProbability = probability;
            return this;
        }
        
        public Builder clusterSize(int size) {
            this.clusterSize = size;
            return this;
        }
        
        public DecorationConfig build() {
            // 初始化默认启用状态
            for (DecorationType type : DecorationType.values()) {
                enabledDecorations.putIfAbsent(type, true);
            }
            
            // 初始化默认道路等级密度
            for (RoadLevel level : RoadLevel.values()) {
                roadLevelDensity.putIfAbsent(level, level.getDecorationDensity());
            }
            
            return new DecorationConfig(this);
        }
    }
    
    /**
     * 装饰组合规则
     */
    public static class CombinationRules {
        private final Set<DecorationType> compatibleTypes;
        private final Set<DecorationType> incompatibleTypes;
        private final double combinationProbability;
        private final int maxCombinationSize;
        
        public CombinationRules(Set<DecorationType> compatibleTypes, 
                              Set<DecorationType> incompatibleTypes,
                              double combinationProbability, int maxCombinationSize) {
            this.compatibleTypes = compatibleTypes;
            this.incompatibleTypes = incompatibleTypes;
            this.combinationProbability = combinationProbability;
            this.maxCombinationSize = maxCombinationSize;
        }
        
        public Set<DecorationType> getCompatibleTypes() { return compatibleTypes; }
        public Set<DecorationType> getIncompatibleTypes() { return incompatibleTypes; }
        public double getCombinationProbability() { return combinationProbability; }
        public int getMaxCombinationSize() { return maxCombinationSize; }
        
        /**
         * 检查两个装饰类型是否兼容
         */
        public boolean areCompatible(DecorationType type1, DecorationType type2) {
            if (incompatibleTypes.contains(type1) && incompatibleTypes.contains(type2)) {
                return false;
            }
            return compatibleTypes.contains(type1) || compatibleTypes.contains(type2);
        }
        
        /**
         * 检查装饰组合是否有效
         */
        public boolean isValidCombination(Set<DecorationType> combination) {
            if (combination.size() > maxCombinationSize) {
                return false;
            }
            
            // 检查组合中是否有不兼容的类型
            for (DecorationType type1 : combination) {
                for (DecorationType type2 : combination) {
                    if (type1 != type2 && !areCompatible(type1, type2)) {
                        return false;
                    }
                }
            }
            
            return true;
        }
    }
    
    /**
     * 获取默认组合规则
     */
    public static CombinationRules getDefaultCombinationRules() {
        Set<DecorationType> compatibleTypes = new HashSet<>(Arrays.asList(
            DecorationType.BENCH, DecorationType.FLOWER_BED, DecorationType.LAMPPOST,
            DecorationType.TRASH_BIN, DecorationType.MAILBOX, DecorationType.STREET_SIGN
        ));
        
        Set<DecorationType> incompatibleTypes = new HashSet<>(Arrays.asList(
            DecorationType.FOUNTAIN, DecorationType.STATUE, DecorationType.CLOCK_TOWER
        ));
        
        return new CombinationRules(compatibleTypes, incompatibleTypes, 0.4, 4);
    }
}
