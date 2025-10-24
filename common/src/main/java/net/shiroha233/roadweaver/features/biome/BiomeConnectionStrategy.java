package net.shiroha233.roadweaver.features.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.shiroha233.roadweaver.features.terrain.EnhancedTerrainAdapter;

import java.util.*;
import java.util.function.Function;

/**
 * 生物群系连接策略系统
 * 在生物群系边界创建特色过渡道路，确保道路风格与环境自然融合
 */
public class BiomeConnectionStrategy {
    
    // 生物群系类型分类
    public enum BiomeCategory {
        PLAINS("plains", "平原"),
        FOREST("forest", "森林"),
        DESERT("desert", "沙漠"),
        MOUNTAIN("mountain", "山地"),
        OCEAN("ocean", "海洋"),
        SWAMP("swamp", "沼泽"),
        TAIGA("taiga", "针叶林"),
        JUNGLE("jungle", "丛林"),
        MESA("mesa", "恶地"),
        SNOW("snow", "雪地"),
        NETHER("nether", "下界"),
        END("end", "末地");
        
        private final String id;
        private final String displayName;
        
        BiomeCategory(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static BiomeCategory fromBiome(ResourceLocation biomeId) {
            String path = biomeId.getPath().toLowerCase();
            
            if (path.contains("plains") || path.contains("meadow")) {
                return PLAINS;
            } else if (path.contains("forest") || path.contains("wood")) {
                return FOREST;
            } else if (path.contains("desert")) {
                return DESERT;
            } else if (path.contains("mountain") || path.contains("peak") || path.contains("slope")) {
                return MOUNTAIN;
            } else if (path.contains("ocean") || path.contains("deep_ocean") || path.contains("river")) {
                return OCEAN;
            } else if (path.contains("swamp") || path.contains("mangrove")) {
                return SWAMP;
            } else if (path.contains("taiga")) {
                return TAIGA;
            } else if (path.contains("jungle")) {
                return JUNGLE;
            } else if (path.contains("badlands") || path.contains("mesa")) {
                return MESA;
            } else if (path.contains("snow") || path.contains("ice") || path.contains("frozen")) {
                return SNOW;
            } else if (path.contains("nether") || path.contains("soul") || path.contains("crimson") || path.contains("warped")) {
                return NETHER;
            } else if (path.contains("end")) {
                return END;
            }
            
            return PLAINS; // 默认
        }
    }
    
    // 过渡策略
    public enum TransitionStrategy {
        GRADUAL("gradual", "渐进过渡"),
        SHARP("sharp", "清晰边界"),
        BLENDED("blended", "混合风格"),
        NATURAL("natural", "自然融合");
        
        private final String id;
        private final String displayName;
        
        TransitionStrategy(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
    }
    
    // 道路材料配置
    public static class RoadMaterials {
        private final BlockState mainBlock;
        private final BlockState borderBlock;
        private final BlockState decorationBlock;
        private final BlockState transitionBlock;
        
        public RoadMaterials(BlockState mainBlock, BlockState borderBlock, 
                           BlockState decorationBlock, BlockState transitionBlock) {
            this.mainBlock = mainBlock;
            this.borderBlock = borderBlock;
            this.decorationBlock = decorationBlock;
            this.transitionBlock = transitionBlock;
        }
        
        public BlockState getMainBlock() { return mainBlock; }
        public BlockState getBorderBlock() { return borderBlock; }
        public BlockState getDecorationBlock() { return decorationBlock; }
        public BlockState getTransitionBlock() { return transitionBlock; }
    }
    
    // 生物群系材料映射
    private static final Map<BiomeCategory, RoadMaterials> BIOME_MATERIALS = new HashMap<>();
    
