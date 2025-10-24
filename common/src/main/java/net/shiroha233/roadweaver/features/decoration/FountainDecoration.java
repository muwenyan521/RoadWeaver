package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 喷泉装饰
 */
public class FountainDecoration extends Decoration {
    private final Vec3i orthogonal;
    
    public FountainDecoration(BlockPos placePos, Vec3i orthogonal, WorldGenLevel world) {
        super(placePos, world);
        this.orthogonal = orthogonal;
    }
    
    @Override
    public void place() {
        if (!placeAllowed()) {
            return;
        }
        
        BlockPos centerPos = getPos();
        WorldGenLevel world = getWorld();
        
        // 创建喷泉基础
        placeFountainBase(world, centerPos);
        
        // 创建喷泉中心柱
        placeFountainColumn(world, centerPos);
        
        // 创建喷泉水池
        placeFountainPool(world, centerPos);
    }
    
    private void placeFountainBase(WorldGenLevel world, BlockPos centerPos) {
        // 基础平台 - 使用石砖
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState polishedAndesite = Blocks.POLISHED_ANDESITE.defaultBlockState();
        
        // 创建3x3的基础平台
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = centerPos.offset(x, -1, z);
                world.setBlock(pos, stoneBricks, 3);
            }
        }
        
        // 创建装饰性边缘
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    BlockPos pos = centerPos.offset(x, -1, z);
                    world.setBlock(pos, polishedAndesite, 3);
                }
            }
        }
    }
    
    private void placeFountainColumn(WorldGenLevel world, BlockPos centerPos) {
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        
        // 创建中心柱
        for (int y = 0; y <= 2; y++) {
            BlockPos pos = centerPos.above(y);
            world.setBlock(pos, stoneBricks, 3);
        }
        
        // 在柱顶放置水源
        BlockPos topPos = centerPos.above(3);
        world.setBlock(topPos, water, 3);
        
        // 在柱顶周围放置装饰性方块
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    BlockPos pos = topPos.offset(x, 0, z);
                    world.setBlock(pos, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3);
                }
            }
        }
    }
    
    private void placeFountainPool(WorldGenLevel world, BlockPos centerPos) {
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState stoneBrickSlab = Blocks.STONE_BRICK_SLAB.defaultBlockState();
        
        // 创建水池（5x5）
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = centerPos.offset(x, 0, z);
                
                // 水池边缘使用石砖台阶
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    world.setBlock(pos, stoneBrickSlab, 3);
                } else {
                    // 水池内部使用水
                    world.setBlock(pos, water, 3);
                }
            }
        }
        
        // 在水池底部放置装饰性方块
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = centerPos.offset(x, -1, z);
                if (x == 0 && z == 0) {
                    // 中心位置已经放置了基础，跳过
                    continue;
                }
                world.setBlock(pos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
            }
        }
    }
}
