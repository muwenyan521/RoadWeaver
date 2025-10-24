package net.shiroha233.roadweaver.features.roadlogic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.shiroha233.roadweaver.helpers.Records;

/**
 * 结构发现回调接口
 * <p>
 * 定义在路径规划过程中发现新结构时的回调机制。
 * 允许动态处理新发现的结构并更新道路网络。
 * </p>
 */
@FunctionalInterface
public interface StructureDiscoveryCallback {
    
    /**
     * 当在路径规划过程中发现新结构时调用
     * 
     * @param discoveredPos 发现结构的位置
     * @param structureInfo 发现的结构信息
     * @param currentPath 当前正在计算的路径
     * @param serverWorld 服务器世界
     * @return 是否应该继续路径规划（true=继续，false=中断）
     */
    boolean onStructureDiscovered(BlockPos discoveredPos, Records.StructureInfo structureInfo, 
                                 StructureDiscoveryPath currentPath, ServerLevel serverWorld);
    
    /**
     * 默认回调：记录发现的结构并继续路径规划
     */
    static StructureDiscoveryCallback defaultCallback() {
        return (discoveredPos, structureInfo, currentPath, serverWorld) -> {
            // 记录发现的结构
            currentPath.addDiscoveredStructure(structureInfo);
            // 继续路径规划
            return true;
        };
    }
    
    /**
     * 创建中断路径规划的回调
     * 当发现重要结构时中断当前路径并重新规划
     */
    static StructureDiscoveryCallback interruptOnDiscovery() {
        return (discoveredPos, structureInfo, currentPath, serverWorld) -> {
            // 记录发现的结构
            currentPath.addDiscoveredStructure(structureInfo);
            // 中断当前路径规划，需要重新规划
            return false;
        };
    }
}
