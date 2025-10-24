package net.shiroha233.roadweaver.features;

import com.mojang.serialization.Codec;
import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.shiroha233.roadweaver.features.chunk.ChunkStateManager;
import net.shiroha233.roadweaver.features.chunk.BlockConflictResolutionResult;
import net.shiroha233.roadweaver.features.chunk.ConflictResolutionStrategy;
import net.shiroha233.roadweaver.features.config.RoadFeatureConfig;
import net.shiroha233.roadweaver.features.config.RoadWidthManager;
import net.shiroha233.roadweaver.features.config.WidthLevelConfig;
import net.shiroha233.roadweaver.features.decoration.*;
import net.shiroha233.roadweaver.features.roadlogic.RoadPathCalculator;
import net.shiroha233.roadweaver.features.terrain.EnhancedTerrainAdapter;
import net.shiroha233.roadweaver.features.biome.BiomeConnectionStrategy;
import net.shiroha233.roadweaver.features.EnhancedStructureDetector;
import net.shiroha233.roadweaver.helpers.Records;
import net.shiroha233.roadweaver.helpers.StructureConnector;
import net.shiroha233.roadweaver.persistence.WorldDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 通用道路特性（Common）。
 * 依赖 ConfigProvider 与 WorldDataProvider 做平台桥接。
 */
