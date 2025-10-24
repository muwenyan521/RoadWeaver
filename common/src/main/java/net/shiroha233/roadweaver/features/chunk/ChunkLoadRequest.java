package net.shiroha233.roadweaver.features.chunk;

import net.minecraft.util.math.ChunkPos;

/**
 * 区块加载请求，用于管理强制区块加载操作
 */
public record ChunkLoadRequest(
    ChunkPos chunkPos,
    LoadPriority priority,
    long requestTime,
    long timeout,
    String requester
) {
    
    /**
     * 检查请求是否已超时
     */
    public boolean isTimedOut(long currentTime) {
        return currentTime - requestTime > timeout;
    }
    
    /**
     * 创建新的区块加载请求
     */
    public static ChunkLoadRequest create(ChunkPos chunkPos, LoadPriority priority, long timeout, String requester) {
        return new ChunkLoadRequest(chunkPos, priority, System.currentTimeMillis(), timeout, requester);
    }
    
    /**
     * 创建高优先级区块加载请求
     */
    public static ChunkLoadRequest createHighPriority(ChunkPos chunkPos, String requester) {
        return create(chunkPos, LoadPriority.HIGH, 30000L, requester); // 30秒超时
    }
    
    /**
     * 创建标准优先级区块加载请求
     */
    public static ChunkLoadRequest createStandard(ChunkPos chunkPos, String requester) {
        return create(chunkPos, LoadPriority.STANDARD, 60000L, requester); // 60秒超时
    }
    
    /**
     * 创建低优先级区块加载请求
     */
    public static ChunkLoadRequest createLowPriority(ChunkPos chunkPos, String requester) {
        return create(chunkPos, LoadPriority.LOW, 120000L, requester); // 120秒超时
    }
}
