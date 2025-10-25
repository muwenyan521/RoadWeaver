package net.shiroha233.roadweaver.features;

import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 三级道路分级系统
 * 实现石板主路、木石混合次级路、泥径破损支路的分级体系
 * 支持混合路段渐变过渡和道路破损度算法
 */
public class RoadGradingSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    
    // 道路等级枚举
    public enum RoadGrade {
        PRIMARY,      // 石板主路
        SECONDARY,    // 木石混合次级路
        TERTIARY      // 泥径破损支路
    }
    
    // 道路材料配置
    public static class RoadMaterials {
        public final List<BlockState> primaryMaterials;    // 石板主路材料
        public final List<BlockState> secondaryMaterials;  // 木石混合材料
        public final List<BlockState> tertiaryMaterials;   // 泥径材料
        public final List<BlockState> mossMaterials;       // 苔藓材料
        public final List<BlockState> cobwebMaterials;     // 蜘蛛网材料
        
        public RoadMaterials(List<BlockState> primaryMaterials, 
                            List<BlockState> secondaryMaterials,
                            List<BlockState> tertiaryMaterials,
                            List<BlockState> mossMaterials,
                            List<BlockState> cobwebMaterials) {
            this.primaryMaterials = primaryMaterials;
            this.secondaryMaterials = secondaryMaterials;
            this.tertiaryMaterials = tertiaryMaterials;
            this.mossMaterials = mossMaterials;
            this.cobwebMaterials = cobwebMaterials;
        }
    }
    
    // 道路破损配置
    public static class RoadDamageConfig {
        public final double baseDamageRate;           // 基础破损率
        public final double distanceDamageMultiplier; // 距离破损乘数
        public final double mossGrowthRate;           // 苔藓生长率
        public final double cobwebGrowthRate;         // 蜘蛛网生长率
        public final int maxDamageDistance;           // 最大破损距离
        
        public RoadDamageConfig(double baseDamageRate, double distanceDamageMultiplier,
                               double mossGrowthRate, double cobwebGrowthRate, int maxDamageDistance) {
            this.baseDamageRate = baseDamageRate;
            this.distanceDamageMultiplier = distanceDamageMultiplier;
            this.mossGrowthRate = mossGrowthRate;
            this.cobwebGrowthRate = cobwebGrowthRate;
            this.maxDamageDistance = maxDamageDistance;
        }
    }
    
    // 混合路段配置
    public static class MixedSegmentConfig {
        public final int transitionLength;           // 过渡段长度
        public final double transitionSmoothness;    // 过渡平滑度
        public final boolean enableGradualTransition; // 是否启用渐变过渡
        
        public MixedSegmentConfig(int transitionLength, double transitionSmoothness, boolean enableGradualTransition) {
            this.transitionLength = transitionLength;
            this.transitionSmoothness = transitionSmoothness;
            this.enableGradualTransition = enableGradualTransition;
        }
    }
    
    // 默认道路材料配置
    private static final RoadMaterials DEFAULT_MATERIALS = new RoadMaterials(
        // 石板主路材料
        List.of(
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.SMOOTH_STONE.defaultBlockState(),
            Blocks.POLISHED_ANDESITE.defaultBlockState(),
            Blocks.POLISHED_DIORITE.defaultBlockState()
        ),
        // 木石混合次级路材料
        List.of(
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.GRAVEL.defaultBlockState()
        ),
        // 泥径破损支路材料
        List.of(
            Blocks.DIRT.defaultBlockState(),
            Blocks.COARSE_DIRT.defaultBlockState(),
            Blocks.PODZOL.defaultBlockState(),
            Blocks.GRASS_BLOCK.defaultBlockState()
        ),
        // 苔藓材料
        List.of(
            Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
            Blocks.MOSS_BLOCK.defaultBlockState(),
            Blocks.VINE.defaultBlockState()
        ),
        // 蜘蛛网材料
        List.of(
            Blocks.COBWEB.defaultBlockState(),
            Blocks.DEAD_BUSH.defaultBlockState()
        )
    );
    
    // 默认破损配置
    private static final RoadDamageConfig DEFAULT_DAMAGE_CONFIG = new RoadDamageConfig(
        0.05,   // 基础破损率 5%
        0.001,  // 距离破损乘数 0.1%/格
        0.02,   // 苔藓生长率 2%
        0.01,   // 蜘蛛网生长率 1%
        1000    // 最大破损距离 1000格
    );
    
    // 默认混合路段配置
    private static final MixedSegmentConfig DEFAULT_MIXED_CONFIG = new MixedSegmentConfig(
        20,     // 过渡段长度 20格
        0.8,    // 过渡平滑度
        true    // 启用渐变过渡
    );
    
    private final RoadMaterials materials;
    private final RoadDamageConfig damageConfig;
    private final MixedSegmentConfig mixedConfig;
    
    public RoadGradingSystem() {
        this(DEFAULT_MATERIALS, DEFAULT_DAMAGE_CONFIG, DEFAULT_MIXED_CONFIG);
    }
    
    public RoadGradingSystem(RoadMaterials materials, RoadDamageConfig damageConfig, MixedSegmentConfig mixedConfig) {
        this.materials = materials;
        this.damageConfig = damageConfig;
        this.mixedConfig = mixedConfig;
        LOGGER.debug("RoadGradingSystem initialized with {} materials", materials.primaryMaterials.size());
    }
    
    /**
     * 获取指定道路等级的材料
     */
    public List<BlockState> getMaterialsForGrade(RoadGrade grade) {
        switch (grade) {
            case PRIMARY:
                return materials.primaryMaterials;
            case SECONDARY:
                return materials.secondaryMaterials;
            case TERTIARY:
                return materials.tertiaryMaterials;
            default:
                return materials.primaryMaterials;
        }
    }
    
    /**
     * 计算道路破损度
     * @param distanceFromTarget 距离目标的距离
     * @param random 随机源
     * @return 破损度 (0.0 - 1.0)
     */
    public double calculateDamageRate(int distanceFromTarget, RandomSource random) {
        // 基础破损率 + 距离相关破损率
        double distanceFactor = Math.min(distanceFromTarget, damageConfig.maxDamageDistance) * damageConfig.distanceDamageMultiplier;
        double baseRate = damageConfig.baseDamageRate + distanceFactor;
        
        // 添加随机波动
        double randomVariation = (random.nextDouble() - 0.5) * 0.1;
        return Math.max(0.0, Math.min(1.0, baseRate + randomVariation));
    }
    
    /**
     * 应用道路破损效果
     */
    public void applyRoadDamage(LevelAccessor level, BlockPos pos, double damageRate, RandomSource random) {
        if (damageRate <= 0.0) {
            return;
        }
        
        BlockState currentState = level.getBlockState(pos);
        
        // 根据破损率决定是否替换方块
        if (random.nextDouble() < damageRate) {
            // 破损效果：替换为更破损的材料
            BlockState damagedState = getDamagedVariant(currentState, random);
            if (damagedState != null) {
                level.setBlock(pos, damagedState, 3);
            }
        }
        
        // 添加苔藓效果
        if (random.nextDouble() < damageConfig.mossGrowthRate * damageRate) {
            applyMossEffect(level, pos, random);
        }
        
        // 添加蜘蛛网效果
        if (random.nextDouble() < damageConfig.cobwebGrowthRate * damageRate) {
            applyCobwebEffect(level, pos, random);
        }
    }
    
    /**
     * 获取破损变体材料
     */
    private BlockState getDamagedVariant(BlockState original, RandomSource random) {
        Block block = original.getBlock();
        
        // 石板材料破损变体
        if (block == Blocks.STONE_BRICKS) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        }
        if (block == Blocks.SMOOTH_STONE) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        if (block == Blocks.POLISHED_ANDESITE || block == Blocks.POLISHED_DIORITE) {
            return Blocks.ANDESITE.defaultBlockState();
        }
        
        // 木石混合材料破损变体
        if (block == Blocks.OAK_PLANKS || block == Blocks.SPRUCE_PLANKS) {
            return materials.mossMaterials.get(random.nextInt(materials.mossMaterials.size()));
        }
        if (block == Blocks.COBBLESTONE) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        }
        
        // 泥径材料破损变体
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (block == Blocks.GRASS_BLOCK) {
            return Blocks.DIRT.defaultBlockState();
        }
        
        return null; // 没有合适的破损变体
    }
    
    /**
     * 应用苔藓效果
     */
    private void applyMossEffect(LevelAccessor level, BlockPos pos, RandomSource random) {
        BlockState mossMaterial = materials.mossMaterials.get(random.nextInt(materials.mossMaterials.size()));
        
        // 在道路上方放置苔藓或藤蔓
        BlockPos abovePos = pos.above();
        if (level.getBlockState(abovePos).isAir()) {
            if (mossMaterial.getBlock() == Blocks.VINE) {
                // 藤蔓需要附着在固体方块上
                level.setBlock(abovePos, mossMaterial, 3);
            } else {
                // 其他苔藓材料直接放置
                level.setBlock(pos, mossMaterial, 3);
            }
        } else {
            // 如果上方不是空气，直接替换当前方块
            level.setBlock(pos, mossMaterial, 3);
        }
    }
    
    /**
     * 应用蜘蛛网效果
     */
    private void applyCobwebEffect(LevelAccessor level, BlockPos pos, RandomSource random) {
        BlockState cobwebMaterial = materials.cobwebMaterials.get(random.nextInt(materials.cobwebMaterials.size()));
        
        // 在道路上方放置蜘蛛网或枯灌木
        BlockPos abovePos = pos.above();
        if (level.getBlockState(abovePos).isAir()) {
            level.setBlock(abovePos, cobwebMaterial, 3);
        }
    }
    
    /**
     * 创建混合路段过渡
     * @param startGrade 起始道路等级
     * @param endGrade 目标道路等级
     * @param segmentIndex 路段索引
     * @param totalSegments 总路段数
     * @return 混合材料列表
     */
    public List<BlockState> createMixedSegment(RoadGrade startGrade, RoadGrade endGrade, 
                                              int segmentIndex, int totalSegments, RandomSource random) {
        if (!mixedConfig.enableGradualTransition || startGrade == endGrade) {
            return getMaterialsForGrade(startGrade);
        }
        
        // 计算过渡进度 (0.0 - 1.0)
        double transitionProgress = calculateTransitionProgress(segmentIndex, totalSegments);
        
        // 根据过渡进度混合材料
        return mixMaterials(startGrade, endGrade, transitionProgress, random);
    }
    
    /**
     * 计算过渡进度
     */
    private double calculateTransitionProgress(int segmentIndex, int totalSegments) {
        if (mixedConfig.transitionLength <= 0) {
            return 1.0;
        }
        
        // 计算在过渡段中的位置
        int transitionStart = (totalSegments - mixedConfig.transitionLength) / 2;
        int transitionEnd = transitionStart + mixedConfig.transitionLength;
        
        if (segmentIndex < transitionStart) {
            return 0.0; // 过渡前
        } else if (segmentIndex > transitionEnd) {
            return 1.0; // 过渡后
        } else {
            // 在过渡段中，使用平滑函数计算进度
            double normalizedProgress = (double)(segmentIndex - transitionStart) / mixedConfig.transitionLength;
            return applySmoothTransition(normalizedProgress);
        }
    }
    
    /**
     * 应用平滑过渡函数
     */
    private double applySmoothTransition(double progress) {
        // 使用平滑的S形曲线过渡
        return 0.5 - 0.5 * Math.cos(Math.PI * Math.pow(progress, mixedConfig.transitionSmoothness));
    }
    
    /**
     * 混合两种道路等级的材料
     */
    private List<BlockState> mixMaterials(RoadGrade startGrade, RoadGrade endGrade, 
                                         double progress, RandomSource random) {
        List<BlockState> startMaterials = getMaterialsForGrade(startGrade);
        List<BlockState> endMaterials = getMaterialsForGrade(endGrade);
        List<BlockState> mixedMaterials = new ArrayList<>();
        
        // 根据进度混合材料
        for (int i = 0; i < Math.max(startMaterials.size(), endMaterials.size()); i++) {
            BlockState startMaterial = i < startMaterials.size() ? startMaterials.get(i) : startMaterials.get(0);
            BlockState endMaterial = i < endMaterials.size() ? endMaterials.get(i) : endMaterials.get(0);
            
            // 根据进度选择材料
            if (random.nextDouble() < progress) {
                mixedMaterials.add(endMaterial);
            } else {
                mixedMaterials.add(startMaterial);
            }
        }
        
        return mixedMaterials;
    }
    
    /**
     * 根据距离和地形条件确定道路等级
     */
    public RoadGrade determineRoadGrade(int distanceFromTarget, LevelAccessor level, BlockPos pos, RandomSource random) {
        IModConfig config = ConfigProvider.get();
        
        // 基础逻辑：距离越远，道路等级越低
        if (distanceFromTarget < 200) {
            return RoadGrade.PRIMARY; // 近距离使用主路
        } else if (distanceFromTarget < 600) {
            return RoadGrade.SECONDARY; // 中等距离使用次级路
        } else {
            return RoadGrade.TERTIARY; // 远距离使用支路
        }
    }
    
    /**
     * 获取道路宽度配置（基于道路等级）
     */
    public int getRoadWidthForGrade(RoadGrade grade) {
        switch (grade) {
            case PRIMARY:
                return 3; // 主路宽度3格
            case SECONDARY:
                return 2; // 次级路宽度2格
            case TERTIARY:
                return 1; // 支路宽度1格
            default:
                return 2;
        }
    }
    
    /**
     * 获取装饰密度配置（基于道路等级）
     */
    public double getDecorationDensityForGrade(RoadGrade grade) {
        switch (grade) {
            case PRIMARY:
                return 1.0; // 主路装饰密度高
            case SECONDARY:
                return 0.6; // 次级路装饰密度中等
            case TERTIARY:
                return 0.3; // 支路装饰密度低
            default:
                return 0.5;
        }
    }
    
    /**
     * 验证道路材料配置
     */
    public boolean validateMaterials() {
        return !materials.primaryMaterials.isEmpty() &&
               !materials.secondaryMaterials.isEmpty() &&
               !materials.tertiaryMaterials.isEmpty();
    }
    
    /**
     * 获取系统统计信息
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("primary_materials_count", materials.primaryMaterials.size());
        stats.put("secondary_materials_count", materials.secondaryMaterials.size());
        stats.put("tertiary_materials_count", materials.tertiaryMaterials.size());
        stats.put("base_damage_rate", damageConfig.baseDamageRate);
        stats.put("max_damage_distance", damageConfig.maxDamageDistance);
        stats.put("transition_length", mixedConfig.transitionLength);
        return stats;
    }
}
