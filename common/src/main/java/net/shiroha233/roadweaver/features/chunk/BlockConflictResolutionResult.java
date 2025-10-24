package net.shiroha233.roadweaver.features.chunk;

import net.minecraft.util.math.BlockPos;

/**
 * 方块冲突解决结果，记录冲突解决的状态和详细信息
 */
public record BlockConflictResolutionResult(
    BlockPos position,
    ConflictResolutionStrategy strategy,
    boolean resolved,
    String resolutionDetails,
    boolean requiresHeightAdjustment,
    int adjustedY,
    boolean blockPreserved,
    String preservedBlockType
) {
    
    /**
     * 创建成功解决的冲突结果
     */
    public static BlockConflictResolutionResult resolved(BlockPos position, ConflictResolutionStrategy strategy, String details) {
        return new BlockConflictResolutionResult(
            position, strategy, true, details, false, position.getY(), false, null
        );
    }
    
    /**
     * 创建需要高度调整的冲突结果
     */
    public static BlockConflictResolutionResult heightAdjusted(BlockPos position, int adjustedY, String details) {
        return new BlockConflictResolutionResult(
            position, ConflictResolutionStrategy.ADJUST_HEIGHT, true, details, true, adjustedY, false, null
        );
    }
    
    /**
     * 创建方块被保护的冲突结果
     */
    public static BlockConflictResolutionResult preserved(BlockPos position, String blockType, String details) {
        return new BlockConflictResolutionResult(
            position, ConflictResolutionStrategy.PRESERVE_IMPORTANT, true, details, false, position.getY(), true, blockType
        );
    }
    
    /**
     * 创建跳过的冲突结果
     */
    public static BlockConflictResolutionResult skipped(BlockPos position, String details) {
        return new BlockConflictResolutionResult(
            position, ConflictResolutionStrategy.SKIP, false, details, false, position.getY(), false, null
        );
    }
    
    /**
     * 创建失败的冲突结果
     */
    public static BlockConflictResolutionResult failed(BlockPos position, String details) {
        return new BlockConflictResolutionResult(
            position, ConflictResolutionStrategy.REPLACE, false, details, false, position.getY(), false, null
        );
    }
    
    /**
     * 检查是否成功解决冲突
     */
    public boolean isSuccessful() {
        return resolved;
    }
    
    /**
     * 检查是否需要高度调整
     */
    public boolean needsHeightAdjustment() {
        return requiresHeightAdjustment;
    }
    
    /**
     * 检查是否保护了方块
     */
    public boolean isBlockPreserved() {
        return blockPreserved;
    }
    
    /**
     * 获取调整后的位置
     */
    public BlockPos getAdjustedPosition() {
        if (requiresHeightAdjustment) {
            return new BlockPos(position.getX(), adjustedY, position.getZ());
        }
        return position;
    }
    
    /**
     * 生成详细的解决报告
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("位置: ").append(position.toShortString());
        report.append(", 策略: ").append(strategy);
        report.append(", 状态: ").append(resolved ? "已解决" : "未解决");
        
        if (resolutionDetails != null && !resolutionDetails.isEmpty()) {
            report.append(", 详情: ").append(resolutionDetails);
        }
        
        if (requiresHeightAdjustment) {
            report.append(", 高度调整: ").append(position.getY()).append(" -> ").append(adjustedY);
        }
        
        if (blockPreserved) {
            report.append(", 保护方块: ").append(preservedBlockType);
        }
        
        return report.toString();
    }
}