    static {
        // 平原
        BIOME_MATERIALS.put(BiomeCategory.PLAINS, new RoadMaterials(
            Blocks.GRASS_BLOCK.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState(),
            Blocks.POPPY.defaultBlockState(),
            Blocks.COARSE_DIRT.defaultBlockState()
        ));
        
        // 森林
        BIOME_MATERIALS.put(BiomeCategory.FOREST, new RoadMaterials(
            Blocks.PODZOL.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.FERN.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState()
        ));
        
        // 沙漠
        BIOME_MATERIALS.put(BiomeCategory.DESERT, new RoadMaterials(
            Blocks.SAND.defaultBlockState(),
            Blocks.SANDSTONE.defaultBlockState(),
            Blocks.DEAD_BUSH.defaultBlockState(),
            Blocks.SMOOTH_SANDSTONE.defaultBlockState()
        ));
        
        // 山地
        BIOME_MATERIALS.put(BiomeCategory.MOUNTAIN, new RoadMaterials(
            Blocks.STONE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.STONE_BUTTON.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState()
        ));
        
        // 海洋（海岸道路）
        BIOME_MATERIALS.put(BiomeCategory.OCEAN, new RoadMaterials(
            Blocks.PRISMARINE.defaultBlockState(),
            Blocks.DARK_PRISMARINE.defaultBlockState(),
            Blocks.SEA_LANTERN.defaultBlockState(),
            Blocks.PRISMARINE_BRICKS.defaultBlockState()
        ));
        
        // 沼泽
        BIOME_MATERIALS.put(BiomeCategory.SWAMP, new RoadMaterials(
            Blocks.MUD.defaultBlockState(),
            Blocks.MANGROVE_PLANKS.defaultBlockState(),
            Blocks.LILY_PAD.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState()
        ));
        
        // 针叶林
        BIOME_MATERIALS.put(BiomeCategory.TAIGA, new RoadMaterials(
            Blocks.PODZOL.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.SWEET_BERRY_BUSH.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState()
        ));
        
        // 丛林
        BIOME_MATERIALS.put(BiomeCategory.JUNGLE, new RoadMaterials(
            Blocks.MOSS_BLOCK.defaultBlockState(),
            Blocks.JUNGLE_PLANKS.defaultBlockState(),
            Blocks.VINE.defaultBlockState(),
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
        ));
        
        // 恶地
        BIOME_MATERIALS.put(BiomeCategory.MESA, new RoadMaterials(
            Blocks.TERRACOTTA.defaultBlockState(),
            Blocks.RED_SANDSTONE.defaultBlockState(),
            Blocks.DEAD_BUSH.defaultBlockState(),
            Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState()
        ));
        
        // 雪地
        BIOME_MATERIALS.put(BiomeCategory.SNOW, new RoadMaterials(
            Blocks.SNOW_BLOCK.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState(),
            Blocks.SNOW.defaultBlockState(),
            Blocks.PACKED_ICE.defaultBlockState()
        ));
        
        // 下界
        BIOME_MATERIALS.put(BiomeCategory.NETHER, new RoadMaterials(
            Blocks.NETHERRACK.defaultBlockState(),
            Blocks.NETHER_BRICKS.defaultBlockState(),
            Blocks.NETHER_WART.defaultBlockState(),
            Blocks.CRACKED_NETHER_BRICKS.defaultBlockState()
        ));
        
        // 末地
        BIOME_MATERIALS.put(BiomeCategory.END, new RoadMaterials(
            Blocks.END_STONE.defaultBlockState(),
            Blocks.PURPUR_BLOCK.defaultBlockState(),
            Blocks.CHORUS_FLOWER.defaultBlockState(),
            Blocks.END_STONE_BRICKS.defaultBlockState()
        ));
    }
    
    /**
     * 获取生物群系的道路材料
     */
    public static RoadMaterials getBiomeMaterials(BiomeCategory category) {
        return BIOME_MATERIALS.getOrDefault(category, BIOME_MATERIALS.get(BiomeCategory.PLAINS));
    }
    
    /**
     * 检测生物群系边界
     * 返回边界位置和过渡宽度
     */
    public static BiomeBoundary detectBiomeBoundary(LevelAccessor level, BlockPos startPos, int searchRadius) {
        Biome startBiome = level.getBiome(startPos).value();
        ResourceLocation startBiomeId = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
            .getKey(startBiome);
        BiomeCategory startCategory = BiomeCategory.fromBiome(startBiomeId);
        
        List<BlockPos> boundaryPositions = new ArrayList<>();
        int transitionWidth = 0;
        
        // 在搜索半径内检测生物群系变化
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                BlockPos checkPos = startPos.offset(x, 0, z);
                Biome checkBiome = level.getBiome(checkPos).value();
                ResourceLocation checkBiomeId = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getKey(checkBiome);
                BiomeCategory checkCategory = BiomeCategory.fromBiome(checkBiomeId);
                
                if (!startCategory.equals(checkCategory)) {
                    boundaryPositions.add(checkPos);
                    // 计算过渡宽度（基于生物群系差异程度）
                    transitionWidth = Math.max(transitionWidth, calculateTransitionWidth(startCategory, checkCategory));
                }
            }
        }
        
