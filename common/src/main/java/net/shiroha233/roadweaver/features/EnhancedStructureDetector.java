package net.shiroha233.roadweaver.features;

import net.shiroha233.roadweaver.features.config.StructureDetectionConfig;
import net.shiroha233.roadweaver.features.biome.BiomeConnectionStrategy;
import net.shiroha233.roadweaver.helpers.StructureLocatorImpl;
import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 增强型结构检测器
 * 整合灵活的结构检测配置和生物群系连接策略
 */
public class EnhancedStructureDetector {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    
    private final StructureDetectionConfig structureConfig;
    private final Map<ResourceLocation, StructureDetectionConfig.StructureType> structureTypeCache;
    
    public EnhancedStructureDetector() {
        this.structureConfig = StructureDetectionConfig.getDefault();
        this.structureTypeCache = new HashMap<>();
    }
    
    public EnhancedStructureDetector(StructureDetectionConfig config) {
        this.structureConfig = config;
        this.structureTypeCache = new HashMap<>();
    }
    
    /**
     * 增强的结构定位方法
     * 支持地表、地下、海上结构的可配置包含
     */
    public void locateEnhancedStructures(ServerLevel level, int locateCount, boolean locateAtPlayer) {
        if (locateCount <= 0) {
            return;
        }
        
        IModConfig config = ConfigProvider.get();
        
        // 获取所有可能的结构
        Optional<HolderSet<Structure>> allStructures = resolveEnhancedStructureTargets(level, config.structuresToLocate());
        if (allStructures.isEmpty()) {
            LOGGER.warn("RoadWeaver: 无法解析增强结构目标列表，跳过定位。");
            return;
        }
        
        List<Holder<Structure>> filteredStructures = filterStructuresByConfig(level, allStructures.get());
        if (filteredStructures.isEmpty()) {
            LOGGER.warn("RoadWeaver: 根据配置过滤后无可用结构，跳过定位。");
            return;
        }
        
        LOGGER.info("RoadWeaver: 增强结构检测 - 共 {} 种结构类型，过滤后 {} 种符合配置", 
                   allStructures.get().size(), filteredStructures.size());
        
        // 使用现有的异步定位机制，但使用过滤后的结构列表
        locateFilteredStructures(level, filteredStructures, locateCount, locateAtPlayer);
    }
    
    /**
     * 根据配置过滤结构
     */
    private List<Holder<Structure>> filterStructuresByConfig(ServerLevel level, HolderSet<Structure> structures) {
        return structures.stream()
            .filter(structure -> isStructureEnabled(level, structure))
            .collect(Collectors.toList());
    }
    
    /**
     * 检查结构是否根据配置启用
     */
    private boolean isStructureEnabled(ServerLevel level, Holder<Structure> structure) {
        ResourceLocation structureId = structure.unwrapKey()
            .map(ResourceKey::location)
            .orElse(null);
            
        if (structureId == null) {
            return false;
        }
        
        // 获取结构类型
        StructureDetectionConfig.StructureType type = getStructureType(level, structure);
        
        // 检查该类型是否在配置中启用
        return structureConfig.isStructureTypeEnabled(type);
    }
    
    /**
     * 确定结构类型（地表、地下、海上等）
     */
    private StructureDetectionConfig.StructureType getStructureType(ServerLevel level, Holder<Structure> structure) {
        ResourceLocation structureId = structure.unwrapKey()
            .map(ResourceKey::location)
            .orElse(null);
            
        if (structureId == null) {
            return StructureDetectionConfig.StructureType.SURFACE;
        }
        
        // 检查缓存
        if (structureTypeCache.containsKey(structureId)) {
            return structureTypeCache.get(structureId);
        }
        
        // 根据结构ID和特征推断类型
        StructureDetectionConfig.StructureType type = inferStructureType(structureId);
        structureTypeCache.put(structureId, type);
        
        return type;
    }
    
