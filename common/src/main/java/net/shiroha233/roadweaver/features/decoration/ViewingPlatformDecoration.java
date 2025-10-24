package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.shiroha233.roadweaver.features.roadlogic.Road;
import net.shiroha233.roadweaver.features.biome.BiomeConnectionStrategy;

import java.util.Random;

/**
 * 观景台装饰类
 * 生成专门用于观景的平台，通常位于高处或水边
 */
public class ViewingPlatformDecoration extends AbstractDecoration {
    
    public ViewingPlatformDecoration(LevelAccessor level, Random random, 
                                   BiomeConnectionStrategy biomeStrategy) {
        super(level, random, biomeStrategy);
    }
    
    @Override
    public void generateDecoration(Road road, BlockPos roadPos) {
        BlockPos basePos = findSuitableGround(roadPos);
        if (basePos == null) return;
        
        // 观景台平台 (7x7)
        createPlatform(basePos, 7);
        
        // 观景台栏杆
        createRailings(basePos, 7);
        
        // 观景台座椅
        createBenches(basePos);
        
        // 观景台望远镜或装饰
        createTelescope(basePos);
        
        // 观景台照明
        createLighting(basePos);
    }
    
    /**
     * 创建观景台平台
     */
    private void createPlatform(BlockPos centerPos, int size) {
        int radius = size / 2;
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos pos = centerPos.offset(x, 0, z);
                BlockState platformBlock = getPlatformBlock();
                level.setBlock(pos, platformBlock, 3);
            }
        }
    }
    
    /**
     * 创建观景台栏杆
     */
    private void createRailings(BlockPos centerPos, int size) {
        int radius = size / 2;
        BlockState railingBlock = getRailingBlock();
        
        // 创建四周栏杆
        for (int x = -radius; x <= radius; x++) {
            level.setBlock(centerPos.offset(x, 1, -radius), railingBlock, 3);
            level.setBlock(centerPos.offset(x, 1, radius), railingBlock, 3);
        }
        
        for (int z = -radius + 1; z < radius; z++) {
            level.setBlock(centerPos.offset(-radius, 1, z), railingBlock, 3);
            level.setBlock(centerPos.offset(radius, 1, z), railingBlock, 3);
        }
        
        // 创建入口（移除部分栏杆）
        level.setBlock(centerPos.offset(0, 1, -radius), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(centerPos.offset(1, 1, -radius), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(centerPos.offset(-1, 1, -radius), Blocks.AIR.defaultBlockState(), 3);
    }
    
    /**
     * 创建观景台座椅
     */
    private void createBenches(BlockPos centerPos) {
        BlockState benchBlock = getBenchBlock();
        
        // 四个角落的座椅
        level.setBlock(centerPos.offset(-2, 1, -2), benchBlock, 3);
        level.setBlock(centerPos.offset(2, 1, -2), benchBlock, 3);
        level.setBlock(centerPos.offset(-2, 1, 2), benchBlock, 3);
        level.setBlock(centerPos.offset(2, 1, 2), benchBlock, 3);
        
        // 中心区域的座椅
        if (random.nextBoolean()) {
            level.setBlock(centerPos.offset(-1, 1, 0), benchBlock, 3);
            level.setBlock(centerPos.offset(1, 1, 0), benchBlock, 3);
        }
    }
    
    /**
     * 创建观景台望远镜
     */
    private void createTelescope(BlockPos centerPos) {
        if (random.nextDouble() < 0.3) { // 30%概率生成望远镜
            BlockPos telescopePos = centerPos.offset(0, 1, 0);
            
            // 望远镜底座
            BlockState baseBlock = getTelescopeBaseBlock();
            level.setBlock(telescopePos, baseBlock, 3);
            
            // 望远镜柱
            BlockState pillarBlock = getTelescopePillarBlock();
            for (int y = 2; y <= 3; y++) {
                level.setBlock(telescopePos.offset(0, y, 0), pillarBlock, 3);
            }
            
            // 望远镜顶部
            BlockState topBlock = getTelescopeTopBlock();
            level.setBlock(telescopePos.offset(0, 4, 0), topBlock, 3);
        }
    }
    
    /**
     * 创建观景台照明
     */
    private void createLighting(BlockPos centerPos) {
        BlockState lightBlock = getLightBlock();
        
        // 四个角落的照明
        level.setBlock(centerPos.offset(-3, 2, -3), lightBlock, 3);
        level.setBlock(centerPos.offset(3, 2, -3), lightBlock, 3);
        level.setBlock(centerPos.offset(-3, 2, 3), lightBlock, 3);
        level.setBlock(centerPos.offset(3, 2, 3), lightBlock, 3);
    }
    
    /**
     * 获取平台方块
     */
    private BlockState getPlatformBlock() {
        return biomeStrategy.getSuitableBlock(
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState()
        );
    }
    
    /**
     * 获取栏杆方块
     */
    private BlockState getRailingBlock() {
        return biomeStrategy.getSuitableBlock(
            Blocks.STONE_BRICK_WALL.defaultBlockState(),
            Blocks.SPRUCE_FENCE.defaultBlockState()
        );
    }
    
    /**
     * 获取座椅方块
     */
    private BlockState getBenchBlock() {
        return biomeStrategy.getSuitableBlock(
            Blocks.SPRUCE_STAIRS.defaultBlockState(),
            Blocks.OAK_STAIRS.defaultBlockState()
        );
    }
    
    /**
     * 获取望远镜底座方块
     */
    private BlockState getTelescopeBaseBlock() {
        return Blocks.STONE_BRICKS.defaultBlockState();
    }
    
    /**
     * 获取望远镜柱子方块
     */
    private BlockState getTelescopePillarBlock() {
        return Blocks.IRON_BARS.defaultBlockState();
    }
    
    /**
     * 获取望远镜顶部方块
     */
    private BlockState getTelescopeTopBlock() {
        return Blocks.OBSERVER.defaultBlockState();
    }
    
    /**
     * 获取照明方块
     */
    private BlockState getLightBlock() {
        return biomeStrategy.getSuitableBlock(
            Blocks.TORCH.defaultBlockState(),
            Blocks.LANTERN.defaultBlockState()
        );
    }
    
    @Override
    public boolean canGenerateAt(Road road, BlockPos pos) {
        // 观景台需要较好的视野和相对平坦的地形
        return hasGoodView(pos) && isRelativelyFlat(pos, 5);
    }
    
    /**
     * 检查是否有良好视野
     */
    private boolean hasGoodView(BlockPos pos) {
        // 检查周围是否有开阔区域
        int openAreaCount = 0;
        
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (Math.abs(x) + Math.abs(z) > 8) continue;
                
                BlockPos checkPos = pos.offset(x, 0, z);
                if (isFlatArea(checkPos, 3)) {
                    openAreaCount++;
                }
            }
        }
        
        return openAreaCount >= 15;
    }
    
    /**
     * 检查是否为相对平坦区域
     */
    private boolean isRelativelyFlat(BlockPos centerPos, int radius) {
        int centerHeight = centerPos.getY();
        int maxHeightDiff = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = centerPos.offset(x, 0, z);
                int checkHeight = getTerrainHeight(checkPos.getX(), checkPos.getZ());
                int heightDiff = Math.abs(checkHeight - centerHeight);
                maxHeightDiff = Math.max(maxHeightDiff, heightDiff);
            }
        }
        
        return maxHeightDiff <= 3;
    }
}
