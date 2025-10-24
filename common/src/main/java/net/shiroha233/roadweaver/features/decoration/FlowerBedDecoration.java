package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

/**
 * 花坛装饰
 */
public class FlowerBedDecoration extends Decoration {
    private final Vec3i orthogonal;
    private static final RandomSource RANDOM = RandomSource.create();
    
    // 可用的花朵类型
    private static final List<BlockState> FLOWERS = Arrays.asList(
        Blocks.POPPY.defaultBlockState(),
        Blocks.DANDELION.defaultBlockState(),
        Blocks.BLUE_ORCHID.defaultBlockState(),
        Blocks.ALLIUM.defaultBlockState(),
        Blocks.AZURE_BLUET.defaultBlockState(),
        Blocks.RED_TULIP.defaultBlockState(),
        Blocks.ORANGE_TULIP.defaultBlockState(),
        Blocks.WHITE_TULIP.defaultBlockState(),
        Blocks.PINK_TULIP.defaultBlockState(),
        Blocks.OXEYE_DAISY.defaultBlockState(),
        Blocks.CORNFLOWER.defaultBlockState(),
        Blocks.LILY_OF_THE_VALLEY.defaultBlockState()
    );
    
    public FlowerBedDecoration(BlockPos placePos, Vec3i orthogonal, WorldGenLevel world) {
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
        
        // 创建花坛边界
        placeFlowerBedBorder(world, centerPos);
        
        // 创建花坛土壤
        placeFlowerBedSoil(world, centerPos);
        
        // 种植花朵
        plantFlowers(world, centerPos);
        
        // 添加装饰性元素
        addDecorativeElements(world, centerPos);
    }
    
    private void placeFlowerBedBorder(WorldGenLevel world, BlockPos centerPos) {
        BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        BlockState stoneBrickSlab = Blocks.STONE_BRICK_SLAB.defaultBlockState();
        
        // 创建3x3的花坛边界
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // 只在边缘放置边界
                if (Math.abs(x) == 1 || Math.abs(z) == 1) {
                    BlockPos borderPos = centerPos.offset(x, 0, z);
                    world.setBlock(borderPos, cobblestone, 3);
                    
                    // 在边界上方放置台阶作为装饰
                    BlockPos slabPos = borderPos.above();
                    world.setBlock(slabPos, stoneBrickSlab, 3);
                }
            }
        }
    }
    
    private void placeFlowerBedSoil(WorldGenLevel world, BlockPos centerPos) {
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        
        // 在花坛中心放置土壤
        BlockPos soilPos = centerPos;
        world.setBlock(soilPos, dirt, 3);
        
        // 在土壤周围放置草方块
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // 跳过中心位置
                
                BlockPos grassPos = centerPos.offset(x, -1, z);
                world.setBlock(grassPos, grass, 3);
            }
        }
    }
    
    private void plantFlowers(WorldGenLevel world, BlockPos centerPos) {
        // 在花坛中心种植花朵
        BlockState randomFlower = getRandomFlower();
        world.setBlock(centerPos.above(), randomFlower, 3);
        
        // 在花坛周围随机种植花朵
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // 跳过中心位置
                
                // 随机决定是否种植花朵
                if (RANDOM.nextDouble() < 0.6) {
                    BlockPos flowerPos = centerPos.offset(x, 0, z);
                    BlockState flower = getRandomFlower();
                    world.setBlock(flowerPos.above(), flower, 3);
                }
            }
        }
        
        // 在花坛角落种植高花（向日葵或玫瑰丛）
        if (RANDOM.nextDouble() < 0.3) {
            BlockState tallFlower = RANDOM.nextBoolean() 
                ? Blocks.SUNFLOWER.defaultBlockState() 
                : Blocks.ROSE_BUSH.defaultBlockState();
            
            // 随机选择一个角落
            int cornerX = RANDOM.nextBoolean() ? 1 : -1;
            int cornerZ = RANDOM.nextBoolean() ? 1 : -1;
            BlockPos cornerPos = centerPos.offset(cornerX, 0, cornerZ);
            
            world.setBlock(cornerPos.above(), tallFlower, 3);
        }
    }
    
    private void addDecorativeElements(WorldGenLevel world, BlockPos centerPos) {
        // 添加小径装饰
        if (RANDOM.nextDouble() < 0.4) {
            BlockState pathBlock = Blocks.GRAVEL.defaultBlockState();
            
            // 在花坛一侧创建小径
            int pathDirectionX = orthogonal.getX() != 0 ? orthogonal.getX() : 0;
            int pathDirectionZ = orthogonal.getZ() != 0 ? orthogonal.getZ() : 0;
            
            for (int i = 1; i <= 2; i++) {
                BlockPos pathPos = centerPos.offset(pathDirectionX * i, -1, pathDirectionZ * i);
                world.setBlock(pathPos, pathBlock, 3);
            }
        }
        
        // 添加照明装饰（火把）
        if (RANDOM.nextDouble() < 0.5) {
            BlockState torch = Blocks.TORCH.defaultBlockState();
            
            // 在花坛角落放置火把
            int torchX = RANDOM.nextBoolean() ? 1 : -1;
            int torchZ = RANDOM.nextBoolean() ? 1 : -1;
            BlockPos torchPos = centerPos.offset(torchX, 1, torchZ);
            
            world.setBlock(torchPos, torch, 3);
        }
    }
    
    private BlockState getRandomFlower() {
        return FLOWERS.get(RANDOM.nextInt(FLOWERS.size()));
    }
}