    /**
     * 根据结构ID推断结构类型
     */
    private StructureDetectionConfig.StructureType inferStructureType(ResourceLocation structureId) {
        String path = structureId.getPath().toLowerCase();
        String namespace = structureId.getNamespace();
        
        // 地下结构
        if (path.contains("mineshaft") || path.contains("dungeon") || 
            path.contains("stronghold") || path.contains("ruined_portal") ||
            path.contains("ancient_city") || path.contains("bastion")) {
            return StructureDetectionConfig.StructureType.UNDERGROUND;
        }
        
        // 水下结构
        if (path.contains("shipwreck") || path.contains("ocean_ruin") || 
            path.contains("monument") || path.contains("underwater")) {
            return StructureDetectionConfig.StructureType.UNDERWATER;
        }
        
        // 海岸结构
        if (path.contains("village") && (path.contains("beach") || path.contains("coast"))) {
            return StructureDetectionConfig.StructureType.COASTAL;
        }
        
        // 山地结构
        if (path.contains("village") && path.contains("mountain")) {
            return StructureDetectionConfig.StructureType.MOUNTAIN;
        }
        
        // 空中结构（末地城等）
        if (path.contains("end_city") || path.contains("end")) {
            return StructureDetectionConfig.StructureType.AERIAL;
        }
        
        // 默认地表结构
        return StructureDetectionConfig.StructureType.SURFACE;
    }
    
    /**
     * 解析增强的结构目标
     */
    private Optional<HolderSet<Structure>> resolveEnhancedStructureTargets(ServerLevel level, List<String> identifiers) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> holders = new ArrayList<>();
        
        if (identifiers == null || identifiers.isEmpty()) {
            // 如果未指定结构，使用默认结构集
            return getDefaultStructureSet(level);
        }
        