        return new BiomeBoundary(boundaryPositions, startCategory, transitionWidth);
    }
    
    /**
     * 计算生物群系之间的过渡宽度
     */
    private static int calculateTransitionWidth(BiomeCategory from, BiomeCategory to) {
        // 基于生物群系差异程度计算过渡宽度
        Map<BiomeCategory, Integer> complexityScores = Map.of(
            BiomeCategory.PLAINS, 1,
            BiomeCategory.FOREST, 2,
            BiomeCategory.DESERT, 2,
            BiomeCategory.MOUNTAIN, 3,
            BiomeCategory.OCEAN, 3,
            BiomeCategory.SWAMP, 3,
            BiomeCategory.TAIGA, 2,
            BiomeCategory.JUNGLE, 3,
            BiomeCategory.MESA, 2,
            BiomeCategory.SNOW, 2,
            BiomeCategory.NETHER, 4,
            BiomeCategory.END, 4
        );
        
        int fromScore = complexityScores.getOrDefault(from, 1);
        int toScore = complexityScores.getOrDefault(to, 1);
        int difference = Math.abs(fromScore - toScore);
        
        // 基础宽度 + 差异调整
        return 3 + difference * 2;
    }
    
    /**
     * 生成跨生物群系过渡道路
     */
    public static List<BlockPos> generateBiomeTransitionRoad(LevelAccessor level, 
                                                           BlockPos fromPos, BlockPos toPos,
                                                           BiomeCategory fromBiome, BiomeCategory toBiome,
                                                           TransitionStrategy strategy) {
        List<BlockPos> roadPositions = new ArrayList<>();
        EnhancedTerrainAdapter terrainAdapter = new EnhancedTerrainAdapter();
        
        // 检测生物群系边界
        BiomeBoundary boundary = detectBiomeBoundary(level, fromPos, 16);
        
        // 根据策略选择过渡方式
        switch (strategy) {
            case GRADUAL:
                roadPositions.addAll(createGradualTransition(level, fromPos, toPos, fromBiome, toBiome, boundary));
                break;
            case SHARP:
                roadPositions.addAll(createSharpTransition(level, fromPos, toPos, fromBiome, toBiome, boundary));
                break;
            case BLENDED:
                roadPositions.addAll(createBlendedTransition(level, fromPos, toPos, fromBiome, toBiome, boundary));
                break;
            case NATURAL:
                roadPositions.addAll(createNaturalTransition(level, fromPos, toPos, fromBiome, toBiome, boundary, terrainAdapter));
                break;
        }
        
        return roadPositions;
    }
    
    /**
     * 渐进过渡：材料逐渐变化
     */
    private static List<BlockPos> createGradualTransition(LevelAccessor level, BlockPos fromPos, BlockPos toPos,
                                                         BiomeCategory fromBiome, BiomeCategory toBiome,
                                                         BiomeBoundary boundary) {
        List<BlockPos> road = new ArrayList<>();
        RoadMaterials fromMaterials = getBiomeMaterials(fromBiome);
        RoadMaterials toMaterials = getBiomeMaterials(toBiome);
        
        // 在边界区域创建渐变道路
        for (BlockPos boundaryPos : boundary.getPositions()) {
            double distance = Math.sqrt(boundaryPos.distSqr(fromPos));
            double maxDistance = Math.sqrt(fromPos.distSqr(toPos));
            double ratio = Math.min(distance / maxDistance, 1.0);
            
            // 根据距离比例混合材料
            BlockState blendedBlock = blendBlockStates(fromMaterials.getMainBlock(), toMaterials.getMainBlock(), ratio);
            
            // 应用地形适配
            EnhancedTerrainAdapter.TerrainAnalysis analysis = 
                new EnhancedTerrainAdapter().analyzeTerrain(level, boundaryPos, 3);
            List<BlockPos> adaptedPositions = new EnhancedTerrainAdapter()
                .applyTerrainAdaptation(level, boundaryPos, analysis);
            
            for (BlockPos pos : adaptedPositions) {
                level.setBlock(pos, blendedBlock, 3);
                road.add(pos);
            }
        }
        
        return road;
    }
    
