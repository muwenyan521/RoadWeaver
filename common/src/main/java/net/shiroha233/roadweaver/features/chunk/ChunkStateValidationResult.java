package net.shiroha233.roadweaver.features.chunk;

/**
 * 区块状态验证结果
 * <p>
 * 包含区块状态验证的详细结果信息。
 * </p>
 */
public record ChunkStateValidationResult(
    ChunkState chunkState,
    boolean isSafe,
    String message
) {
    
    /**
     * 简化的构造函数，自动生成消息
     */
    public ChunkStateValidationResult(ChunkState chunkState, boolean isSafe) {
        this(chunkState, isSafe, generateMessage(chunkState, isSafe));
    }
    
    private static String generateMessage(ChunkState chunkState, boolean isSafe) {
        if (isSafe) {
            return "Chunk is safe for road placement";
        } else {
            return "Chunk is not safe: " + chunkState.getStatusDescription();
        }
    }
    
    /**
     * 获取详细的验证报告
     */
    public String getDetailedReport() {
        return String.format(
            "Chunk Validation Report:\n" +
            "  Position: %s\n" +
            "  Loaded: %s\n" +
            "  Accessible: %s\n" +
            "  Generated: %s\n" +
            "  Safe: %s\n" +
            "  Message: %s",
            chunkState.chunkPos(),
            chunkState.isLoaded(),
            chunkState.isAccessible(),
            chunkState.isGenerated(),
            isSafe,
            message
        );
    }
    
    @Override
    public String toString() {
        return String.format("ChunkStateValidationResult{safe=%s, message='%s', state=%s}",
            isSafe, message, chunkState);
    }
}
