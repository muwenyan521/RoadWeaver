package net.shiroha233.roadweaver.config.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FabricModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("roadweaver.json");
    
    private static ConfigData data = new ConfigData();
    
    // 结构配置（多行：每行一个结构ID或标签）
    public static List<String> getStructuresToLocate() { return data.structuresToLocate; }
    public static void setStructuresToLocate(List<String> value) { data.structuresToLocate = value != null ? value : new ArrayList<>(); }
    
    public static int getStructureSearchRadius() { return data.structureSearchRadius; }
    public static void setStructureSearchRadius(int value) { data.structureSearchRadius = value; }
    
    // 预生成配置
    public static int getInitialLocatingCount() { return data.initialLocatingCount; }
    public static void setInitialLocatingCount(int value) { data.initialLocatingCount = value; }
    
    public static int getMaxConcurrentRoadGeneration() { return data.maxConcurrentRoadGeneration; }
    public static void setMaxConcurrentRoadGeneration(int value) { data.maxConcurrentRoadGeneration = value; }
    
    public static int getStructureSearchTriggerDistance() { return data.structureSearchTriggerDistance; }
    public static void setStructureSearchTriggerDistance(int value) { 
        data.structureSearchTriggerDistance = Math.max(150, Math.min(1500, value)); 
    }
    
    public static int getStructureBatchSize() { return data.structureBatchSize; }
    public static void setStructureBatchSize(int value) { 
        data.structureBatchSize = Math.max(1, Math.min(50, value)); 
    }
    
    public static int getStructureSearchThreads() { return data.structureSearchThreads; }
    public static void setStructureSearchThreads(int value) { 
        data.structureSearchThreads = Math.max(1, Math.min(8, value)); 
    }
    
    public static boolean getEnableAsyncStructureSearch() { return data.enableAsyncStructureSearch; }
    public static void setEnableAsyncStructureSearch(boolean value) { 
        data.enableAsyncStructureSearch = value; 
    }
    
    // 道路配置
    public static int getAveragingRadius() { return data.averagingRadius; }
    public static void setAveragingRadius(int value) { data.averagingRadius = value; }
    
    public static boolean getAllowArtificial() { return data.allowArtificial; }
    public static void setAllowArtificial(boolean value) { data.allowArtificial = value; }
    
    public static boolean getAllowNatural() { return data.allowNatural; }
    public static void setAllowNatural(boolean value) { data.allowNatural = value; }
    
    public static int getStructureDistanceFromRoad() { return data.structureDistanceFromRoad; }
    public static void setStructureDistanceFromRoad(int value) { data.structureDistanceFromRoad = value; }
    
    public static int getMaxHeightDifference() { return data.maxHeightDifference; }
    public static void setMaxHeightDifference(int value) { data.maxHeightDifference = value; }
    
    public static int getMaxTerrainStability() { return data.maxTerrainStability; }
    public static void setMaxTerrainStability(int value) { data.maxTerrainStability = value; }
    
    // 装饰配置
    public static boolean getPlaceWaypoints() { return data.placeWaypoints; }
    public static void setPlaceWaypoints(boolean value) { data.placeWaypoints = value; }
    
    public static boolean getPlaceRoadFences() { return data.placeRoadFences; }
    public static void setPlaceRoadFences(boolean value) { data.placeRoadFences = value; }
    
    public static boolean getPlaceSwings() { return data.placeSwings; }
    public static void setPlaceSwings(boolean value) { data.placeSwings = value; }
    
    public static boolean getPlaceBenches() { return data.placeBenches; }
    public static void setPlaceBenches(boolean value) { data.placeBenches = value; }
    
    public static boolean getPlaceGloriettes() { return data.placeGloriettes; }
    public static void setPlaceGloriettes(boolean value) { data.placeGloriettes = value; }
    
    // 障碍物检测与绕行配置
    public static boolean getEnableObstacleDetection() { return data.enableObstacleDetection; }
    public static void setEnableObstacleDetection(boolean value) { data.enableObstacleDetection = value; }
    
    public static boolean getEnableTreeDetection() { return data.enableTreeDetection; }
    public static void setEnableTreeDetection(boolean value) { data.enableTreeDetection = value; }
    
    public static boolean getEnableWaterDetection() { return data.enableWaterDetection; }
    public static void setEnableWaterDetection(boolean value) { data.enableWaterDetection = value; }
    
    public static boolean getEnableCropProtection() { return data.enableCropProtection; }
    public static void setEnableCropProtection(boolean value) { data.enableCropProtection = value; }
    
    public static boolean getEnableNarrowAreaAdaptation() { return data.enableNarrowAreaAdaptation; }
    public static void setEnableNarrowAreaAdaptation(boolean value) { data.enableNarrowAreaAdaptation = value; }
    
    public static boolean getEnableSpecialTerrainHandling() { return data.enableSpecialTerrainHandling; }
    public static void setEnableSpecialTerrainHandling(boolean value) { data.enableSpecialTerrainHandling = value; }
    
    public static int getObstacleDetectionRadius() { return data.obstacleDetectionRadius; }
    public static void setObstacleDetectionRadius(int value) { data.obstacleDetectionRadius = value; }
    
    public static int getMaxDetourDistance() { return data.maxDetourDistance; }
    public static void setMaxDetourDistance(int value) { data.maxDetourDistance = value; }
    
    public static boolean getEnableBridgeGeneration() { return data.enableBridgeGeneration; }
    public static void setEnableBridgeGeneration(boolean value) { data.enableBridgeGeneration = value; }
    
    public static int getMaxBridgeLength() { return data.maxBridgeLength; }
    public static void setMaxBridgeLength(int value) { data.maxBridgeLength = value; }
    
    public static boolean getEnableRavineDetection() { return data.enableRavineDetection; }
    public static void setEnableRavineDetection(boolean value) { data.enableRavineDetection = value; }
    
    public static int getRavineDetectionDepth() { return data.ravineDetectionDepth; }
    public static void setRavineDetectionDepth(int value) { data.ravineDetectionDepth = value; }
    
    public static boolean getEnableSnowBiomeAdaptation() { return data.enableSnowBiomeAdaptation; }
    public static void setEnableSnowBiomeAdaptation(boolean value) { data.enableSnowBiomeAdaptation = value; }
    
    public static boolean getEnableRedwoodForestAdaptation() { return data.enableRedwoodForestAdaptation; }
    public static void setEnableRedwoodForestAdaptation(boolean value) { data.enableRedwoodForestAdaptation = value; }
    
    public static int getNarrowAreaMinWidth() { return data.narrowAreaMinWidth; }
    public static void setNarrowAreaMinWidth(int value) { data.narrowAreaMinWidth = value; }
    
    public static int getCropProtectionRadius() { return data.cropProtectionRadius; }
    public static void setCropProtectionRadius(int value) { data.cropProtectionRadius = value; }

    // 道路分级系统配置
    public static boolean getEnableRoadGradingSystem() { return data.enableRoadGradingSystem; }
    public static void setEnableRoadGradingSystem(boolean value) { data.enableRoadGradingSystem = value; }
    
    public static boolean getEnablePrimaryRoads() { return data.enablePrimaryRoads; }
    public static void setEnablePrimaryRoads(boolean value) { data.enablePrimaryRoads = value; }
    
    public static boolean getEnableSecondaryRoads() { return data.enableSecondaryRoads; }
    public static void setEnableSecondaryRoads(boolean value) { data.enableSecondaryRoads = value; }
    
    public static boolean getEnableTertiaryRoads() { return data.enableTertiaryRoads; }
    public static void setEnableTertiaryRoads(boolean value) { data.enableTertiaryRoads = value; }
    
    public static int getPrimaryRoadWidth() { return data.primaryRoadWidth; }
    public static void setPrimaryRoadWidth(int value) { data.primaryRoadWidth = value; }
    
    public static int getSecondaryRoadWidth() { return data.secondaryRoadWidth; }
    public static void setSecondaryRoadWidth(int value) { data.secondaryRoadWidth = value; }
    
    public static int getTertiaryRoadWidth() { return data.tertiaryRoadWidth; }
    public static void setTertiaryRoadWidth(int value) { data.tertiaryRoadWidth = value; }
    
    public static boolean getEnableRoadDamageSystem() { return data.enableRoadDamageSystem; }
    public static void setEnableRoadDamageSystem(boolean value) { data.enableRoadDamageSystem = value; }
    
    public static int getMaxDamageDistance() { return data.maxDamageDistance; }
    public static void setMaxDamageDistance(int value) { data.maxDamageDistance = value; }
    
    public static double getBaseDamageRate() { return data.baseDamageRate; }
    public static void setBaseDamageRate(double value) { data.baseDamageRate = value; }
    
    public static boolean getEnableMossGrowth() { return data.enableMossGrowth; }
    public static void setEnableMossGrowth(boolean value) { data.enableMossGrowth = value; }
    
    public static boolean getEnableCobwebGrowth() { return data.enableCobwebGrowth; }
    public static void setEnableCobwebGrowth(boolean value) { data.enableCobwebGrowth = value; }
    
    public static boolean getEnableMixedRoadSegments() { return data.enableMixedRoadSegments; }
    public static void setEnableMixedRoadSegments(boolean value) { data.enableMixedRoadSegments = value; }
    
    public static int getMixedSegmentTransitionLength() { return data.mixedSegmentTransitionLength; }
    public static void setMixedSegmentTransitionLength(int value) { data.mixedSegmentTransitionLength = value; }

    // 桥梁隧道系统配置
    public static boolean getEnableBridgeTunnelSystem() { return data.enableBridgeTunnelSystem; }
    public static void setEnableBridgeTunnelSystem(boolean value) { data.enableBridgeTunnelSystem = value; }
    
    public static boolean getEnableRiverBridges() { return data.enableRiverBridges; }
    public static void setEnableRiverBridges(boolean value) { data.enableRiverBridges = value; }
    
    public static boolean getEnableViaducts() { return data.enableViaducts; }
    public static void setEnableViaducts(boolean value) { data.enableViaducts = value; }
    
    public static boolean getEnableRuinedBridges() { return data.enableRuinedBridges; }
    public static void setEnableRuinedBridges(boolean value) { data.enableRuinedBridges = value; }
    
    public static boolean getEnableCanyonBridges() { return data.enableCanyonBridges; }
    public static void setEnableCanyonBridges(boolean value) { data.enableCanyonBridges = value; }
    
    public static boolean getEnableCanyonTunnels() { return data.enableCanyonTunnels; }
    public static void setEnableCanyonTunnels(boolean value) { data.enableCanyonTunnels = value; }
    
    public static boolean getEnableMountainTunnels() { return data.enableMountainTunnels; }
    public static void setEnableMountainTunnels(boolean value) { data.enableMountainTunnels = value; }
    
    public static boolean getEnableUnderwaterTunnels() { return data.enableUnderwaterTunnels; }
    public static void setEnableUnderwaterTunnels(boolean value) { data.enableUnderwaterTunnels = value; }
    
    public static int getMaxBridgeLength() { return data.maxBridgeLength; }
    public static void setMaxBridgeLength(int value) { data.maxBridgeLength = value; }
    
    public static int getMaxTunnelLength() { return data.maxTunnelLength; }
    public static void setMaxTunnelLength(int value) { data.maxTunnelLength = value; }
    
    public static boolean getEnableNavigationRequirements() { return data.enableNavigationRequirements; }
    public static void setEnableNavigationRequirements(boolean value) { data.enableNavigationRequirements = value; }
    
    public static int getMinBridgeClearance() { return data.minBridgeClearance; }
    public static void setMinBridgeClearance(int value) { data.minBridgeClearance = value; }
    
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                data = GSON.fromJson(json, ConfigData.class);
                // 迁移：旧版单字符串 -> 新版多行列表
                if ((data.structuresToLocate == null || data.structuresToLocate.isEmpty()) && data.structureToLocate != null && !data.structureToLocate.isBlank()) {
                    data.structuresToLocate = tokenizeToList(data.structureToLocate);
                    // 清理旧字段以避免混淆
                    data.structureToLocate = null;
                    save();
                }
                // 验证并修正配置范围
                if (data.structureSearchTriggerDistance < 150 || data.structureSearchTriggerDistance > 1500) {
                    data.structureSearchTriggerDistance = 500; // 重置为默认值
                    save();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load config file: {}", CONFIG_PATH, e);
            }
        } else {
            // 初始化默认
            if (data.structuresToLocate == null || data.structuresToLocate.isEmpty()) {
                data.structuresToLocate = new ArrayList<>(List.of("#minecraft:village"));
            }
            save();
        }
    }
    
    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            LOGGER.error("Failed to save config file: {}", CONFIG_PATH, e);
        }
    }
    
    private static class ConfigData {
        // 结构配置
        // 旧字段：向后兼容读取后迁移
        String structureToLocate = "#minecraft:village";
        // 新字段：每行一个结构/标签
        List<String> structuresToLocate = new ArrayList<>(List.of("#minecraft:village"));
        int structureSearchRadius = 100;
        
        // 预生成配置
        int initialLocatingCount = 7;
        int maxConcurrentRoadGeneration = 3;
        int structureSearchTriggerDistance = 500;
        int structureBatchSize = 5; // 批量累积：累积多少个结构后再统一加入道路规划
        int structureSearchThreads = 3; // 结构搜索线程池大小
        boolean enableAsyncStructureSearch = true; // 是否启用异步多线程结构搜索
        
        // 道路配置
        int averagingRadius = 1;
        boolean allowArtificial = true;
        boolean allowNatural = false;
        int structureDistanceFromRoad = 4;
        int maxHeightDifference = 5;
        int maxTerrainStability = 4;
        
        // 装饰配置
        boolean placeWaypoints = false;
        boolean placeRoadFences = true;
        boolean placeSwings = false;
        boolean placeBenches = false;
        boolean placeGloriettes = false;
        
        // 障碍物检测与绕行配置
        boolean enableObstacleDetection = true;
        boolean enableTreeDetection = true;
        boolean enableWaterDetection = true;
        boolean enableCropProtection = true;
        boolean enableNarrowAreaAdaptation = true;
        boolean enableSpecialTerrainHandling = true;
        int obstacleDetectionRadius = 5;
        int maxDetourDistance = 15;
        boolean enableBridgeGeneration = true;
        int maxBridgeLength = 10;
        boolean enableRavineDetection = true;
        int ravineDetectionDepth = 10;
        boolean enableSnowBiomeAdaptation = true;
        boolean enableRedwoodForestAdaptation = true;
        int narrowAreaMinWidth = 3;
        int cropProtectionRadius = 3;
        
        // 道路分级系统配置
        boolean enableRoadGradingSystem = true;
        boolean enablePrimaryRoads = true;
        boolean enableSecondaryRoads = true;
        boolean enableTertiaryRoads = true;
        int primaryRoadWidth = 5;
        int secondaryRoadWidth = 3;
        int tertiaryRoadWidth = 2;
        boolean enableRoadDamageSystem = true;
        int maxDamageDistance = 100;
        double baseDamageRate = 0.1;
        boolean enableMossGrowth = true;
        boolean enableCobwebGrowth = true;
        boolean enableMixedRoadSegments = true;
        int mixedSegmentTransitionLength = 8;
        
        // 桥梁隧道系统配置
        boolean enableBridgeTunnelSystem = true;
        boolean enableRiverBridges = true;
        boolean enableViaducts = true;
        boolean enableRuinedBridges = true;
        boolean enableCanyonBridges = true;
        boolean enableCanyonTunnels = true;
        boolean enableMountainTunnels = true;
        boolean enableUnderwaterTunnels = true;
        int maxBridgeLength = 20;
        int maxTunnelLength = 30;
        boolean enableNavigationRequirements = true;
        int minBridgeClearance = 4;
    }
    
    private static List<String> tokenizeToList(String raw) {
        List<String> list = new ArrayList<>();
        if (raw == null) return list;
        String normalized = raw.replace('\r', '\n');
        List<String> lines = Arrays.asList(normalized.split("\n"));
        for (String line : lines) {
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // 行内继续支持逗号/分号/空白分隔
            String[] tokens = trimmed.split("[;,\\s]+");
            for (String t : tokens) {
                if (t == null) continue;
                String token = t.trim();
                if (!token.isEmpty()) list.add(token);
            }
        }
        return list;
    }
}