    /**
     * 清晰边界：在边界处明确切换材料
     */
    private static List<BlockPos> createSharpTransition(LevelAccessor level, BlockPos fromPos, BlockPos toPos,
                                                       BiomeCategory fromBiome, BiomeCategory toBiome,
                                                       BiomeBoundary boundary) {
        List<BlockPos> road = new ArrayList<>();
        RoadMaterials fromMaterials = getBiomeMaterials(fromBiome);
        RoadMaterials toMaterials = getBiomeMaterials(toBiome);
        
        // 在边界处创建明确的材料切换
        for (BlockPos boundaryPos : boundary.getPositions()) {
            // 使用过渡材料作为边界标记
            level.setBlock(boundaryPos, fromMaterials.getTransitionBlock(), 3);
            road.add(boundaryPos);
            
            // 在边界两侧分别使用各自的材料
            BlockPos fromSide = boundaryPos.offset(fromPos.subtract(boundaryPos).normalize());
            BlockPos toSide = boundaryPos.offset(toPos.subtract(boundaryPos).normalize());
            
            level.setBlock(fromSide, fromMaterials.getMainBlock(), 3);
            level.setBlock(toSide, toMaterials.getMainBlock(), 3);
            road.add(fromSide);
            road.add(toSide);
        }
        
        return road;
    }
    
    /**
     * 混合风格：在过渡区域混合使用两种材料
     */
    private static List<BlockPos> createBlendedTransition(LevelAccessor level, BlockPos fromPos, BlockPos toPos,
                                                         BiomeCategory fromBiome, BiomeCategory toBiome,
                                                         BiomeBoundary boundary) {
        List<BlockPos> road = new ArrayList<>();
        RoadMaterials fromMaterials = getBiomeMaterials(fromBiome);
        RoadMaterials toMaterials = getBiomeMaterials(toBiome);
        
        // 创建棋盘格模式的混合道路
        for (BlockPos boundaryPos : boundary.getPositions()) {
            boolean useFromMaterial = (boundaryPos.getX() + boundaryPos.getZ()) % 2 == 0;
            BlockState block = useFromMaterial ? fromMaterials.getMainBlock() : toMaterials.getMainBlock();
            
            level.setBlock(boundaryPos, block, 3);
            road.add(boundaryPos);
        }
        
        return road;
    }
    
    /**
     * 自然融合：基于地形特征的自然过渡
     */
    private static List<BlockPos> createNaturalTransition(LevelAccessor level, BlockPos fromPos, BlockPos toPos,
                                                         BiomeCategory fromBiome, BiomeCategory toBiome,
                                                         BiomeBoundary boundary, EnhancedTerrainAdapter terrainAdapter) {
        List<BlockPos> road = new ArrayList<>();
        RoadMaterials fromMaterials = getBiomeMaterials(fromBiome);
        RoadMaterials toMaterials = getBiomeMaterials(toBiome);
        
        // 分析地形特征
        for (BlockPos boundaryPos : boundary.getPositions()) {
            EnhancedTerrainAdapter.TerrainAnalysis analysis = terrainAdapter.analyzeTerrain(level, boundaryPos, 5);
            
            // 根据地形特征选择材料
            BlockState material;
            if (analysis.getSlopeGradient() > 0.3) {
                // 陡坡使用更稳定的材料
                material = fromMaterials.getBorderBlock();
            } else if (analysis.getHeightDifference() > 3) {
                // 高度差大时使用过渡材料
                material = fromMaterials.getTransitionBlock();
            } else {
                // 平坦区域混合材料
                double distanceRatio = Math.sqrt(boundaryPos.distSqr(fromPos)) / Math.sqrt(fromPos.distSqr(toPos));
                material = blendBlockStates(fromMaterials.getMainBlock(), toMaterials.getMainBlock(), distanceRatio);
            }
            
            // 应用地形适配
            List<BlockPos> adaptedPositions = terrainAdapter.applyTerrainAdaptation(level, boundaryPos, analysis);
            
            for (BlockPos pos : adaptedPositions) {
                level.setBlock(pos, material, 3);
                road.add(pos);
            }
        }
        
        return road;
    }
    
    /**
     * 混合两种方块状态（简化实现）
     */
    private static BlockState blendBlockStates(BlockState state1, BlockState state2, double ratio) {
        // 简化实现：根据比例选择其中一种方块
        // 在实际实现中，可能需要更复杂的混合逻辑
        return ratio < 0.5 ? state1 : state2;
    }
    
    /**
     * 生物群系边界数据类
     */
    public static class BiomeBoundary {
        private final List<BlockPos> positions;
        private final BiomeCategory primaryCategory;
        private final int transitionWidth;
        
