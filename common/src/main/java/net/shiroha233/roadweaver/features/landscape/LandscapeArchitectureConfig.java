package net.shiroha233.roadweaver.features.landscape;

import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.HashMap;
import java.util.Map;

/**
 * 景观建筑配置类
 * 控制景观建筑的生成参数和规则
 */
public class LandscapeArchitectureConfig {
    
    private final Map<Road.RoadType, Double> generationChances;
    private final Map<LandscapeArchitectureType, Double> typeWeights;
    private final Map<String, Object> terrainRequirements;
    
    public LandscapeArchitectureConfig() {
        this.generationChances = new HashMap<>();
        this.typeWeights = new HashMap<>();
        this.terrainRequirements = new HashMap<>();
        
        initializeDefaultConfig();
    }
    
    /**
     * 初始化默认配置
     */
    private void initializeDefaultConfig() {
        // 设置道路类型的生成概率
        generationChances.put(Road.RoadType.HIGHWAY, 0.02);
        generationChances.put(Road.RoadType.MAIN_ROAD, 0.05);
        generationChances.put(Road.RoadType.SECONDARY_ROAD, 0.08);
        generationChances.put(Road.RoadType.RESIDENTIAL_ROAD, 0.12);
        generationChances.put(Road.RoadType.NATURAL_PATH, 0.15);
        
        // 设置景观建筑类型的权重
        typeWeights.put(LandscapeArchitectureType.PAVILION, 1.0);
        typeWeights.put(LandscapeArchitectureType.VIEWING_PLATFORM, 1.2);
        typeWeights.put(LandscapeArchitectureType.SMALL_BRIDGE, 0.8);
        typeWeights.put(LandscapeArchitectureType.GAZEBO, 1.1);
        typeWeights.put(LandscapeArchitectureType.FOUNTAIN_SQUARE, 0.9);
        typeWeights.put(LandscapeArchitectureType.REST_AREA, 1.3);
        typeWeights.put(LandscapeArchitectureType.SCULPTURE_GARDEN, 0.7);
        typeWeights.put(LandscapeArchitectureType.WATER_FEATURE, 0.6);
        typeWeights.put(LandscapeArchitectureType.MAZE_GARDEN, 0.5);
        typeWeights.put(LandscapeArchitectureType.CLOCK_TOWER, 0.4);
        
        // 设置地形要求
        terrainRequirements.put("min_flat_area_size", 5);
        terrainRequirements.put("max_slope_angle", 15.0);
        terrainRequirements.put("min_distance_from_water", 2);
        terrainRequirements.put("max_distance_from_water", 20);
        terrainRequirements.put("require_good_view", true);
    }
    
    /**
     * 获取指定道路类型的生成概率
     */
    public double getGenerationChance(Road.RoadType roadType) {
        return generationChances.getOrDefault(roadType, 0.05);
    }
    
    /**
     * 设置指定道路类型的生成概率
     */
    public void setGenerationChance(Road.RoadType roadType, double chance) {
        generationChances.put(roadType, Math.max(0.0, Math.min(1.0, chance)));
    }
    
    /**
     * 获取指定景观建筑类型的权重
     */
    public double getTypeWeight(LandscapeArchitectureType type) {
        return typeWeights.getOrDefault(type, 1.0);
    }
    
    /**
     * 设置指定景观建筑类型的权重
     */
    public void setTypeWeight(LandscapeArchitectureType type, double weight) {
        typeWeights.put(type, Math.max(0.1, weight));
    }
    
    /**
     * 获取地形要求参数
     */
    public Object getTerrainRequirement(String key) {
        return terrainRequirements.get(key);
    }
    
    /**
     * 设置地形要求参数
     */
    public void setTerrainRequirement(String key, Object value) {
        terrainRequirements.put(key, value);
    }
    
    /**
     * 获取所有道路类型的生成概率
     */
    public Map<Road.RoadType, Double> getAllGenerationChances() {
        return new HashMap<>(generationChances);
    }
    
    /**
     * 获取所有景观建筑类型的权重
     */
    public Map<LandscapeArchitectureType, Double> getAllTypeWeights() {
        return new HashMap<>(typeWeights);
    }
    
    /**
     * 获取所有地形要求
     */
    public Map<String, Object> getAllTerrainRequirements() {
        return new HashMap<>(terrainRequirements);
    }
    
    /**
     * 验证配置的有效性
     */
    public boolean validate() {
        // 检查生成概率是否在有效范围内
        for (double chance : generationChances.values()) {
            if (chance < 0.0 || chance > 1.0) {
                return false;
            }
        }
        
        // 检查权重是否为正数
        for (double weight : typeWeights.values()) {
            if (weight <= 0.0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 创建配置构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 配置构建器类
     */
    public static class Builder {
        private final LandscapeArchitectureConfig config;
        
        public Builder() {
            this.config = new LandscapeArchitectureConfig();
        }
        
        /**
         * 设置道路类型生成概率
         */
        public Builder setRoadTypeChance(Road.RoadType roadType, double chance) {
            config.setGenerationChance(roadType, chance);
            return this;
        }
        
        /**
         * 设置景观建筑类型权重
         */
        public Builder setArchitectureTypeWeight(LandscapeArchitectureType type, double weight) {
            config.setTypeWeight(type, weight);
            return this;
        }
        
        /**
         * 设置地形要求
         */
        public Builder setTerrainRequirement(String key, Object value) {
            config.setTerrainRequirement(key, value);
            return this;
        }
        
        /**
         * 构建配置
         */
        public LandscapeArchitectureConfig build() {
            if (!config.validate()) {
                throw new IllegalStateException("Invalid landscape architecture configuration");
            }
            return config;
        }
    }
}