public class RoadFeature extends Feature<RoadFeatureConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");

    // 道路放置相关常量
    public static final Set<Block> dontPlaceHere = new HashSet<>();
    static {
        dontPlaceHere.add(Blocks.PACKED_ICE);
        dontPlaceHere.add(Blocks.ICE);
        dontPlaceHere.add(Blocks.BLUE_ICE);
        dontPlaceHere.add(Blocks.TALL_SEAGRASS);
        dontPlaceHere.add(Blocks.MANGROVE_ROOTS);
    }

    // 结构搜索相关常量
    public static int chunksForLocatingCounter = 1;
    
    // 道路放置边界：避免在结构附近放置道路（单位：方块段）
    private static final int STRUCTURE_EDGE_OFFSET = 60;
    
    // 装饰放置相关常量
    private static final int SIGN_PLACEMENT_OFFSET = 65;
    private static final int LAMPPOST_DECORATION_SPACING = 59;
    private static final int FENCE_DECORATION_SPACING = 15;
    private static final int LARGE_DECORATION_SPACING = 80;
    private static final int WAYPOINT_SPACING = 25;
    
    // 清理常量：清理道路上方的空气
    private static final int CLEAR_HEIGHT_ABOVE_ROAD = 3;

    // 供 Fabric 端注册/引用，Forge 端不强制使用
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROAD_FEATURE_KEY =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("roadweaver", "road_feature"));
    public static final ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> ROAD_FEATURE_PLACED_KEY =
            ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation("roadweaver", "road_feature_placed"));

    public static final Feature<RoadFeatureConfig> ROAD_FEATURE = new RoadFeature(RoadFeatureConfig.CODEC);

    public RoadFeature(Codec<RoadFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RoadFeatureConfig> context) {
        // 使用配置控制缓存大小，避免内存溢出
        IModConfig config = ConfigProvider.get();
        if (RoadPathCalculator.heightCache.size() > config.heightCacheMaxSize()) {
            LOGGER.debug("Height cache size exceeded limit ({}), clearing cache", config.heightCacheMaxSize());
            RoadPathCalculator.heightCache.clear();
        }
        WorldGenLevel level = context.level();
        ServerLevel serverLevel = (ServerLevel) level.getLevel();
        WorldDataProvider dataProvider = WorldDataProvider.getInstance();

        Records.StructureLocationData structureLocationData = dataProvider.getStructureLocations(serverLevel);
        if (structureLocationData == null) {
            return false;
        }
        List<BlockPos> villageLocations = structureLocationData.structureLocations();
        
        // 🔍 调试日志：确认 RoadFeature 是否被调用
        if (chunksForLocatingCounter % 50 == 0) {
            LOGGER.info("RoadFeature.place() called, counter: {}, structures: {}", 
                chunksForLocatingCounter, villageLocations.size());
        }
        
        // 使用增强型结构检测器（如果启用）
        if (config.enableEnhancedStructureDetection()) {
            EnhancedStructureDetector enhancedDetector = new EnhancedStructureDetector();
            List<BlockPos> enhancedStructures = enhancedDetector.detectStructures(serverLevel, villageLocations);
            
            // 记录增强检测结果
            if (enhancedStructures.size() > villageLocations.size()) {
                LOGGER.info("🔍 Enhanced structure detection found {} structures (original: {})", 
                    enhancedStructures.size(), villageLocations.size());
            }
        }
        
        tryFindNewStructureConnection(villageLocations, serverLevel);
        Set<Decoration> roadDecorationCache = new HashSet<>();
        runEnhancedRoadLogic(level, context, roadDecorationCache);
        RoadStructures.tryPlaceDecorations(roadDecorationCache);
        return true;
    }

    private void tryFindNewStructureConnection(List<BlockPos> villageLocations, ServerLevel serverLevel) {
        // 移除数量限制，改为基于距离的智能搜寻
        chunksForLocatingCounter++;
        int triggerDistance = ConfigProvider.get().structureSearchTriggerDistance();
        if (chunksForLocatingCounter > triggerDistance) {
            LOGGER.info("🔍 Triggering new structure search (counter reached {}), current structures: {}", 
                triggerDistance, villageLocations.size());
            serverLevel.getServer().execute(() -> StructureConnector.cacheNewConnection(serverLevel, true));
            chunksForLocatingCounter = 1;
        }
    }

    private void runEnhancedRoadLogic(WorldGenLevel level, FeaturePlaceContext<RoadFeatureConfig> context, Set<Decoration> roadDecorationPlacementPositions) {
        IModConfig config = ConfigProvider.get();
        WorldDataProvider dataProvider = WorldDataProvider.getInstance();
        ServerLevel serverLevel = (ServerLevel) level.getLevel();

        int averagingRadius = config.averagingRadius();
        List<Records.RoadData> roadDataList = dataProvider.getRoadDataList(serverLevel);
        if (roadDataList == null) return;
        ChunkPos currentChunkPos = new ChunkPos(context.origin());

        // 初始化增强系统
        EnhancedTerrainAdapter terrainAdapter = null;
        BiomeConnectionStrategy biomeStrategy = null;
        
        if (config.enableTerrainAdaptation()) {
            terrainAdapter = new EnhancedTerrainAdapter();
        }
        
        if (config.enableBiomeConnectionStrategy()) {
            biomeStrategy = new BiomeConnectionStrategy();
        }

        Set<BlockPos> posAlreadyContainsSegment = new HashSet<>();
        for (Records.RoadData data : roadDataList) {
            int roadType = data.roadType();
            List<BlockState> materials = data.materials();
            List<Records.RoadSegmentPlacement> segmentList = data.roadSegmentList();

            List<BlockPos> middlePositions = segmentList.stream().map(Records.RoadSegmentPlacement::middlePos).toList();
            int segmentIndex = 0;
            for (int i = 2; i < segmentList.size() - 2; i++) {
                if (posAlreadyContainsSegment.contains(middlePositions.get(i))) continue;
                segmentIndex++;
                Records.RoadSegmentPlacement segment = segmentList.get(i);
                BlockPos segmentMiddlePos = segment.middlePos();
                // 靠近结构处不铺路
                if (segmentIndex < STRUCTURE_EDGE_OFFSET || segmentIndex > segmentList.size() - STRUCTURE_EDGE_OFFSET) continue;
                ChunkPos middleChunkPos = new ChunkPos(segmentMiddlePos);
                if (!middleChunkPos.equals(currentChunkPos)) continue;

                BlockPos prevPos = middlePositions.get(i - 2);
                BlockPos nextPos = middlePositions.get(i + 2);
                List<Double> heights = new ArrayList<>();
                for (int j = i - averagingRadius; j <= i + averagingRadius; j++) {
                    if (j >= 0 && j < middlePositions.size()) {
                        BlockPos samplePos = middlePositions.get(j);
                        double y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, samplePos.getX(), samplePos.getZ());
                        heights.add(y);
                    }
                }

                int averageY = (int) Math.round(heights.stream().mapToDouble(Double::doubleValue).average().orElse(segmentMiddlePos.getY()));
                BlockPos averagedPos = new BlockPos(segmentMiddlePos.getX(), averageY, segmentMiddlePos.getZ());

                // 应用地形适配（如果启用）
                if (terrainAdapter != null) {
                    averagedPos = terrainAdapter.adaptToTerrain(level, averagedPos, prevPos, nextPos);
                }

                // 应用生物群系连接策略（如果启用）
                List<BlockState> adaptedMaterials = materials;
                if (biomeStrategy != null) {
                    adaptedMaterials = biomeStrategy.getAdaptedMaterials(level, averagedPos, materials, roadType);
                }

                RandomSource random = context.random();
                if (!config.placeWaypoints()) {
                    for (BlockPos widthBlock : segment.positions()) {
                        BlockPos correctedYPos = new BlockPos(widthBlock.getX(), averagedPos.getY(), widthBlock.getZ());
                        placeEnhancedOnSurface(level, correctedYPos, adaptedMaterials, roadType, random, terrainAdapter);
                    }
                }
                addDecoration(level, roadDecorationPlacementPositions, averagedPos, segmentIndex, nextPos, prevPos, middlePositions, roadType, random, config);
                posAlreadyContainsSegment.add(segmentMiddlePos);
            }
        }
    }

    private void runRoadLogic(WorldGenLevel level, FeaturePlaceContext<RoadFeatureConfig> context, Set<Decoration> roadDecorationPlacementPositions) {
        IModConfig config = ConfigProvider.get();
        WorldDataProvider dataProvider = WorldDataProvider.getInstance();
        ServerLevel serverLevel = (ServerLevel) level.getLevel();

        int averagingRadius = config.averagingRadius();
        List<Records.RoadData> roadDataList = dataProvider.getRoadDataList(serverLevel);
        if (roadDataList == null) return;
        ChunkPos currentChunkPos = new ChunkPos(context.origin());

        Set<BlockPos> posAlreadyContainsSegment = new HashSet<>();
        for (Records.RoadData data : roadDataList) {
            int roadType = data.roadType();
            List<BlockState> materials = data.materials();
            List<Records.RoadSegmentPlacement> segmentList = data.roadSegmentList();

            List<BlockPos> middlePositions = segmentList.stream().map(Records.RoadSegmentPlacement::middlePos).toList();
            int segmentIndex = 0;
            for (int i = 2; i < segmentList.size() - 2; i++) {
                if (posAlreadyContainsSegment.contains(middlePositions.get(i))) continue;
                segmentIndex++;
                Records.RoadSegmentPlacement segment = segmentList.get(i);
                BlockPos segmentMiddlePos = segment.middlePos();
                // 靠近结构处不铺路
                if (segmentIndex < STRUCTURE_EDGE_OFFSET || segmentIndex > segmentList.size() - STRUCTURE_EDGE_OFFSET) continue;
                ChunkPos middleChunkPos = new ChunkPos(segmentMiddlePos);
                if (!middleChunkPos.equals(currentChunkPos)) continue;

                BlockPos prevPos = middlePositions.get(i - 2);
                BlockPos nextPos = middlePositions.get(i + 2);
                List<Double> heights = new ArrayList<>();
                for (int j = i - averagingRadius; j <= i + averagingRadius; j++) {
                    if (j >= 0 && j < middlePositions.size()) {
                        BlockPos samplePos = middlePositions.get(j);
                        double y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, samplePos.getX(), samplePos.getZ());
                        heights.add(y);
                    }
                }

                int averageY = (int) Math.round(heights.stream().mapToDouble(Double::doubleValue).average().orElse(segmentMiddlePos.getY()));
                BlockPos averagedPos = new BlockPos(segmentMiddlePos.getX(), averageY, segmentMiddlePos.getZ());

                RandomSource random = context.random();
                if (!config.placeWaypoints()) {
                    for (BlockPos widthBlock : segment.positions()) {
                        BlockPos correctedYPos = new BlockPos(widthBlock.getX(), averageY, widthBlock.getZ());
                        placeOnSurface(level, correctedYPos, materials, roadType, random);
                    }
                }
                addDecoration(level, roadDecorationPlacementPositions, averagedPos, segmentIndex, nextPos, prevPos, middlePositions, roadType, random, config);
                posAlreadyContainsSegment.add(segmentMiddlePos);
            }
        }
    }

    private static void tryPlaceDecorations(Set<Decoration> decorations) {
        RoadStructures.tryPlaceDecorations(decorations);
    }

    private void addDecoration(WorldGenLevel level, Set<Decoration> roadDecorationPlacementPositions,
                               BlockPos placePos, int segmentIndex, BlockPos nextPos, BlockPos prevPos, List<BlockPos> middleBlockPositions, int roadType, RandomSource random, IModConfig config) {
        BlockPos surfacePos = placePos.atY(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, placePos.getX(), placePos.getZ()));
        BlockState blockStateAtPos = level.getBlockState(surfacePos.below());
        
        // 获取道路宽度配置
        RoadWidthManager widthManager = RoadWidthManager.getInstance();
        ServerLevel serverLevel = (ServerLevel) level.getLevel();
        int roadWidth = widthManager.getRoadWidthForLevel(serverLevel, roadType);
        WidthLevelConfig widthConfig = widthManager.getWidthLevelConfig(roadWidth);
        
        // 水面在 placeOnSurface 中处理
        if (config.placeWaypoints()) {
            if (segmentIndex % WAYPOINT_SPACING == 0) {
                roadDecorationPlacementPositions.add(new FenceWaypointDecoration(surfacePos, level));
            }
            return;
        }
        
        int dx = nextPos.getX() - prevPos.getX();
        int dz = nextPos.getZ() - prevPos.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        int normDx = length != 0 ? (int) Math.round(dx / length) : 0;
        int normDz = length != 0 ? (int) Math.round(dz / length) : 0;
        Vec3i directionVector = new Vec3i(normDx, 0, normDz);

        Vec3i orthogonalVector = new Vec3i(-directionVector.getZ(), 0, directionVector.getX());
        boolean isEnd = segmentIndex != middleBlockPositions.size() - SIGN_PLACEMENT_OFFSET;
        BlockPos shiftedPos;
        
        // 基于宽度配置计算装饰间距
        int lampPostSpacing = widthConfig.calculateDecorationSpacing(LAMPPOST_DECORATION_SPACING, roadWidth);
        int fenceSpacing = widthConfig.calculateDecorationSpacing(FENCE_DECORATION_SPACING, roadWidth);
        int largeDecorationSpacing = widthConfig.calculateDecorationSpacing(LARGE_DECORATION_SPACING, roadWidth);
        
        if (segmentIndex == SIGN_PLACEMENT_OFFSET || segmentIndex == middleBlockPositions.size() - SIGN_PLACEMENT_OFFSET) {
            shiftedPos = isEnd ? placePos.offset(orthogonalVector.multiply(2)) : placePos.offset(orthogonalVector.multiply(-2));
            roadDecorationPlacementPositions.add(new DistanceSignDecoration(shiftedPos, orthogonalVector, level, isEnd, String.valueOf(middleBlockPositions.size())));
        }
        else if (segmentIndex % lampPostSpacing == 0) {
            boolean leftRoadSide = random.nextBoolean();
            shiftedPos = leftRoadSide ? placePos.offset(orthogonalVector.multiply(2)) : placePos.offset(orthogonalVector.multiply(-2));
            shiftedPos = shiftedPos.atY(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, shiftedPos.getX(), shiftedPos.getZ()));
            if (Math.abs(shiftedPos.getY() - placePos.getY()) > 1) {
                return;
            }
            if (roadType == 0) {
                roadDecorationPlacementPositions.add(new LamppostDecoration(shiftedPos, orthogonalVector, level, leftRoadSide));
            }
            else {
                roadDecorationPlacementPositions.add(new FenceWaypointDecoration(shiftedPos, level));
            }
        }
        // 间断栏杆装饰
        else if (config.placeRoadFences() && segmentIndex % fenceSpacing == 0) {
            boolean leftRoadSide = random.nextBoolean();
            shiftedPos = leftRoadSide ? placePos.offset(orthogonalVector.multiply(2)) : placePos.offset(orthogonalVector.multiply(-2));
            shiftedPos = shiftedPos.atY(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, shiftedPos.getX(), shiftedPos.getZ()));
            if (Math.abs(shiftedPos.getY() - placePos.getY()) > 1) {
                return;
            }
            int fenceLength = random.nextInt(1, 4);
            roadDecorationPlacementPositions.add(new RoadFenceDecoration(shiftedPos, orthogonalVector, level, leftRoadSide, fenceLength));
        }
        // 大型装饰（秋千、长椅、凉亭）
        else if (segmentIndex % largeDecorationSpacing == 0) {
            List<String> availableStructures = new ArrayList<>();
            if (config.placeSwings()) availableStructures.add("swing");
            if (config.placeBenches()) availableStructures.add("bench");
            if (config.placeGloriettes()) availableStructures.add("gloriette");
            if (availableStructures.isEmpty()) return;
            
            // 基于宽度配置控制大型装饰概率
            if (random.nextDouble() > widthConfig.getLargeDecorationProbability()) {
                return;
            }
            
            // 随机选择一个装饰类型
            String chosenStructure = availableStructures.get(random.nextInt(availableStructures.size()));
            
            // 放置在道路边，距离由配置决定
            boolean leftRoadSide = random.nextBoolean();
            int distanceFromRoad = config.structureDistanceFromRoad();
            shiftedPos = leftRoadSide 
                ? placePos.offset(orthogonalVector.multiply(distanceFromRoad)) 
                : placePos.offset(orthogonalVector.multiply(-distanceFromRoad));
            shiftedPos = shiftedPos.atY(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, shiftedPos.getX(), shiftedPos.getZ()));
            
            // 检查高度差是否合适
            if (Math.abs(shiftedPos.getY() - placePos.getY()) > 2) {
                return;
            }
            
            // 根据类型创建对应的装饰
            switch (chosenStructure) {
                case "swing":
                    roadDecorationPlacementPositions.add(new SwingDecoration(shiftedPos, orthogonalVector, level));
                    break;
                case "bench":
                    roadDecorationPlacementPositions.add(new BenchDecoration(shiftedPos, orthogonalVector, level));
                    break;
                case "gloriette":
                    roadDecorationPlacementPositions.add(new GlorietteDecoration(shiftedPos, orthogonalVector, level));
                    break;
            }
        }
    }

    private void placeEnhancedOnSurface(WorldGenLevel level, BlockPos placePos, List<BlockState> material, int natural, RandomSource random, EnhancedTerrainAdapter terrainAdapter) {
        IModConfig config = ConfigProvider.get();
        double naturalBlockChance = 0.5;
        BlockPos surfacePos = placePos;
        if (natural == 1 || config.averagingRadius() == 0) {
            surfacePos = new BlockPos(placePos.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, placePos.getX(), placePos.getZ()), placePos.getZ());
        }
        BlockPos topPos = new BlockPos(surfacePos.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, surfacePos.getX(), surfacePos.getZ()), surfacePos.getZ());
        BlockState blockStateAtPos = level.getBlockState(topPos.below());

        // 水面则放置未点燃营火
        if (blockStateAtPos.equals(Blocks.WATER.defaultBlockState())) {
            level.setBlock(topPos, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false), 3);
            return;
        }

        // 使用区块状态管理器验证区块状态
        ChunkStateManager chunkManager = ChunkStateManager.getInstance();
        ServerLevel serverLevel = (ServerLevel) level.getLevel();
        
        // 验证区块状态
        var chunkValidationResult = chunkManager.validateChunkState(serverLevel, surfacePos);
        if (!chunkValidationResult.isSafe()) {
            LOGGER.debug("Chunk at {} is not safe for road placement, skipping", surfacePos);
            return;
        }

        // 应用地形适配（如果启用且适配器可用）
        if (terrainAdapter != null && config.enableTerrainAdaptation()) {
            // 获取周围地形信息用于坡度分析
            BlockPos[] surroundingPositions = terrainAdapter.getSurroundingPositions(surfacePos, 2);
            surfacePos = terrainAdapter.applySlopeAdaptation(level, surfacePos, surroundingPositions);
            
            // 应用台阶替换算法
            if (config.enableStepReplacement()) {
                terrainAdapter.applyStepReplacement(level, surfacePos, material, random);
                return; // 台阶替换会处理完整的道路放置
            }
        }

        // 放置道路
        if (natural == 0 || random.nextDouble() < naturalBlockChance) {
            placeRoadBlock(level, blockStateAtPos, surfacePos, material, random);
        }
    }

    private void placeOnSurface(WorldGenLevel level, BlockPos placePos, List<BlockState> material, int natural, RandomSource random) {
        IModConfig config = ConfigProvider.get();
        double naturalBlockChance = 0.5;
        BlockPos surfacePos = placePos;
        if (natural == 1 || config.averagingRadius() == 0) {
            surfacePos = new BlockPos(placePos.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, placePos.getX(), placePos.getZ()), placePos.getZ());
        }
        BlockPos topPos = new BlockPos(surfacePos.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, surfacePos.getX(), surfacePos.getZ()), surfacePos.getZ());
        BlockState blockStateAtPos = level.getBlockState(topPos.below());

        // 水面则放置未点燃营火
        if (blockStateAtPos.equals(Blocks.WATER.defaultBlockState())) {
            level.setBlock(topPos, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false), 3);
            return;
        }

        // 使用区块状态管理器验证区块状态
        ChunkStateManager chunkManager = ChunkStateManager.getInstance();
        ServerLevel serverLevel = (ServerLevel) level.getLevel();
        
        // 验证区块状态
        var chunkValidationResult = chunkManager.validateChunkState(serverLevel, surfacePos);
        if (!chunkValidationResult.isSafe()) {
            LOGGER.debug("Chunk at {} is not safe for road placement, skipping", surfacePos);
            return;
        }

        // 放置道路
        if (natural == 0 || random.nextDouble() < naturalBlockChance) {
            placeRoadBlock(level, blockStateAtPos, surfacePos, material, random);
        }
    }

    private void placeRoadBlock(WorldGenLevel level, BlockState blockStateAtPos, BlockPos surfacePos, List<BlockState> materials, RandomSource deterministicRandom) {
        if (!placeAllowedCheck(blockStateAtPos.getBlock())
                || (!level.getBlockState(surfacePos.below()).canOcclude())
                && !level.getBlockState(surfacePos.below(2)).canOcclude()) {
            return;
        }
        
        // 使用区块状态管理器进行智能冲突解决
        ChunkStateManager chunkManager = ChunkStateManager.getInstance();
        ServerLevel serverLevel = (ServerLevel) level.getLevel();
        BlockState material = materials.get(deterministicRandom.nextInt(materials.size()));
        
        // 解决方块冲突
        BlockConflictResolutionResult resolutionResult = chunkManager.resolveBlockConflict(
            serverLevel, surfacePos.below(), material, ConflictResolutionStrategy.PRESERVE_IMPORTANT
        );
        
        // 根据冲突解决结果决定是否放置道路
        if (resolutionResult.isSuccessful()) {
            BlockPos placementPos = resolutionResult.needsHeightAdjustment() 
                ? resolutionResult.getAdjustedPosition() 
                : surfacePos.below();
            level.setBlock(placementPos, material, 3);
            
            // 清理道路上方的方块
            for (int i = 0; i < CLEAR_HEIGHT_ABOVE_ROAD; i++) {
                BlockState blockStateUp = level.getBlockState(surfacePos.above(i));
                if (!blockStateUp.getBlock().equals(Blocks.AIR) && !blockStateUp.is(BlockTags.LOGS) && !blockStateUp.is(BlockTags.FENCES)) {
                    level.setBlock(surfacePos.above(i), Blocks.AIR.defaultBlockState(), 3);
                } else {
                    break;
                }
            }

            BlockPos belowPos1 = surfacePos.below(2);
            BlockState belowState1 = level.getBlockState(belowPos1);
            if (belowState1.getBlock().equals(Blocks.GRASS_BLOCK)) {
                level.setBlock(belowPos1, Blocks.DIRT.defaultBlockState(), 3);
            }
            
            // 记录冲突解决信息
            if (resolutionResult.isBlockPreserved()) {
                LOGGER.debug("Block conflict resolved at {}: {}", surfacePos, resolutionResult.generateReport());
            }
        } else {
            LOGGER.debug("Road placement skipped at {} due to conflict resolution: {}", surfacePos, resolutionResult.generateReport());
        }
    }

    private boolean placeAllowedCheck(Block blockToCheck) {
        return !(dontPlaceHere.contains(blockToCheck)
                || blockToCheck.defaultBlockState().is(BlockTags.LEAVES)
                || blockToCheck.defaultBlockState().is(BlockTags.LOGS)
                || blockToCheck.defaultBlockState().is(BlockTags.UNDERWATER_BONEMEALS)
                || blockToCheck.defaultBlockState().is(BlockTags.WOODEN_FENCES)
                || blockToCheck.defaultBlockState().is(BlockTags.PLANKS));
    }
}