        public BiomeBoundary(List<BlockPos> positions, BiomeCategory primaryCategory, int transitionWidth) {
            this.positions = positions;
            this.primaryCategory = primaryCategory;
            this.transitionWidth = transitionWidth;
        }
        
        public List<BlockPos> getPositions() { return positions; }
        public BiomeCategory getPrimaryCategory() { return primaryCategory; }
        public int getTransitionWidth() { return transitionWidth; }
        
        public boolean hasBoundary() {
            return !positions.isEmpty();
        }
    }
    
    /**
     * 获取推荐的过渡策略
     */
    public static TransitionStrategy getRecommendedTransitionStrategy(BiomeCategory from, BiomeCategory to) {
        // 基于生物群系类型推荐过渡策略
        if (from == to) {
            return TransitionStrategy.NATURAL;
        }
        
        // 极端环境之间的过渡使用渐进策略
        if ((from == BiomeCategory.NETHER || from == BiomeCategory.END) || 
            (to == BiomeCategory.NETHER || to == BiomeCategory.END)) {
            return TransitionStrategy.GRADUAL;
        }
        
        // 相似环境使用自然融合
        if (isSimilarEnvironment(from, to)) {
            return TransitionStrategy.NATURAL;
        }
        
        // 差异较大的环境使用混合风格
        return TransitionStrategy.BLENDED;
    }
    
    /**
     * 检查两个生物群系是否属于相似环境
     */
    private static boolean isSimilarEnvironment(BiomeCategory cat1, BiomeCategory cat2) {
        // 定义环境分组
        Set<BiomeCategory> temperateGroup = Set.of(BiomeCategory.PLAINS, BiomeCategory.FOREST, BiomeCategory.TAIGA);
        Set<BiomeCategory> aridGroup = Set.of(BiomeCategory.DESERT, BiomeCategory.MESA);
        Set<BiomeCategory> coldGroup = Set.of(BiomeCategory.SNOW, BiomeCategory.TAIGA);
        Set<BiomeCategory> wetGroup = Set.of(BiomeCategory.SWAMP, BiomeCategory.JUNGLE, BiomeCategory.OCEAN);
        
        return (temperateGroup.contains(cat1) && temperateGroup.contains(cat2)) ||
               (aridGroup.contains(cat1) && aridGroup.contains(cat2)) ||
               (coldGroup.contains(cat1) && coldGroup.contains(cat2)) ||
               (wetGroup.contains(cat1) && wetGroup.contains(cat2));
    }
    
    /**
     * 获取生物群系的美学协调方案
     */
    public static AestheticCoordination getAestheticCoordination(BiomeCategory from, BiomeCategory to) {
        TransitionStrategy strategy = getRecommendedTransitionStrategy(from, to);
        RoadMaterials fromMaterials = getBiomeMaterials(from);
        RoadMaterials toMaterials = getBiomeMaterials(to);
        
        return new AestheticCoordination(fromMaterials, toMaterials, strategy);
    }
    
    /**
     * 获取适应生物群系的材料列表
     * 这是RoadFeature调用的核心方法，用于在道路生成时应用生物群系连接策略
     */
    public List<BlockState> getAdaptedMaterials(LevelAccessor level, BlockPos position, 
                                               List<BlockState> originalMaterials, int roadType) {
        // 检测当前位置的生物群系
        Biome currentBiome = level.getBiome(position).value();
        ResourceLocation currentBiomeId = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
            .getKey(currentBiome);
        BiomeCategory currentCategory = BiomeCategory.fromBiome(currentBiomeId);
        
        // 获取当前生物群系的推荐材料
        RoadMaterials biomeMaterials = getBiomeMaterials(currentCategory);
        
        // 创建适应后的材料列表
        List<BlockState> adaptedMaterials = new ArrayList<>();
        
        // 根据道路类型和生物群系特性调整材料
        switch (roadType) {
            case 0: // 主要道路
                adaptedMaterials.add(biomeMaterials.getMainBlock());
                adaptedMaterials.add(biomeMaterials.getBorderBlock());
                if (originalMaterials.size() > 2) {
                    adaptedMaterials.add(biomeMaterials.getDecorationBlock());
                }
                break;
                
            case 1: // 次要道路/自然道路
                adaptedMaterials.add(biomeMaterials.getMainBlock());
                adaptedMaterials.add(biomeMaterials.getTransitionBlock());
                // 保留一些原始的自然材料特性
                if (!originalMaterials.isEmpty()) {
                    adaptedMaterials.add(originalMaterials.get(0));
                }
                break;
                
            default: // 其他道路类型
                // 混合使用生物群系材料和原始材料
                adaptedMaterials.add(biomeMaterials.getMainBlock());
                if (!originalMaterials.isEmpty()) {
                    adaptedMaterials.add(originalMaterials.get(0));
                }
                adaptedMaterials.add(biomeMaterials.getTransitionBlock());
                break;
        }
        
        // 确保材料列表不为空
        if (adaptedMaterials.isEmpty()) {
            adaptedMaterials.addAll(originalMaterials);
        }
        
        return adaptedMaterials;
    }
    
