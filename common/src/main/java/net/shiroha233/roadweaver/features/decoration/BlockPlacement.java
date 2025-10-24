package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 方块放置记录 - 表示一个方块的位置和状态
 */
public class BlockPlacement {
    private final BlockPos position;
    private final BlockState blockState;
    
    public BlockPlacement(BlockPos position, BlockState blockState) {
        this.position = position;
        this.blockState = blockState;
    }
    
    public BlockPos getPosition() {
        return position;
    }
    
    public BlockState getBlockState() {
        return blockState;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BlockPlacement that = (BlockPlacement) obj;
        return position.equals(that.position) && blockState.equals(that.blockState);
    }
    
    @Override
    public int hashCode() {
        return 31 * position.hashCode() + blockState.hashCode();
    }
    
    @Override
    public String toString() {
        return "BlockPlacement{position=" + position + ", blockState=" + blockState + "}";
    }
}
