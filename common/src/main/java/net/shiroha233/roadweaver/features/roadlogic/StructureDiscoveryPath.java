package net.shiroha233.roadweaver.features.roadlogic;

import net.minecraft.core.BlockPos;
import net.shiroha233.roadweaver.helpers.Records;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 结构发现路径类
 * <p>
 * 管理路径规划过程中发现的结构信息，支持增量MST更新。
 * 跟踪已发现的结构、路径节点和连接信息。
 * </p>
 */
public class StructureDiscoveryPath {
    private final List<StructureDiscoveryNode> pathNodes;
    private final Set<Records.StructureInfo> discoveredStructures;
    private final List<Records.StructureConnection> newConnections;
    private final BlockPos start;
    private final BlockPos end;

    /**
     * 创建新的结构发现路径
     * 
     * @param start 路径起点
     * @param end 路径终点
     */
    public StructureDiscoveryPath(BlockPos start, BlockPos end) {
        this.start = start;
        this.end = end;
        this.pathNodes = new ArrayList<>();
        this.discoveredStructures = new HashSet<>();
        this.newConnections = new ArrayList<>();
    }

    /**
     * 添加路径节点
     */
    public void addNode(StructureDiscoveryNode node) {
        pathNodes.add(node);
        
        // 如果节点发现了结构，添加到发现的结构集合
        if (node.hasDiscoveredStructure()) {
            discoveredStructures.add(node.getDiscoveredStructureInfo());
        }
    }

    /**
     * 添加发现的结构
     */
    public void addDiscoveredStructure(Records.StructureInfo structureInfo) {
        discoveredStructures.add(structureInfo);
    }

    /**
     * 添加新的连接
     */
    public void addNewConnection(Records.StructureConnection connection) {
        newConnections.add(connection);
    }

    /**
     * 获取路径节点列表
     */
    public List<StructureDiscoveryNode> getPathNodes() {
        return new ArrayList<>(pathNodes);
    }

    /**
     * 获取发现的结构集合
     */
    public Set<Records.StructureInfo> getDiscoveredStructures() {
        return new HashSet<>(discoveredStructures);
    }

    /**
     * 获取新的连接列表
     */
    public List<Records.StructureConnection> getNewConnections() {
        return new ArrayList<>(newConnections);
    }

    /**
     * 获取路径起点
     */
    public BlockPos getStart() {
        return start;
    }

    /**
     * 获取路径终点
     */
    public BlockPos getEnd() {
        return end;
    }

    /**
     * 检查是否发现了任何结构
     */
    public boolean hasDiscoveredStructures() {
        return !discoveredStructures.isEmpty();
    }

    /**
     * 检查是否创建了任何新连接
     */
    public boolean hasNewConnections() {
        return !newConnections.isEmpty();
    }

    /**
     * 获取路径长度（节点数量）
     */
    public int getPathLength() {
        return pathNodes.size();
    }

    /**
     * 获取最后一个节点
     */
    public StructureDiscoveryNode getLastNode() {
        if (pathNodes.isEmpty()) {
            return null;
        }
        return pathNodes.get(pathNodes.size() - 1);
    }

    /**
     * 获取第一个节点
     */
    public StructureDiscoveryNode getFirstNode() {
        if (pathNodes.isEmpty()) {
            return null;
        }
        return pathNodes.get(0);
    }

    /**
     * 清空路径（用于重新规划）
     */
    public void clear() {
        pathNodes.clear();
        discoveredStructures.clear();
        newConnections.clear();
    }

    /**
     * 合并另一个路径的发现
     */
    public void merge(StructureDiscoveryPath otherPath) {
        this.pathNodes.addAll(otherPath.getPathNodes());
        this.discoveredStructures.addAll(otherPath.getDiscoveredStructures());
        this.newConnections.addAll(otherPath.getNewConnections());
    }

    @Override
    public String toString() {
        return "StructureDiscoveryPath{" +
                "start=" + start +
                ", end=" + end +
                ", pathLength=" + getPathLength() +
                ", discoveredStructures=" + discoveredStructures.size() +
                ", newConnections=" + newConnections.size() +
                '}';
    }
}
