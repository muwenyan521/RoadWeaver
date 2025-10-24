package net.shiroha233.roadweaver.config;

import java.util.List;

public interface IModConfig {
    // Structures
    List<String> structuresToLocate();
    int structureSearchRadius();

    // Pre-generation
    int initialLocatingCount();
    int maxConcurrentRoadGeneration();
    int structureSearchTriggerDistance();
    int structureBatchSize(); // 批量累积：累积多少个结构后再统一加入道路规划
    int structureSearchThreads(); // 结构搜索线程池大小
    boolean enableAsyncStructureSearch(); // 是否启用异步多线程结构搜索

    // Roads
    int averagingRadius();
    boolean allowArtificial();
    boolean allowNatural();
    boolean placeWaypoints();
    boolean placeRoadFences();
    boolean placeSwings();
    boolean placeBenches();
    boolean placeGloriettes();
    int structureDistanceFromRoad();
    int maxHeightDifference();
    int maxTerrainStability();

    // 性能配置
    default int heightCacheMaxSize() {
        return 100_000; // 默认10万个条目
    }

    // 道路宽度配置
    default boolean enableWidthConfiguration() {
        return true;
    }
    
    default int defaultRoadWidth() {
        return 3;
    }
    
    default boolean enableWidthBasedDecoration() {
        return true;
    }

    // 增强型结构检测配置
    default boolean enableEnhancedStructureDetection() {
        return true;
    }
    
    default String structureDetectionMode() {
        return "ALL"; // ALL, SURFACE_ONLY, UNDERGROUND_ONLY, UNDERWATER_ONLY, CUSTOM
    }
    
    default boolean detectSurfaceStructures() {
        return true;
    }
    
    default boolean detectUndergroundStructures() {
        return false;
    }
    
    default boolean detectUnderwaterStructures() {
        return false;
    }
    
    default boolean detectAerialStructures() {
        return false;
    }
    
    default boolean detectCoastalStructures() {
        return true;
    }
    
    default boolean detectMountainStructures() {
        return true;
    }

    // 地形适配配置
    default boolean enableTerrainAdaptation() {
        return true;
    }
    
    default boolean enableStepReplacement() {
        return true;
    }
    
    default boolean enableSlopeAdaptation() {
        return true;
    }
    
    default int maxStepHeight() {
        return 3;
    }
    
    default int maxSlopeAngle() {
        return 45;
    }

    // 生物群系连接策略配置
    default boolean enableBiomeConnectionStrategy() {
        return true;
    }
    
    default String biomeTransitionStrategy() {
        return "GRADUAL"; // GRADUAL, CLEAR_BOUNDARY, MIXED_STYLE, NATURAL_FUSION
    }
    
    default boolean enableBiomeBoundaryDetection() {
        return true;
    }
    
    default int biomeTransitionWidth() {
        return 8;
    }
}
