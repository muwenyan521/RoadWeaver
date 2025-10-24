package net.shiroha233.roadweaver.features.config;

import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.shiroha233.roadweaver.helpers.Records;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 道路宽度管理器
 * 负责管理道路宽度配置，支持热重载和动态应用
 */
public class RoadWidthManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    private static RoadWidthManager instance;
    
    private RoadWidthConfig currentConfig;
    private final Map<ServerLevel, RoadWidthConfig> levelConfigs;
    private boolean configReloadRequired;
    
    private RoadWidthManager() {
        this.currentConfig = RoadWidthConfig.DEFAULT;
        this.levelConfigs = new ConcurrentHashMap<>();
        this.configReloadRequired = false;
    }
    
    public static synchronized RoadWidthManager getInstance() {
        if (instance == null) {
            instance = new RoadWidthManager();
        }
        return instance;
    }
    
    /**
     * 获取当前配置
     */
    public RoadWidthConfig getCurrentConfig() {
        return currentConfig;
    }
    
    /**
     * 更新配置（支持热重载）
     */
    public synchronized void updateConfig(RoadWidthConfig newConfig) {
        LOGGER.info("Updating road width configuration");
        this.currentConfig = newConfig;
        this.configReloadRequired = true;
        notifyConfigChange();
    }
    
    /**
     * 获取指定世界的配置
     */
    public RoadWidthConfig getConfigForLevel(ServerLevel level) {
        return levelConfigs.getOrDefault(level, currentConfig);
    }
    
    /**
     * 为指定世界设置配置
     */
    public void setConfigForLevel(ServerLevel level, RoadWidthConfig config) {
        levelConfigs.put(level, config);
        LOGGER.debug("Set road width config for level: {}", level.dimension().location());
    }
    
    /**
     * 检查是否需要重新加载配置
     */
    public boolean isConfigReloadRequired() {
        return configReloadRequired;
    }
    
    /**
     * 标记配置已重新加载
     */
    public void markConfigReloaded() {
        this.configReloadRequired = false;
        LOGGER.debug("Road width configuration reloaded");
    }
    
    /**
     * 根据道路等级和配置获取随机宽度
     */
    public int getRandomWidthForGrade(int roadGrade, RandomSource random) {
        List<Integer> availableWidths = currentConfig.getAvailableWidthsForGrade(roadGrade);
        if (availableWidths.isEmpty()) {
            LOGGER.warn("No available widths for road grade {}, using default width 3", roadGrade);
            return 3;
        }
        return availableWidths.get(random.nextInt(availableWidths.size()));
    }
    
    /**
     * 根据结构连接确定道路等级
     */
    public int determineRoadGrade(Records.StructureConnection connection) {
        // 基于连接距离确定道路等级
        double distance = Math.sqrt(
            Math.pow(connection.to().getX() - connection.from().getX(), 2) +
            Math.pow(connection.to().getZ() - connection.from().getZ(), 2)
        );
        
        IModConfig config = ConfigProvider.get();
        
        if (distance < 100) {
            return 0; // 短距离连接 - 低等级道路
        } else if (distance < 500) {
            return 1; // 中等距离连接 - 中等级道路
        } else {
            return 2; // 长距离连接 - 高等级道路
        }
    }
    
    /**
     * 获取指定道路等级的道路宽度
     */
    public int getRoadWidthForLevel(ServerLevel level, int roadType) {
        RoadWidthConfig config = getConfigForLevel(level);
        List<Integer> availableWidths = config.getAvailableWidthsForGrade(roadType);
        
        if (availableWidths.isEmpty()) {
            LOGGER.warn("No available widths for road type {}, using default width 3", roadType);
            return 3;
        }
        
        // 对于简单实现，返回第一个可用宽度
        // 在实际使用中，可能需要更复杂的逻辑来选择宽度
        return availableWidths.get(0);
    }
    
    /**
     * 获取指定宽度的宽度等级配置
     */
    public WidthLevelConfig getWidthLevelConfig(int width) {
        return currentConfig.getWidthLevel(width);
    }
    
    /**
     * 验证宽度配置
     */
    public boolean validateWidthConfig() {
        if (currentConfig == null) {
            LOGGER.error("Road width configuration is null");
            return false;
        }
        
        // 验证宽度等级
        for (Integer width : currentConfig.getSupportedWidths()) {
            if (!currentConfig.isValidWidth(width)) {
                LOGGER.error("Invalid width in configuration: {}", width);
                return false;
            }
        }
        
        // 验证道路等级映射
        for (Map.Entry<Integer, List<Integer>> entry : currentConfig.getRoadGradeToWidthMapping().entrySet()) {
            int grade = entry.getKey();
            List<Integer> widths = entry.getValue();
            
            if (widths.isEmpty()) {
                LOGGER.error("No widths defined for road grade: {}", grade);
                return false;
            }
            
            for (Integer width : widths) {
                if (!currentConfig.isValidWidth(width)) {
                    LOGGER.error("Invalid width {} for road grade {}", width, grade);
                    return false;
                }
            }
        }
        
        LOGGER.debug("Road width configuration validation passed");
        return true;
    }
    
    /**
     * 应用配置到道路生成器
     */
    public void applyConfigToRoadGenerator(RoadFeatureConfig roadConfig, int roadGrade, RandomSource random) {
        int selectedWidth = getRandomWidthForGrade(roadGrade, random);
        WidthLevelConfig widthConfig = currentConfig.getWidthLevel(selectedWidth);
        
        // 应用材质配置
        if (widthConfig.getArtificialMaterials() != null && !widthConfig.getArtificialMaterials().isEmpty()) {
            // 这里需要更新roadConfig的材质列表
            // 由于RoadFeatureConfig是不可变的，可能需要创建新的实例
        }
        
        LOGGER.debug("Applied width configuration: grade={}, width={}", roadGrade, selectedWidth);
    }
    
    /**
     * 通知配置变更
     */
    private void notifyConfigChange() {
        LOGGER.info("Road width configuration changed, notifying systems");
        // 这里可以添加通知其他系统的逻辑
        // 例如：重新计算现有道路的装饰间距等
    }
    
    /**
     * 清理指定世界的配置
     */
    public void cleanupLevelConfig(ServerLevel level) {
        levelConfigs.remove(level);
        LOGGER.debug("Cleaned up road width config for level: {}", level.dimension().location());
    }
    
    /**
     * 获取配置统计信息
     */
    public String getConfigStats() {
        return String.format(
            "RoadWidthManager Stats: Levels=%d, SupportedWidths=%s, ReloadRequired=%b",
            levelConfigs.size(),
            currentConfig.getSupportedWidths(),
            configReloadRequired
        );
    }
}
