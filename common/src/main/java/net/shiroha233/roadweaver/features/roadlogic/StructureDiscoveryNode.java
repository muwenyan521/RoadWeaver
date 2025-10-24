package net.shiroha233.roadweaver.features.roadlogic;

import net.minecraft.core.BlockPos;
import net.shiroha233.roadweaver.helpers.Records;

/**
 * 扩展的路径节点类，包含结构发现功能
 * <p>
 * 在A*路径规划的每个节点扩展时，检测当前位置是否存在未注册结构。
 * 当发现新结构时动态更新最小生成树，确保道路连续生成并正确连接意外发现的结构。
 * </p>
 */
public class StructureDiscoveryNode {
    private final BlockPos pos;
    private final StructureDiscoveryNode parent;
    private final double gScore;
    private final double fScore;
    private final boolean discoveredStructure;
    private final Records.StructureInfo discoveredStructureInfo;

    /**
     * 创建新的结构发现节点
     * 
     * @param pos 节点位置
     * @param parent 父节点
     * @param gScore 从起点到当前节点的实际成本
     * @param fScore 总估算成本 (gScore + 启发式)
     * @param discoveredStructure 是否在此节点发现结构
     * @param discoveredStructureInfo 发现的结构信息
     */
    public StructureDiscoveryNode(BlockPos pos, StructureDiscoveryNode parent, double gScore, double fScore,
                                 boolean discoveredStructure, Records.StructureInfo discoveredStructureInfo) {
        this.pos = pos;
        this.parent = parent;
        this.gScore = gScore;
        this.fScore = fScore;
        this.discoveredStructure = discoveredStructure;
        this.discoveredStructureInfo = discoveredStructureInfo;
    }

    /**
     * 创建新的结构发现节点（未发现结构）
     */
    public StructureDiscoveryNode(BlockPos pos, StructureDiscoveryNode parent, double gScore, double fScore) {
        this(pos, parent, gScore, fScore, false, null);
    }

    /**
     * 创建新的结构发现节点（发现结构）
     */
    public static StructureDiscoveryNode withStructure(BlockPos pos, StructureDiscoveryNode parent, double gScore, double fScore,
                                                      Records.StructureInfo structureInfo) {
        return new StructureDiscoveryNode(pos, parent, gScore, fScore, true, structureInfo);
    }

    // Getters
    public BlockPos getPos() {
        return pos;
    }

    public StructureDiscoveryNode getParent() {
        return parent;
    }

    public double getGScore() {
        return gScore;
    }

    public double getFScore() {
        return fScore;
    }

    public boolean hasDiscoveredStructure() {
        return discoveredStructure;
    }

    public Records.StructureInfo getDiscoveredStructureInfo() {
        return discoveredStructureInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StructureDiscoveryNode that = (StructureDiscoveryNode) obj;
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public String toString() {
        return "StructureDiscoveryNode{" +
                "pos=" + pos +
                ", discoveredStructure=" + discoveredStructure +
                ", structureInfo=" + discoveredStructureInfo +
                '}';
    }
}
