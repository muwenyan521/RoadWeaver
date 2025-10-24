package net.shiroha233.roadweaver.features.chunk;

import net.minecraft.world.level.ChunkPos;

/**
 * 区块状态信息
 * <p>
 * 表示单个区块的加载和可访问状态。
 * </p>
 */
public record ChunkState(
    ChunkPos chunkPos,
    boolean isLoaded,
    boolean isAccessible,
    boolean isGenerated,
    long timestamp
) {
    
    private static final long CACHE_EXPIRY_TIME = 30_000; // 30秒缓存过期
    
    /**
     * 检查区块状态是否已过期
     * 
     * @return 如果状态已过期返回true
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME;
    }
    
    /**
     * 检查区块是否安全可访问
     * 
     * @return 如果区块已加载且可访问返回true
     */
    public boolean isSafe() {
        return isLoaded && isAccessible && isGenerated;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 人类可读的状态描述
     */
    public String getStatusDescription() {
        if (!isLoaded) {
            return "Chunk not loaded";
        }
        if (!isAccessible) {
            return "Chunk not accessible";
        }
        if (!isGenerated) {
            return "Chunk not fully generated";
        }
        return "Chunk ready";
    }
    
    @Override
    public String toString() {
        return String.format("ChunkState{pos=%s, loaded=%s, accessible=%s, generated=%s, age=%dms}",
            chunkPos, isLoaded, isAccessible, isGenerated, System.currentTimeMillis() - timestamp);
    }
}
