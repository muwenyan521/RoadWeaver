package net.shiroha233.roadweaver.features.chunk;

/**
 * 冲突解决策略枚举
 * <p>
 * 定义在道路放置过程中遇到方块冲突时的处理策略。
 * </p>
 */
public enum ConflictResolutionStrategy {
    /**
     * 替换策略 - 直接替换冲突的方块
     * 适用于大多数情况，但可能破坏玩家建筑
     */
    REPLACE,
    
    /**
     * 保护重要方块 - 只替换非重要方块
     * 保护玩家建筑和重要结构，替换自然生成的方块
     */
    PRESERVE_IMPORTANT,
    
    /**
     * 调整高度 - 寻找替代高度放置道路
     * 避免冲突，保持原有方块不变
     */
    ADJUST_HEIGHT,
    
    /**
     * 跳过策略 - 跳过冲突位置
     * 最保守的策略，完全避免任何破坏
     */
    SKIP
}