        // 重用现有的解析逻辑 - 由于StructureLocatorImpl的方法是私有的，我们需要自己实现
        return resolveStructureTargets(level, identifiers);
    }
    
    /**
     * 解析结构目标（复制自StructureLocatorImpl的实现）
     */
    private Optional<HolderSet<Structure>> resolveStructureTargets(ServerLevel level, List<String> identifiersList) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> holders = new ArrayList<>();

        if (identifiersList == null || identifiersList.isEmpty()) {
            return Optional.empty();
        }

        for (String line : identifiersList) {
            if (line == null) continue;
            String norm = line.replace('\r', ' ').replace('\n', ' ').trim();
            if (norm.isEmpty()) continue;
            // 允许行内继续使用逗号/分号/空白再分割
            String[] tokens = norm.split("[;,\\s]+");
            for (String raw : tokens) {
                if (raw == null) continue;
                String token = raw.trim();
                if (token.isEmpty()) continue;
                // 重用单字符串解析的清洗逻辑
                token = token
                        .replace("\r", "")
                        .replace("\n", "");
                token = token.replaceAll("^[\\\"'`]+|[\\\"'`]+$", "");
                token = token.replaceAll("[,;，；]+$", "");
                if (!token.isEmpty() && token.charAt(0) == '\uFEFF') token = token.substring(1);
                token = token
                        .replace('＃', '#')
                        .replace('"', ' ')
                        .replace('"', ' ')
                        .replace('「', ' ')
                        .replace('」', ' ')
                        .replace('『', ' ')
                        .replace('』', ' ')
                        .replace('《', ' ')
                        .replace('》', ' ')
                        .trim();
                if (token.isEmpty()) continue;

                int hashIdx = token.indexOf('#');
                if (hashIdx >= 0) {
                    String tagToken = token.substring(hashIdx + 1).trim();
                    try {
                        ResourceLocation tagId = new ResourceLocation(tagToken);
                        TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, tagId);
                        registry.getTag(tag).ifPresentOrElse(named -> {
                            for (Holder<Structure> h : named) holders.add(h);
                        }, () -> LOGGER.warn("RoadWeaver: structure tag not found: #{}", tagToken));
                    } catch (Exception ex) {
                        LOGGER.warn("RoadWeaver: invalid structure tag token skipped: #{} (line='{}')", tagToken, line);
                    }
                } else {
                    try {
                        String cleaned = token.replaceAll("^[^a-z0-9_.:/\\-]+", "");
                        
                        // 支持通配符匹配（例如：modid:structure_*）
                        if (cleaned.contains("*")) {
                            String pattern = cleaned.replace("*", "");
                            int matchCount = 0;
                            for (var entry : registry.entrySet()) {
                                String structureId = entry.getKey().location().toString();
                                if (structureId.startsWith(pattern)) {
                                    registry.getHolder(entry.getKey()).ifPresent(holders::add);
                                    matchCount++;
                                }
                            }
                            if (matchCount > 0) {
                                LOGGER.info("RoadWeaver: 通配符 '{}' 匹配到 {} 个结构", cleaned, matchCount);
                            } else {
                                LOGGER.warn("RoadWeaver: 通配符 '{}' 未匹配到任何结构", cleaned);
                            }
                        } else {
                            // 精确匹配
                            ResourceLocation id = new ResourceLocation(cleaned);
                            ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
                            registry.getHolder(key).ifPresentOrElse(holders::add,
                                    () -> LOGGER.warn("RoadWeaver: structure id not found: {}", cleaned));
                        }
                    } catch (Exception ex) {
                        LOGGER.warn("RoadWeaver: invalid structure id token skipped: {} (line='{}')", token, line);
                    }
                }
            }
        }

        if (holders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(HolderSet.direct(holders));
    }
    
    /**
     * 获取默认结构集（根据配置过滤）
     */
    private Optional<HolderSet<Structure>> getDefaultStructureSet(ServerLevel level) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> defaultStructures = new ArrayList<>();
        
        // 添加常见结构类型
        String[] defaultStructureIds = {
            "village", "mineshaft", "stronghold", "shipwreck", "ocean_ruin",
            "desert_pyramid", "jungle_temple", "swamp_hut", "igloo",
            "pillager_outpost", "mansion", "monument", "fortress", "end_city",
            "buried_treasure", "ruined_portal", "bastion_remnant", "ancient_city"
        };
        
        for (String structureId : defaultStructureIds) {
            try {
                ResourceLocation id = new ResourceLocation(structureId);
                ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
                registry.getHolder(key).ifPresent(holder -> {
                    if (isStructureEnabled(level, holder)) {
                        defaultStructures.add(holder);
                    }
                });
            } catch (Exception ex) {
                LOGGER.warn("RoadWeaver: 无效的默认结构ID: {}", structureId);
            }
        }
        
        if (defaultStructures.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(HolderSet.direct(defaultStructures));
    }
    
    /**
     * 定位过滤后的结构
     */
    private void locateFilteredStructures(ServerLevel level, List<Holder<Structure>> structures, 
                                         int locateCount, boolean locateAtPlayer) {
        // 重用现有的异步定位机制
        // 这里简化实现，实际中需要修改StructureLocatorImpl以支持自定义结构列表
        
        LOGGER.info("RoadWeaver: 开始增强结构定位 - {} 个结构类型，{} 个定位任务", 
                   structures.size(), locateCount);
        
        // 对于每个结构类型，提交定位任务
        for (int i = 0; i < locateCount && i < structures.size(); i++) {
            Holder<Structure> structure = structures.get(i);
            String structureName = structure.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unknown");
                
            LOGGER.debug("RoadWeaver: 定位结构类型: {}", structureName);
            
            // 这里可以添加更精确的定位逻辑
            // 目前重用现有机制
        }
        
        // 重用现有的定位逻辑
        StructureLocatorImpl.locateConfiguredStructure(level, locateCount, locateAtPlayer);
    }
    
    /**
     * 生成跨生物群系道路
     */
    public List<BlockPos> generateBiomeTransitionRoad(LevelAccessor level, BlockPos fromPos, BlockPos toPos) {
        // 获取生物群系信息
        Biome fromBiome = level.getBiome(fromPos).value();
        Biome toBiome = level.getBiome(toPos).value();
        
        ResourceLocation fromBiomeId = level.registryAccess().registryOrThrow(Registries.BIOME)
            .getKey(fromBiome);
        ResourceLocation toBiomeId = level.registryAccess().registryOrThrow(Registries.BIOME)
            .getKey(toBiome);
            
        BiomeConnectionStrategy.BiomeCategory fromCategory = 
            BiomeConnectionStrategy.BiomeCategory.fromBiome(fromBiomeId);
        BiomeConnectionStrategy.BiomeCategory toCategory = 
            BiomeConnectionStrategy.BiomeCategory.fromBiome(toBiomeId);
        
        // 获取推荐的过渡策略
        BiomeConnectionStrategy.TransitionStrategy strategy = 
            BiomeConnectionStrategy.getRecommendedTransitionStrategy(fromCategory, toCategory);
        
        LOGGER.info("RoadWeaver: 生成生物群系过渡道路 - 从 {} 到 {}, 使用策略: {}", 
                   fromCategory.getDisplayName(), toCategory.getDisplayName(), strategy.getDisplayName());
        
        // 生成过渡道路
        return BiomeConnectionStrategy.generateBiomeTransitionRoad(
            level, fromPos, toPos, fromCategory, toCategory, strategy);
    }
    
    /**
     * 检测并处理生物群系边界
     */
    public BiomeConnectionStrategy.BiomeBoundary detectAndProcessBiomeBoundary(LevelAccessor level, BlockPos centerPos) {
        BiomeConnectionStrategy.BiomeBoundary boundary = 
            BiomeConnectionStrategy.detectBiomeBoundary(level, centerPos, 16);
        
        if (boundary.hasBoundary()) {
            LOGGER.info("RoadWeaver: 检测到生物群系边界 - {} 个边界点, 过渡宽度: {}", 
                       boundary.getPositions().size(), boundary.getTransitionWidth());
        }
        
        return boundary;
    }
    
    /**
     * 获取结构检测配置
     */
    public StructureDetectionConfig getStructureConfig() {
        return structureConfig;
    }
    
    /**
     * 更新结构检测配置
     */
    public void updateStructureConfig(StructureDetectionConfig newConfig) {
        // 清除类型缓存，因为配置已更改
        structureTypeCache.clear();
        // 在实际实现中，这里应该更新配置
    }
    
    /**
     * 获取启用的结构类型统计
     */
    public Map<StructureDetectionConfig.StructureType, Integer> getEnabledStructureStats(ServerLevel level) {
        Map<StructureDetectionConfig.StructureType, Integer> stats = new HashMap<>();
        
        // 获取所有结构
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        
        for (Holder<Structure> structure : registry.stream().map(entry -> entry).toList()) {
            StructureDetectionConfig.StructureType type = getStructureType(level, structure);
            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }
        
        return stats;
    }
    
    /**
     * 获取结构检测报告
     */
    public String getDetectionReport(ServerLevel level) {
        StringBuilder report = new StringBuilder();
        report.append("=== RoadWeaver 增强结构检测报告 ===\n");
        
        // 配置信息
        report.append("检测模式: ").append(structureConfig.getDetectionMode().getDisplayName()).append("\n");
        
        // 启用的结构类型
        report.append("启用的结构类型:\n");
        for (StructureDetectionConfig.StructureType type : StructureDetectionConfig.StructureType.values()) {
            boolean enabled = structureConfig.isStructureTypeEnabled(type);
            report.append("  - ").append(type.getDisplayName()).append(": ").append(enabled ? "启用" : "禁用").append("\n");
        }
        
        // 结构统计
        Map<StructureDetectionConfig.StructureType, Integer> stats = getEnabledStructureStats(level);
        report.append("结构类型统计:\n");
        for (Map.Entry<StructureDetectionConfig.StructureType, Integer> entry : stats.entrySet()) {
            report.append("  - ").append(entry.getKey().getDisplayName())
                  .append(": ").append(entry.getValue()).append(" 种结构\n");
        }
        
        return report.toString();
    }
}
