package net.shiroha233.roadweaver.features.roadlogic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.shiroha233.roadweaver.helpers.Records;
import net.shiroha233.roadweaver.persistence.WorldDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 增量最小生成树更新器
 * <p>
 * 在路径规划过程中发现新结构时，动态更新最小生成树。
 * 避免全图重算，只更新受影响的连接部分。
 * </p>
 */
public class IncrementalMSTUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");

    /**
     * 增量更新最小生成树
     * <p>
     * 当发现新结构时，找到最优方式将其连接到现有道路网络。
     * 使用Prim算法的增量版本，只考虑新结构与现有网络的连接。
     * </p>
     * 
     * @param serverWorld 服务器世界
     * @param discoveredStructures 新发现的结构
     * @param currentPath 当前路径（包含路径上下文）
     * @return 新创建的连接列表
     */
    public static List<Records.StructureConnection> updateMSTIncrementally(
            ServerLevel serverWorld, 
            Set<Records.StructureInfo> discoveredStructures,
            StructureDiscoveryPath currentPath) {
        
        if (discoveredStructures.isEmpty()) {
            return Collections.emptyList();
        }

        WorldDataProvider dataProvider = WorldDataProvider.getInstance();
        Records.StructureLocationData existingData = dataProvider.getStructureLocations(serverWorld);
        List<Records.StructureConnection> existingConnections = dataProvider.getStructureConnections(serverWorld);

        if (existingData == null || existingConnections == null) {
            LOGGER.warn("RoadWeaver: 无法更新MST - 现有数据为空");
            return Collections.emptyList();
        }

        List<Records.StructureConnection> newConnections = new ArrayList<>();
        Set<BlockPos> connectedStructures = getConnectedStructures(existingConnections);

        // 为每个新发现的结构找到最优连接
        for (Records.StructureInfo newStructure : discoveredStructures) {
            BlockPos newPos = newStructure.pos();

            // 如果新结构已经在网络中，跳过
            if (connectedStructures.contains(newPos)) {
                continue;
            }

            // 找到最近的已连接结构
            BlockPos nearestConnected = findNearestConnectedStructure(newPos, connectedStructures);
            if (nearestConnected != null) {
                Records.StructureConnection newConnection = new Records.StructureConnection(newPos, nearestConnected);
                newConnections.add(newConnection);
                connectedStructures.add(newPos); // 将新结构标记为已连接
                
                LOGGER.info("RoadWeaver: 增量MST - 创建连接 {} <-> {} (距离: {} 格)", 
                        newPos, nearestConnected, (int) Math.sqrt(newPos.distSqr(nearestConnected)));
            }
        }

        // 如果新结构之间也需要连接（当发现多个新结构时）
        if (discoveredStructures.size() > 1) {
            List<Records.StructureConnection> internalConnections = 
                    connectNewStructuresInternally(discoveredStructures, connectedStructures);
            newConnections.addAll(internalConnections);
        }

        // 保存更新后的连接
        if (!newConnections.isEmpty()) {
            List<Records.StructureConnection> updatedConnections = new ArrayList<>(existingConnections);
            updatedConnections.addAll(newConnections);
            dataProvider.setStructureConnections(serverWorld, updatedConnections);
            
            // 更新结构位置数据
            updateStructureLocations(serverWorld, discoveredStructures, dataProvider, existingData);
            
            LOGGER.info("RoadWeaver: 增量MST更新完成 - 添加了 {} 个新连接", newConnections.size());
        }

        return newConnections;
    }

    /**
     * 找到最近的已连接结构
     */
    private static BlockPos findNearestConnectedStructure(BlockPos target, Set<BlockPos> connectedStructures) {
        BlockPos nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (BlockPos connected : connectedStructures) {
            double distance = target.distSqr(connected);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = connected;
            }
        }

        return nearest;
    }

    /**
     * 连接新发现的结构之间
     */
    private static List<Records.StructureConnection> connectNewStructuresInternally(
            Set<Records.StructureInfo> newStructures, Set<BlockPos> connectedStructures) {
        
        List<Records.StructureConnection> connections = new ArrayList<>();
        List<BlockPos> newPositions = newStructures.stream()
                .map(Records.StructureInfo::pos)
                .filter(pos -> !connectedStructures.contains(pos))
                .toList();

        if (newPositions.size() < 2) {
            return connections;
        }

        // 使用Kruskal算法连接新结构
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < newPositions.size(); i++) {
            for (int j = i + 1; j < newPositions.size(); j++) {
                BlockPos from = newPositions.get(i);
                BlockPos to = newPositions.get(j);
                double distance = Math.sqrt(from.distSqr(to));
                edges.add(new Edge(from, to, distance));
            }
        }

        // 按距离排序
        edges.sort(Comparator.comparingDouble(e -> e.distance));

        // 使用并查集避免环路
        UnionFind uf = new UnionFind(newPositions);

        for (Edge edge : edges) {
            if (uf.union(edge.from, edge.to)) {
                Records.StructureConnection connection = new Records.StructureConnection(edge.from, edge.to);
                connections.add(connection);
                connectedStructures.add(edge.from);
                connectedStructures.add(edge.to);
                
                LOGGER.debug("RoadWeaver: 连接新结构 {} <-> {} (距离: {} 格)", 
                        edge.from, edge.to, (int) edge.distance);
            }
        }

        return connections;
    }

    /**
     * 更新结构位置数据
     */
    private static void updateStructureLocations(ServerLevel serverWorld, 
                                               Set<Records.StructureInfo> discoveredStructures,
                                               WorldDataProvider dataProvider,
                                               Records.StructureLocationData existingData) {
        
        List<BlockPos> updatedLocations = new ArrayList<>(existingData.structureLocations());
        List<Records.StructureInfo> updatedInfos = new ArrayList<>(existingData.structureInfos());

        for (Records.StructureInfo newStructure : discoveredStructures) {
            if (!updatedLocations.contains(newStructure.pos())) {
                updatedLocations.add(newStructure.pos());
            }
            if (!updatedInfos.contains(newStructure)) {
                updatedInfos.add(newStructure);
            }
        }

        Records.StructureLocationData updatedData = new Records.StructureLocationData(updatedLocations, updatedInfos);
        dataProvider.setStructureLocations(serverWorld, updatedData);
    }

    /**
     * 获取所有已连接的结构
     */
    private static Set<BlockPos> getConnectedStructures(List<Records.StructureConnection> connections) {
        Set<BlockPos> connected = new HashSet<>();
        for (Records.StructureConnection conn : connections) {
            connected.add(conn.from());
            connected.add(conn.to());
        }
        return connected;
    }

    /**
     * 边数据结构（用于内部Kruskal算法）
     */
    private static class Edge {
        final BlockPos from;
        final BlockPos to;
        final double distance;

        Edge(BlockPos from, BlockPos to, double distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }
    }

    /**
     * 并查集数据结构（用于内部Kruskal算法）
     */
    private static class UnionFind {
        private final Map<BlockPos, BlockPos> parent = new HashMap<>();

        UnionFind(List<BlockPos> nodes) {
            for (BlockPos node : nodes) {
                parent.put(node, node);
            }
        }

        BlockPos find(BlockPos node) {
            if (!parent.get(node).equals(node)) {
                parent.put(node, find(parent.get(node)));
            }
            return parent.get(node);
        }

        boolean union(BlockPos a, BlockPos b) {
            BlockPos rootA = find(a);
            BlockPos rootB = find(b);
            
            if (rootA.equals(rootB)) {
                return false;
            }
            
            parent.put(rootA, rootB);
            return true;
        }
    }

    /**
     * 检查是否需要重新规划路径
     * <p>
     * 当发现重要结构时，可能需要中断当前路径并重新规划
     * </p>
     * 
     * @param discoveredStructures 发现的结构
     * @param currentPath 当前路径
     * @return 是否需要重新规划
     */
    public static boolean needsReplanning(Set<Records.StructureInfo> discoveredStructures, StructureDiscoveryPath currentPath) {
        // 如果发现的结构在路径附近，可能需要重新规划以优化连接
        if (discoveredStructures.isEmpty()) {
            return false;
        }

        // 简单的启发式：如果发现的结构数量超过阈值，重新规划
        if (discoveredStructures.size() >= 3) {
            LOGGER.info("RoadWeaver: 发现 {} 个新结构，触发路径重新规划", discoveredStructures.size());
            return true;
        }

        // 检查发现的结构是否在路径的关键位置
        BlockPos start = currentPath.getStart();
        BlockPos end = currentPath.getEnd();
        
        for (Records.StructureInfo structure : discoveredStructures) {
            BlockPos pos = structure.pos();
            double distanceToStart = Math.sqrt(pos.distSqr(start));
            double distanceToEnd = Math.sqrt(pos.distSqr(end));
            double pathLength = Math.sqrt(start.distSqr(end));
            
            // 如果结构在路径中间附近，可能需要重新规划
            if (distanceToStart < pathLength * 0.3 && distanceToEnd < pathLength * 0.3) {
                LOGGER.info("RoadWeaver: 在路径关键位置发现结构 {}，触发重新规划", pos);
                return true;
            }
        }

        return false;
    }
}
