package net.shiroha233.roadweaver.features.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.shiroha233.roadweaver.helpers.Records;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 区块状态管理器
 * <p>
 * 负责检测区块加载状态、管理区块强制加载、处理方块冲突解决。
 * 提供智能的区块状态验证和冲突解决机制。
 * </p>
 */
public class ChunkStateManager {
    
    private static final ChunkStateManager INSTANCE = new ChunkStateManager();
    
    // 区块状态缓存
    private final Map<Long, ChunkState> chunkStateCache = new ConcurrentHashMap<>();
    
    // 强制加载队列
    private final Queue<ChunkLoadRequest> chunkLoadQueue = new LinkedList<>();
    
    // 冲突解决策略
    private ConflictResolutionStrategy defaultStrategy = ConflictResolutionStrategy.REPLACE;
    
    private ChunkStateManager() {}
    
    public static ChunkStateManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 验证区块是否已加载并可安全放置道路
     * 
     * @param level 服务器世界
     * @param pos 要检查的位置
     * @return 区块状态验证结果
     */
    public ChunkStateValidationResult validateChunkState(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        long chunkKey = getChunkKey(chunkPos);
        
        // 检查缓存
        ChunkState cachedState = chunkStateCache.get(chunkKey);
        if (cachedState != null && !cachedState.isExpired()) {
            return new ChunkStateValidationResult(cachedState, true);
        }
        
        // 实时检查区块状态
        ChunkState currentState = checkChunkState(level, chunkPos);
        chunkStateCache.put(chunkKey, currentState);
        
        boolean isSafe = currentState.isLoaded() && currentState.isAccessible();
        return new ChunkStateValidationResult(currentState, isSafe);
    }
    
    /**
     * 批量验证多个区块状态
     * 
     * @param level 服务器世界
     * @param positions 要检查的位置列表
     * @return 验证结果映射
     */
    public Map<BlockPos, ChunkStateValidationResult> validateChunkStates(ServerLevel level, List<BlockPos> positions) {
        Map<BlockPos, ChunkStateValidationResult> results = new HashMap<>();
        
        for (BlockPos pos : positions) {
            results.put(pos, validateChunkState(level, pos));
        }
        
        return results;
    }
    
    /**
     * 强制加载区块
     * 
     * @param level 服务器世界
     * @param chunkPos 区块位置
     * @param priority 加载优先级
     * @param requester 请求者标识
     * @return 是否成功加入加载队列
     */
    public boolean forceLoadChunk(ServerLevel level, ChunkPos chunkPos, LoadPriority priority, String requester) {
        ChunkLoadRequest request = ChunkLoadRequest.create(chunkPos, priority, 60000L, requester);
        
        synchronized (chunkLoadQueue) {
            // 检查是否已在队列中
            for (ChunkLoadRequest existing : chunkLoadQueue) {
                if (existing.chunkPos().equals(chunkPos)) {
                    return false; // 已在队列中
                }
            }
            
            chunkLoadQueue.offer(request);
        }
        
        // 异步处理加载队列
        processLoadQueueAsync(level);
        
        return true;
    }
    
