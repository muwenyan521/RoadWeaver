package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * 路牌装饰
 */
public class StreetSignDecoration extends Decoration {
    private final Vec3i orthogonal;
    
    public StreetSignDecoration(BlockPos placePos, Vec3i orthogonal, WorldGenLevel world) {
        super(placePos, world);
        this.orthogonal = orthogonal;
    }
    
    @Override
    public void place() {
        if (!placeAllowed()) {
            return;
        }
        
        BlockPos basePos = getPos();
        WorldGenLevel world = getWorld();
        
        // 确定路牌朝向（面向道路）
        Direction signDirection = getSignDirection();
        
        // 创建路牌支柱
        placeSignPost(world, basePos);
        
        // 创建路牌
        placeSign(world, basePos, signDirection);
        
        // 添加装饰性底座
        placeDecorativeBase(world, basePos);
    }
    
    private Direction getSignDirection() {
        // 路牌应该面向道路
        if (orthogonal.getX() != 0) {
            return orthogonal.getX() > 0 ? Direction.WEST : Direction.EAST;
        } else {
            return orthogonal.getZ() > 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }
    
    private void placeSignPost(WorldGenLevel world, BlockPos basePos) {
        BlockState fencePost = Blocks.OAK_FENCE.defaultBlockState();
        
        // 创建2格高的支柱
        for (int y = 0; y < 2; y++) {
            BlockPos postPos = basePos.above(y);
            world.setBlock(postPos, fencePost, 3);
        }
    }
    
    private void placeSign(WorldGenLevel world, BlockPos basePos, Direction direction) {
        BlockPos signPos = basePos.above(2);
        
        // 使用橡木墙告示牌
        BlockState signBlock = Blocks.OAK_WALL_SIGN.defaultBlockState()
                .setValue(WallSignBlock.FACING, direction);
        
        world.setBlock(signPos, signBlock, 3);
        
        // 在告示牌上方添加屋顶装饰
        BlockPos roofPos = signPos.above();
        world.setBlock(roofPos, Blocks.OAK_SLAB.defaultBlockState(), 3);
    }
    
    private void placeDecorativeBase(WorldGenLevel world, BlockPos basePos) {
        BlockState stoneSlab = Blocks.STONE_SLAB.defaultBlockState();
        BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        
        // 在支柱周围创建装饰性底座
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // 跳过支柱位置
                
                BlockPos baseBlockPos = basePos.offset(x, -1, z);
                world.setBlock(baseBlockPos, cobblestone, 3);
                
                // 在底座上方放置台阶
                BlockPos slabPos = baseBlockPos.above();
                world.setBlock(slabPos, stoneSlab, 3);
            }
        }
    }
}
