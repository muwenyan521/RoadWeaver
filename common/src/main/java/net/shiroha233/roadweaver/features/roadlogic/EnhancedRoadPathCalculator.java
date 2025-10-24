package net.shiroha233.roadweaver.features.roadlogic;

import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.shiroha233.roadweaver.helpers.Records;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强的道路路径计算器
 * <p>
 * 扩展原有的A*路径规划，集成结构发现功能。
 * 在路径规划的每个节点扩展时检测未注册结构，动态更新最小生成树。
 * </p>
 */
public class EnhancedRoadPathCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");

    // 继承原有的路径计算常量
    private static final int NEIGHBOR_DISTANCE = 4;
    private static final int WATER_BIOME_COST = 50;
    private static final int ELEVATION_COST_MULTIPLIER = 40;
    private static final int BIOME_COST_MULTIPLIER = 8;
    private static final int SEA_LEVEL_COST_MULTIPLIER = 8;
    private static final int TERRAIN_STABILITY_MULTIPLIER = 16;
    private static final double DIAGONAL_COST_MULTIPLIER = 1.5;
    private static final int HEURISTIC_DISTANCE_MULTIPLIER = 30;
    private static final double HEURISTIC_DIAGONAL_FACTOR = 0.6;

    // 结构发现相关常量
    private static final int STRUCTURE_DETECTION_RADIUS = 16;
    private static final double STRUCTURE_DISCOVERY_PROBABILITY = 0.1; // 10%概率检测结构

    // 高度缓存
    public static final Map<Long, Integer> heightCache = new ConcurrentHashMap<>();

    /**
     * 计算增强的A*道路路径（带结构发现）
     * <p>
     * 在原有A*算法基础上，集成结构发现和动态MST更新功能。
     * </p>
     * 
     * @param start 起点坐标
     * @param end 终点坐标
     * @param width 道路宽度
     * @param serverWorld 服务器世界
     * @param maxSteps 最大搜索步数
     * @param callback 结构发现回调
     * @return 道路段列表，如果未找到路径则返回空列表
     */
    public static List<Records.RoadSegmentPlacement> calculateEnhancedAStarRoadPath(
            BlockPos start, BlockPos end, int width, ServerLevel serverWorld, int maxSteps,
            StructureDiscoveryCallback callback) {
        
        IModConfig cfg = ConfigProvider.get();
        return calculateEnhancedAStarRoadPath(start, end, width, serverWorld, maxSteps,
                cfg.maxHeightDifference(), cfg.maxTerrainStability(), false, callback);
    }

    /**
     * 计算增强的A*道路路径（完整版本）
     */
    public static List<Records.RoadSegmentPlacement> calculateEnhancedAStarRoadPath(
            BlockPos start, BlockPos end, int width, ServerLevel serverWorld, int maxSteps,
            int maxHeightDifference, int maxTerrainStability, boolean ignoreWater,
            StructureDiscoveryCallback callback) {

        StructureDiscoveryPath discoveryPath = new StructureDiscoveryPath(start, end);
        PriorityQueue<StructureDiscoveryNode> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(StructureDiscoveryNode::getFScore));
        Map<BlockPos, StructureDiscoveryNode> allNodes = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, List<BlockPos>> interpolatedSegments = new HashMap<>();

        int startX = snapToGrid(start.getX(), NEIGHBOR_DISTANCE);
        int startZ = snapToGrid(start.getZ(), NEIGHBOR_DISTANCE);
        int endX = snapToGrid(end.getX(), NEIGHBOR_DISTANCE);
        int endZ = snapToGrid(end.getZ(), NEIGHBOR_DISTANCE);

        start = new BlockPos(startX, start.getY(), startZ);
        end = new BlockPos(endX, end.getY(), endZ);

        BlockPos startGround = new BlockPos(start.getX(), heightSampler(start.getX(), start.getZ(), serverWorld), start.getZ());
        BlockPos endGround = new BlockPos(end.getX(), heightSampler(end.getX(), end.getZ(), serverWorld), end.getZ());

        StructureDiscoveryNode startNode = new StructureDiscoveryNode(startGround, null, 0.0, heuristic(startGround, endGround));
        openSet.add(startNode);
        allNodes.put(startGround, startNode);
        discoveryPath.addNode(startNode);

        int d = NEIGHBOR_DISTANCE;
        int[][] neighborOffsets = {
                {d, 0}, {-d, 0}, {0, d}, {0, -d},
                {d, d}, {d, -d}, {-d, d}, {-d, -d}
        };

        while (!openSet.isEmpty() && maxSteps-- > 0) {
            StructureDiscoveryNode current = openSet.poll();

            if (current.getPos().offset(0, -current.getPos().getY(), 0)
                    .distManhattan(endGround.offset(0, -endGround.getY(), 0)) < NEIGHBOR_DISTANCE * 2) {
                LOGGER.debug("EnhancedRoadPathCalculator: 找到路径! {}", current.getPos());
                
                // 路径完成时处理所有发现的结构
                processDiscoveredStructures(discoveryPath, serverWorld, callback);
                
                return reconstructPath(current, width, interpolatedSegments);
            }

            closedSet.add(current.getPos());
            allNodes.remove(current.getPos());

            for (int[] offset : neighborOffsets) {
                BlockPos neighborXZ = current.getPos().offset(offset[0], 0, offset[1]);
                int y = heightSampler(neighborXZ.getX(), neighborXZ.getZ(), serverWorld);
                BlockPos neighborPos = new BlockPos(neighborXZ.getX(), y, neighborXZ.getZ());
                
                if (closedSet.contains(neighborPos)) continue;

                // 结构发现：检测当前位置是否存在未注册结构
                StructureDiscoveryNode neighborNode = processStructureDiscovery(
                        current, neighborPos, serverWorld, discoveryPath, callback);

                // 如果回调返回false，中断路径规划
                if (neighborNode == null) {
                    LOGGER.info("EnhancedRoadPathCalculator: 结构发现回调中断路径规划");
                    return Collections.emptyList();
                }

                Holder<Biome> biomeHolder = biomeSampler(neighborPos, serverWorld);
                boolean isWater = biomeHolder.is(BiomeTags.IS_RIVER)
                        || biomeHolder.is(BiomeTags.IS_OCEAN)
                        || biomeHolder.is(BiomeTags.IS_DEEP_OCEAN);
                int biomeCost = (isWater && !ignoreWater) ? WATER_BIOME_COST : 0;
                int elevation = Math.abs(y - current.getPos().getY());
                if (elevation > maxHeightDifference) {
                    continue;
                }
                int offsetSum = Math.abs(Math.abs(offset[0])) + Math.abs(offset[1]);
                double stepCost = (offsetSum == 2 * NEIGHBOR_DISTANCE) ? DIAGONAL_COST_MULTIPLIER : 1;
                int terrainStabilityCost = calculateTerrainStability(neighborPos, y, serverWorld);
                if (terrainStabilityCost > maxTerrainStability) {
                    continue;
                }
                int yLevelCost = y == serverWorld.getSeaLevel() ? 20 : 0;
                double tentativeG = current.getGScore() + stepCost
                        + elevation * ELEVATION_COST_MULTIPLIER
                        + biomeCost * BIOME_COST_MULTIPLIER
                        + yLevelCost * SEA_LEVEL_COST_MULTIPLIER
                        + terrainStabilityCost * TERRAIN_STABILITY_MULTIPLIER;

                StructureDiscoveryNode existingNeighbor = allNodes.get(neighborPos);
                if (existingNeighbor == null || tentativeG < existingNeighbor.getGScore()) {
                    double h = heuristic(neighborPos, endGround);
                    StructureDiscoveryNode neighbor = new StructureDiscoveryNode(
                            neighborPos, current, tentativeG, tentativeG + h);
                    
                    allNodes.put(neighborPos, neighbor);
                    openSet.add(neighbor);
                    discoveryPath.addNode(neighbor);

                    List<BlockPos> segmentPoints = new ArrayList<>();
                    for (int i = 1; i < NEIGHBOR_DISTANCE; i++) {
                        int interpX = current.getPos().getX() + (offset[0] * i) / NEIGHBOR_DISTANCE;
                        int interpZ = current.getPos().getZ() + (offset[1] * i) / NEIGHBOR_DISTANCE;
                        BlockPos interpolated = new BlockPos(interpX, current.getPos().getY(), interpZ);
                        segmentPoints.add(interpolated);
                    }
                    interpolatedSegments.put(neighborPos, segmentPoints);
                }
            }
        }

        // 路径规划失败时也处理已发现的结构
        processDiscoveredStructures(discoveryPath, serverWorld, callback);
        
        return Collections.emptyList();
    }

    /**
     * 处理结构发现
     */
    private static StructureDiscoveryNode processStructureDiscovery(
            StructureDiscoveryNode current, BlockPos neighborPos, ServerLevel serverWorld,
            StructureDiscoveryPath discoveryPath, StructureDiscoveryCallback callback) {
        
        // 概率性检测结构，避免性能开销
        if (Math.random() > STRUCTURE_DISCOVERY_PROBABILITY) {
            return new StructureDiscoveryNode(neighborPos, current, 0, 0); // 占位符，实际值在调用处计算
        }

        // 检测当前位置是否存在未注册结构
        Records.StructureInfo discoveredStructure = detectStructureAtPosition(neighborPos, serverWorld);
        
        if (discoveredStructure != null) {
            LOGGER.info("EnhancedRoadPathCalculator: 在位置 {} 发现结构 {}", neighborPos, discoveredStructure.structureId());
            
            // 创建带结构发现的节点
            StructureDiscoveryNode discoveryNode = StructureDiscoveryNode.withStructure(
                    neighborPos, current, 0, 0, discoveredStructure); // 实际值在调用处计算
            
            // 调用回调
            boolean shouldContinue = callback.onStructureDiscovered(
                    neighborPos, discoveredStructure, discoveryPath, serverWorld);
            
            if (!shouldContinue) {
                return null; // 中断路径规划
            }
            
            return discoveryNode;
        }
        
        return new StructureDiscoveryNode(neighborPos, current, 0, 0); // 占位符，实际值在调用处计算
    }

    /**
     * 处理所有发现的结构
     */
    private static void processDiscoveredStructures(StructureDiscoveryPath discoveryPath, 
                                                   ServerLevel serverWorld, 
                                                   StructureDiscoveryCallback callback) {
        
        if (!discoveryPath.hasDiscoveredStructures()) {
            return;
        }

        LOGGER.info("EnhancedRoadPathCalculator: 处理 {} 个发现的结构", 
                discoveryPath.getDiscoveredStructures().size());

        // 增量更新MST
        List<Records.StructureConnection> newConnections = IncrementalMSTUpdater.updateMSTIncrementally(
                serverWorld, discoveryPath.getDiscoveredStructures(), discoveryPath);

        // 将新连接添加到路径中
        for (Records.StructureConnection connection : newConnections) {
            discoveryPath.addNewConnection(connection);
        }

        // 检查是否需要重新规划路径
        if (IncrementalMSTUpdater.needsReplanning(discoveryPath.getDiscoveredStructures(), discoveryPath)) {
            LOGGER.info("EnhancedRoadPathCalculator: 发现重要结构，建议重新规划路径");
        }
    }

    /**
     * 检测指定位置是否存在未注册结构
     */
    private static Records.StructureInfo detectStructureAtPosition(BlockPos pos, ServerLevel serverWorld) {
        // 这里应该实现具体的结构检测逻辑
        // 目前使用简单的模拟检测
        
        // 检查当前位置是否已有结构
        // 这里可以集成Minecraft的结构检测系统
        
        // 模拟检测：随机返回结构信息（实际实现应该检查真实的结构）
        if (Math.random() < 0.05) { // 5%概率"发现"结构
            String[] structureTypes = {"village", "temple", "outpost", "ruins"};
            String randomType = structureTypes[(int) (Math.random() * structureTypes.length)];
            return new Records.StructureInfo(pos, randomType);
        }
        
        return null;
    }

    // 以下方法从原有RoadPathCalculator复制，保持兼容性

    private static double heuristic(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dz = a.getZ() - b.getZ();
        double dxzApprox = Math.abs(dx) + Math.abs(dz) - HEURISTIC_DIAGONAL_FACTOR * Math.min(Math.abs(dx), Math.abs(dz));
        return dxzApprox * HEURISTIC_DISTANCE_MULTIPLIER;
    }

    private static int calculateTerrainStability(BlockPos neighborPos, int y, ServerLevel serverWorld) {
        int cost = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos testPos = neighborPos.relative(direction);
            int testY = heightSampler(testPos.getX(), testPos.getZ(), serverWorld);
            int elevation = Math.abs(y - testY);
            cost += elevation;
        }
        return cost;
    }

    private static List<Records.RoadSegmentPlacement> reconstructPath(
            StructureDiscoveryNode endNode, int width, Map<BlockPos, List<BlockPos>> interpolatedPathMap) {
        
        List<StructureDiscoveryNode> pathNodes = new ArrayList<>();
        StructureDiscoveryNode current = endNode;
        while (current != null) {
            pathNodes.add(current);
            current = current.getParent();
        }
        Collections.reverse(pathNodes);

        Map<BlockPos, Set<BlockPos>> roadSegments = new LinkedHashMap<>();
        Set<BlockPos> widthCache = new HashSet<>();

        for (StructureDiscoveryNode node : pathNodes) {
            BlockPos pos = node.getPos();
            List<BlockPos> interpolated = interpolatedPathMap.getOrDefault(pos, Collections.emptyList());
            RoadDirection roadDirection = RoadDirection.X_AXIS;
            if (!interpolated.isEmpty()) {
                BlockPos firstInterpolated = interpolated.get(0);
                int dx = pos.getX() - firstInterpolated.getX();
                int dz = pos.getZ() - firstInterpolated.getZ();

                if ((dx < 0 && dz > 0) || (dx > 0 && dz < 0)) {
                    roadDirection = RoadDirection.DIAGONAL_1;
                } else if ((dx < 0 && dz < 0) || (dx > 0 && dz > 0)) {
                    roadDirection = RoadDirection.DIAGONAL_2;
                } else if (dx == 0 && dz != 0) {
                    roadDirection = RoadDirection.Z_AXIS;
                }

                for (BlockPos interp : interpolated) {
                    Set<BlockPos> widthSetInterp = generateWidth(interp, width / 2, widthCache, roadDirection);
                    roadSegments.put(interp, widthSetInterp);
                }
            }

            Set<BlockPos> widthSet = generateWidth(pos, width / 2, widthCache, roadDirection);
            roadSegments.put(pos, widthSet);
        }

        List<Records.RoadSegmentPlacement> result = new ArrayList<>();
        for (Map.Entry<BlockPos, Set<BlockPos>> entry : roadSegments.entrySet()) {
            result.add(new Records.RoadSegmentPlacement(entry.getKey(), new ArrayList<>(entry.getValue())));
        }
        return result;
    }

    private static int heightSampler(int x, int z, ServerLevel serverWorld) {
        long key = hashXZ(x, z);
        return heightCache.computeIfAbsent(key, k -> {
            int seaLevel = serverWorld.getSeaLevel();
            int oceanFloorHeight = serverWorld.getChunkSource()
                    .getGenerator()
                    .getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, serverWorld, serverWorld.getChunkSource().randomState());
            int worldSurfaceHeight = serverWorld.getChunkSource()
                    .getGenerator()
                    .getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, serverWorld, serverWorld.getChunkSource().randomState());

            if (worldSurfaceHeight <= seaLevel && oceanFloorHeight < seaLevel) {
                return seaLevel;
            }
            return worldSurfaceHeight;
        });
    }

    private static Holder<Biome> biomeSampler(BlockPos pos, ServerLevel serverWorld) {
        return serverWorld.getBiome(pos);
    }

    private static long hashXZ(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private static int snapToGrid(int value, int gridSize) {
        return Math.floorDiv(value, gridSize) * gridSize;
    }

    private static Set<BlockPos> generateWidth(BlockPos center, int radius, Set<BlockPos> widthPositionsCache, RoadDirection direction) {
        Set<BlockPos> segmentWidthPositions = new HashSet<>();
        int centerX = center.getX();
        int centerZ = center.getZ();
        int y = 0;

        if (direction == RoadDirection.X_AXIS) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = new BlockPos(centerX,
