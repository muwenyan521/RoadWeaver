package net.shiroha233.roadweaver.features.config;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 灵活的结构检测配置
 * 支持地表、地下、海上结构的可配置包含
 */
public class StructureDetectionConfig {
    
    // 结构类型枚举
    public enum StructureType {
        SURFACE("surface", "地表结构"),
        UNDERGROUND("underground", "地下结构"), 
        UNDERWATER("underwater", "水下结构"),
        AERIAL("aerial", "空中结构"),
        COASTAL("coastal", "海岸结构"),
        MOUNTAIN("mountain", "山地结构");
        
        private final String id;
        private final String displayName;
        
        StructureType(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static StructureType fromId(String id) {
            for (StructureType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return SURFACE; // 默认
        }
    }
    
    // 结构检测模式
    public enum DetectionMode {
        ALL("all", "检测所有结构"),
        SURFACE_ONLY("surface_only", "仅检测地表结构"),
        UNDERGROUND_ONLY("underground_only", "仅检测地下结构"),
        UNDERWATER_ONLY("underwater_only", "仅检测水下结构"),
        CUSTOM("custom", "自定义结构类型");
        
        private final String id;
        private final String displayName;
        
        DetectionMode(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static DetectionMode fromId(String id) {
            for (DetectionMode mode : values()) {
                if (mode.id.equals(id)) {
                    return mode;
                }
            }
            return ALL; // 默认
        }
    }
    
    // 结构检测配置
    private final DetectionMode detectionMode;
    private final Map<StructureType, Boolean> enabledTypes;
    private final List<String> customStructureIds;
    private final int maxUndergroundDepth;
    private final int maxUnderwaterDepth;
    private final boolean enableAerialStructures;
    private final boolean enableCoastalStructures;
    private final boolean enableMountainStructures;
    
    // 私有构造函数
    private StructureDetectionConfig(Builder builder) {
        this.detectionMode = builder.detectionMode;
        this.enabledTypes = builder.enabledTypes;
        this.customStructureIds = builder.customStructureIds;
        this.maxUndergroundDepth = builder.maxUndergroundDepth;
        this.maxUnderwaterDepth = builder.maxUnderwaterDepth;
        this.enableAerialStructures = builder.enableAerialStructures;
        this.enableCoastalStructures = builder.enableCoastalStructures;
        this.enableMountainStructures = builder.enableMountainStructures;
    }
    
    // Getters
    public DetectionMode getDetectionMode() { return detectionMode; }
    public Map<StructureType, Boolean> getEnabledTypes() { return enabledTypes; }
    public List<String> getCustomStructureIds() { return customStructureIds; }
    public int getMaxUndergroundDepth() { return maxUndergroundDepth; }
    public int getMaxUnderwaterDepth() { return maxUnderwaterDepth; }
    public boolean isEnableAerialStructures() { return enableAerialStructures; }
    public boolean isEnableCoastalStructures() { return enableCoastalStructures; }
    public boolean isEnableMountainStructures() { return enableMountainStructures; }
    
    /**
     * 检查特定结构类型是否启用
     */
    public boolean isStructureTypeEnabled(StructureType type) {
        if (detectionMode == DetectionMode.ALL) {
            return true;
        } else if (detectionMode == DetectionMode.SURFACE_ONLY) {
            return type == StructureType.SURFACE;
        } else if (detectionMode == DetectionMode.UNDERGROUND_ONLY) {
            return type == StructureType.UNDERGROUND;
        } else if (detectionMode == DetectionMode.UNDERWATER_ONLY) {
            return type == StructureType.UNDERWATER;
        } else if (detectionMode == DetectionMode.CUSTOM) {
            return enabledTypes.getOrDefault(type, false);
        }
        return false;
    }
    
    /**
     * 检查是否启用了任何地下结构
     */
    public boolean hasUndergroundStructures() {
        return isStructureTypeEnabled(StructureType.UNDERGROUND);
    }
    
    /**
     * 检查是否启用了任何水下结构
     */
    public boolean hasUnderwaterStructures() {
        return isStructureTypeEnabled(StructureType.UNDERWATER);
    }
    
    /**
     * 检查是否启用了任何空中结构
     */
    public boolean hasAerialStructures() {
        return isStructureTypeEnabled(StructureType.AERIAL);
    }
    
    /**
     * 获取默认配置
     */
    public static StructureDetectionConfig getDefault() {
        return new Builder()
            .detectionMode(DetectionMode.ALL)
            .maxUndergroundDepth(64)
            .maxUnderwaterDepth(32)
            .enableAerialStructures(true)
            .enableCoastalStructures(true)
            .enableMountainStructures(true)
            .build();
    }
    
    /**
     * 构建器模式
     */
    public static class Builder {
        private DetectionMode detectionMode = DetectionMode.ALL;
        private Map<StructureType, Boolean> enabledTypes = new HashMap<>();
        private List<String> customStructureIds = List.of();
        private int maxUndergroundDepth = 64;
        private int maxUnderwaterDepth = 32;
        private boolean enableAerialStructures = true;
        private boolean enableCoastalStructures = true;
        private boolean enableMountainStructures = true;
        
        public Builder detectionMode(DetectionMode mode) {
            this.detectionMode = mode;
            return this;
        }
        
        public Builder enabledTypes(Map<StructureType, Boolean> types) {
            this.enabledTypes = new HashMap<>(types);
            return this;
        }
        
        public Builder enableStructureType(StructureType type, boolean enabled) {
            this.enabledTypes.put(type, enabled);
            return this;
        }
        
        public Builder customStructureIds(List<String> ids) {
            this.customStructureIds = List.copyOf(ids);
            return this;
        }
        
        public Builder maxUndergroundDepth(int depth) {
            this.maxUndergroundDepth = depth;
            return this;
        }
        
        public Builder maxUnderwaterDepth(int depth) {
            this.maxUnderwaterDepth = depth;
            return this;
        }
        
        public Builder enableAerialStructures(boolean enabled) {
            this.enableAerialStructures = enabled;
            return this;
        }
        
        public Builder enableCoastalStructures(boolean enabled) {
            this.enableCoastalStructures = enabled;
            return this;
        }
        
        public Builder enableMountainStructures(boolean enabled) {
            this.enableMountainStructures = enabled;
            return this;
        }
        
        public StructureDetectionConfig build() {
            // 初始化默认启用状态
            for (StructureType type : StructureType.values()) {
                enabledTypes.putIfAbsent(type, true);
            }
            return new StructureDetectionConfig(this);
        }
    }
}