    /**
     * 处理道路放置前的区块状态验证和强制加载
     * 
     * @param level 服务器世界
     * @param roadSegments 道路段列表
     * @return 可安全放置的道路段列表
     */
    public List<Records.RoadSegmentPlacement> prepareChunksForRoadPlacement(
            ServerLevel level, List<Records.RoadSegmentPlacement> roadSegments) {
        
        List<Records.RoadSegmentPlacement> safeSegments = new ArrayList<>();
        List<ChunkLoadRequest> chunksToLoad = new ArrayList<>();
        
        // 收集所有需要检查的区块
        Set<ChunkPos> chunkPositions = new HashSet<>();
        for (Records.RoadSegmentPlacement segment : roadSegments) {
            for (BlockPos pos : segment.positions()) {
                chunkPositions.add(new ChunkPos(pos));
            }
        }
        
        // 验证区块状态
        for (ChunkPos chunkPos : chunkPositions) {
            ChunkState state = checkChunkState(level, chunkPos);
            
            if (!state.isLoaded() || !state.isAccessible()) {
                // 需要强制加载
                chunksToLoad.add(ChunkLoadRequest.createHighPriority(chunkPos, "RoadPlacement"));
            }
        }
        
        // 处理需要加载的区块
        if (!chunksToLoad.isEmpty()) {
            synchronized (chunkLoadQueue) {
                chunkLoadQueue.addAll(chunksToLoad);
            }
            processLoadQueueAsync(level);
            
            // 等待加载完成（简化实现，实际应该使用回调或等待机制）
            try {
                Thread.sleep(100); // 短暂等待
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 重新验证并过滤安全的路段
        for (Records.RoadSegmentPlacement segment : roadSegments) {
            boolean allSafe = true;
            
            for (BlockPos pos : segment.positions()) {
                ChunkStateValidationResult result = validateChunkState(level, pos);
                if (!result.isSafe()) {
                    allSafe = false;
                    break;
                }
            }
            
            if (allSafe) {
                safeSegments.add(segment);
            }
        }
        
        return safeSegments;
    }
    
    /**
     * 检查方块冲突并应用解决策略
     * 
     * @param level 服务器世界
     * @param pos 位置
     * @param targetState 目标方块状态
     * @param strategy 冲突解决策略
     * @return 冲突解决结果
     */
    public BlockConflictResolutionResult resolveBlockConflict(
            ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState targetState,
            ConflictResolutionStrategy strategy) {
        
        net.minecraft.world.level.block.state.BlockState currentState = level.getBlockState(pos);
        
        if (currentState.isAir()) {
            return BlockConflictResolutionResult.resolved(pos, strategy, "No conflict - empty block");
        }
        
        // 检查是否为重要方块（结构、玩家建筑等）
        boolean isImportantBlock = isImportantBlock(currentState, pos, level);
        
        switch (strategy) {
            case REPLACE:
                if (!isImportantBlock) {
                    level.setBlock(pos, targetState, 3);
                    return BlockConflictResolutionResult.resolved(pos, strategy, "Replaced non-important block");
                }
                break;
                
            case PRESERVE_IMPORTANT:
                if (!isImportantBlock) {
                    level.setBlock(pos, targetState, 3);
                    return BlockConflictResolutionResult.resolved(pos, strategy, "Replaced non-important block");
                } else {
                    return BlockConflictResolutionResult.preserved(pos, currentState.getBlock().getName().getString(), "Preserved important block");
                }
                
            case ADJUST_HEIGHT:
                BlockPos adjustedPos = findAlternativeHeight(level, pos, targetState);
                if (adjustedPos != null) {
                    level.setBlock(adjustedPos, targetState, 3);
                    return BlockConflictResolutionResult.heightAdjusted(pos, adjustedPos.getY(), "Adjusted height to avoid conflict");
                }
                break;
                
            case SKIP:
                return BlockConflictResolutionResult.skipped(pos, "Skipped due to conflict");
        }
        
        return BlockConflictResolutionResult.failed(pos, "Conflict resolution failed");
    }
    
    /**
     * 检查区块状态
     */
    private ChunkState checkChunkState(ServerLevel level, ChunkPos chunkPos) {
        // 使用Minecraft的区块状态检查方法
        boolean isLoaded = level.hasChunk(chunkPos.x, chunkPos.z);
        boolean isAccessible = false;
        boolean isGenerated = false;
        boolean isTerrainGenerated = false;
        
        if (isLoaded) {
            var chunk = level.getChunk(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                isAccessible = true;
                
                // 检查区块生成状态
                ChunkStatus status = chunk.getStatus();
                isGenerated = status.isOrAfter(ChunkStatus.FULL);
                isTerrainGenerated = status.isOrAfter(ChunkStatus.STRUCTURE_STARTS);
                
                // 对于道路放置，我们至少需要地形生成完成
                if (!isTerrainGenerated) {
                    // 如果地形未生成，尝试触发生成
                    try {
                        level.getChunkSource().getGenerator().createBiomes(
                            level.getRegistryAccess(), 
                            level.getChunkSource().randomState(), 
                            level.getStructureManager(), 
                            chunk
                        );
                        isTerrainGenerated = true;
                    } catch (Exception e) {
                        // 生成失败，区块不可用
                        isAccessible = false;
                    }
                }
            }
        }
        
        return new ChunkState(chunkPos, isLoaded, isAccessible, isGenerated, System.currentTimeMillis());
    }
    
    /**
     * 异步处理区块加载队列
     */
    private void processLoadQueueAsync(ServerLevel level) {
        level.getServer().execute(() -> {
            synchronized (chunkLoadQueue) {
                long currentTime = System.currentTimeMillis();
                Iterator<ChunkLoadRequest> iterator = chunkLoadQueue.iterator();
                
                while (iterator.hasNext()) {
                    ChunkLoadRequest request = iterator.next();
                    
                    // 移除超时的请求
                    if (request.isTimedOut(currentTime)) {
                        iterator.remove();
                        continue;
                    }
                    
                    // 处理加载请求
                    if (forceLoadChunkInternal(level, request.chunkPos())) {
                        iterator.remove();
                    }
                }
            }
        });
    }
    
    /**
     * 内部强制加载区块实现
     */
    private boolean forceLoadChunkInternal(ServerLevel level, ChunkPos chunkPos) {
        try {
            // 使用Minecraft的区块强制加载系统
            level.setChunkForced(chunkPos.x, chunkPos.z, true);
            
            // 获取区块，如果不存在则生成
            var chunk = level.getChunk(chunkPos.x, chunkPos.z);
            if (chunk == null) {
                // 如果区块不存在，使用区块源生成
                chunk = level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, true);
            }
            
            // 确保区块完全生成
            if (chunk != null && !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                // 使用适当的区块生成方法
                // 通过获取区块的所有部分来触发完整生成
                level.getChunkSource().getGenerator().createBiomes(level.getRegistryAccess(), 
                    level.getChunkSource().randomState(), level.getStructureManager(), chunk);
                
                // 生成地形特征
                level.getChunkSource().getGenerator().createReferences(level, chunk);
                
                // 生成结构
                level.getChunkSource().getGenerator().applyBiomeDecoration(level, chunk, 
                    level.getChunkSource().randomState());
            }
            
            // 验证区块是否已完全生成并可访问
            if (chunk != null && chunk.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                // 更新区块状态缓存
                long chunkKey = getChunkKey(chunkPos);
                chunkStateCache.put(chunkKey, new ChunkState(
                    chunkPos, true, true, true, System.currentTimeMillis()
                ));
                return true;
            }
            
            return false;
        } catch (Exception e) {
            // 记录错误但继续执行
            System.err.println("Failed to force load chunk at " + chunkPos + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查是否为重要方块
     */
    private boolean isImportantBlock(net.minecraft.world.level.block.state.BlockState state, BlockPos pos, ServerLevel level) {
        // 检查是否为玩家放置的方块（简化实现）
        // 实际实现应该检查方块标签、NBT数据等
        
        var block = state.getBlock();
        
        // 重要方块类型
        if (block.defaultBlockState().is(net.minecraft.tags.BlockTags.DOORS) ||
            block.defaultBlockState().is(net.minecraft.tags.BlockTags.BEDS) ||
            block.defaultBlockState().is(net.minecraft.tags.BlockTags.SIGNS) ||
            block.defaultBlockState().is(net.minecraft.tags.BlockTags.BANNERS)) {
            return true;
        }
        
        // 检查是否为结构的一部分（简化实现）
        // 实际实现应该集成Minecraft的结构检测系统
        
        return false;
    }
    
    /**
     * 寻找替代高度以避免冲突
     */
    private BlockPos findAlternativeHeight(ServerLevel level, BlockPos originalPos, net.minecraft.world.level.block.state.BlockState targetState) {
        // 向上搜索
        for (int yOffset = 1; yOffset <= 3; yOffset++) {
            BlockPos testPos = originalPos.above(yOffset);
            if (level.getBlockState(testPos).isAir()) {
                return testPos;
            }
        }
        
        // 向下搜索
        for (int yOffset = 1; yOffset <= 3; yOffset++) {
            BlockPos testPos = originalPos.below(yOffset);
            if (level.getBlockState(testPos.below()).canOcclude() && level.getBlockState(testPos).isAir()) {
                return testPos;
            }
        }
        
        return null;
    }
    
    private long getChunkKey(ChunkPos chunkPos) {
        return ((long) chunkPos.x << 32) | (chunkPos.z & 0xFFFFFFFFL);
    }
    
    // Getters and Setters
    public void setDefaultStrategy(ConflictResolutionStrategy strategy) {
        this.defaultStrategy = strategy;
    }
    
    public ConflictResolutionStrategy getDefaultStrategy() {
        return defaultStrategy;
    }
    
    public void clearCache() {
        chunkStateCache.clear();
    }
    
    public int getCacheSize() {
        return chunkStateCache.size();
    }
}