    /**
     * 检测生物群系边界并获取过渡材料
     */
    public List<BlockState> getTransitionMaterials(LevelAccessor level, BlockPos position, 
                                                  List<BlockState> originalMaterials) {
        // 检测生物群系边界
        BiomeBoundary boundary = detectBiomeBoundary(level, position, 8);
        
        if (!boundary.hasBoundary()) {
            // 没有边界，返回原始材料
            return originalMaterials;
        }
        
        // 获取推荐过渡策略
        Biome currentBiome = level.getBiome(position).value();
        ResourceLocation currentBiomeId = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
            .getKey(currentBiome);
        BiomeCategory currentCategory = BiomeCategory.fromBiome(currentBiomeId);
        
        TransitionStrategy strategy = getRecommendedTransitionStrategy(currentCategory, boundary.getPrimaryCategory());
        
        // 根据策略生成过渡材料
        List<BlockState> transitionMaterials = new ArrayList<>();
        RoadMaterials currentMaterials = getBiomeMaterials(currentCategory);
        RoadMaterials boundaryMaterials = getBiomeMaterials(boundary.getPrimaryCategory());
        
        switch (strategy) {
            case GRADUAL:
                // 渐进过渡：混合两种材料
                transitionMaterials.add(currentMaterials.getMainBlock());
                transitionMaterials.add(boundaryMaterials.getMainBlock());
                transitionMaterials.add(currentMaterials.getTransitionBlock());
                break;
                
            case SHARP:
                // 清晰边界：使用过渡材料作为标记
                transitionMaterials.add(currentMaterials.getTransitionBlock());
                transitionMaterials.add(boundaryMaterials.getTransitionBlock());
                break;
                
            case BLENDED:
                // 混合风格：交替使用两种材料
                transitionMaterials.add(currentMaterials.getMainBlock());
                transitionMaterials.add(boundaryMaterials.getMainBlock());
                transitionMaterials.add(currentMaterials.getBorderBlock());
                transitionMaterials.add(boundaryMaterials.getBorderBlock());
                break;
                
            case NATURAL:
                // 自然融合：基于当前材料添加自然元素
                transitionMaterials.addAll(originalMaterials);
                transitionMaterials.add(currentMaterials.getDecorationBlock());
                transitionMaterials.add(boundaryMaterials.getDecorationBlock());
                break;
        }
        
        return transitionMaterials;
    }
    
    /**
     * 美学协调方案
     */
    public static class AestheticCoordination {
        private final RoadMaterials fromMaterials;
        private final RoadMaterials toMaterials;
        private final TransitionStrategy strategy;
        private final int transitionWidth;
        
        public AestheticCoordination(RoadMaterials fromMaterials, RoadMaterials toMaterials, 
                                   TransitionStrategy strategy) {
            this.fromMaterials = fromMaterials;
            this.toMaterials = toMaterials;
            this.strategy = strategy;
            this.transitionWidth = calculateTransitionWidth(
                BiomeCategory.fromBiome(new ResourceLocation("")), // 占位符
                BiomeCategory.fromBiome(new ResourceLocation(""))  // 占位符
            );
        }
        
        public RoadMaterials getFromMaterials() { return fromMaterials; }
        public RoadMaterials getToMaterials() { return toMaterials; }
        public TransitionStrategy getStrategy() { return strategy; }
        public int getTransitionWidth() { return transitionWidth; }
        
        /**
         * 应用美学协调方案到道路生成
         */
        public List<BlockPos> applyToRoad(LevelAccessor level, BlockPos fromPos, BlockPos toPos) {
            return generateBiomeTransitionRoad(level, fromPos, toPos, 
                BiomeCategory.fromBiome(new ResourceLocation("")), // 占位符
                BiomeCategory.fromBiome(new ResourceLocation("")), // 占位符
                strategy);
        }
    }
}
