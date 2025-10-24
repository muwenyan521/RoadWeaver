package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.shiroha233.roadweaver.features.decoration.config.DecorationType;
import net.shiroha233.roadweaver.features.roadlogic.Road;

import java.util.Random;

/**
 * 邮箱装饰实现
 * 在道路旁生成邮箱，作为居民区道路的常见装饰
 */
public class MailboxDecoration implements Decoration {
    
    @Override
    public DecorationType getType() {
        return DecorationType.MAILBOX;
    }
    
    @Override
    public boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 邮箱需要靠近居民区道路，并且有足够的空间
        return isCloseToResidentialRoad(pos, road) && 
               checkFlatArea(level, pos, 1, 1) &&
               !isTooCloseToOtherMailbox(level, pos, 8);
    }
    
    @Override
    public void generate(LevelAccessor level, BlockPos pos, Road road, Random random) {
        // 选择邮箱风格
        MailboxStyle mailboxStyle = MailboxStyle.values()[random.nextInt(MailboxStyle.values().length)];
        
        // 根据风格生成邮箱
        switch (mailboxStyle) {
            case STANDARD:
                generateStandardMailbox(level, pos, random);
                break;
            case MODERN:
                generateModernMailbox(level, pos, random);
                break;
            case RUSTIC:
                generateRusticMailbox(level, pos, random);
                break;
        }
        
        // 生成邮箱周围的装饰
        generateMailboxSurroundings(level, pos, random);
    }
    
    /**
     * 邮箱风格枚举
     */
    private enum MailboxStyle {
        STANDARD, // 标准风格
        MODERN,   // 现代风格
        RUSTIC    // 乡村风格
    }
    
    /**
     * 生成标准风格邮箱
     */
    private void generateStandardMailbox(LevelAccessor level, BlockPos pos, Random random) {
        // 生成邮箱支撑柱
        BlockPos postPos = pos;
        level.setBlock(postPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
        
        // 生成邮箱主体
        BlockPos mailboxPos = postPos.above();
        if (random.nextFloat() < 0.7f) {
            level.setBlock(mailboxPos, Blocks.CHEST.defaultBlockState(), 3);
        } else {
            level.setBlock(mailboxPos, Blocks.BARREL.defaultBlockState(), 3);
        }
        
        // 生成邮箱标志（旗帜）
        if (random.nextFloat() < 0.5f) {
            BlockPos flagPos = mailboxPos.above();
            level.setBlock(flagPos, Blocks.WHITE_BANNER.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成现代风格邮箱
     */
    private void generateModernMailbox(LevelAccessor level, BlockPos pos, Random random) {
        // 生成现代风格支撑柱
        BlockPos postPos = pos;
        if (random.nextBoolean()) {
            level.setBlock(postPos, Blocks.QUARTZ_PILLAR.defaultBlockState(), 3);
        } else {
            level.setBlock(postPos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
        }
        
        // 生成现代风格邮箱主体
        BlockPos mailboxPos = postPos.above();
        if (random.nextFloat() < 0.6f) {
            level.setBlock(mailboxPos, Blocks.IRON_BLOCK.defaultBlockState(), 3);
        } else {
            level.setBlock(mailboxPos, Blocks.WHITE_CONCRETE.defaultBlockState(), 3);
        }
        
        // 生成现代风格装饰
        if (random.nextFloat() < 0.4f) {
            BlockPos decorPos = mailboxPos.above();
            level.setBlock(decorPos, Blocks.IRON_BARS.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成乡村风格邮箱
     */
    private void generateRusticMailbox(LevelAccessor level, BlockPos pos, Random random) {
        // 生成乡村风格支撑柱
        BlockPos postPos = pos;
        if (random.nextFloat() < 0.7f) {
            level.setBlock(postPos, Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
        } else {
            level.setBlock(postPos, Blocks.OAK_FENCE.defaultBlockState(), 3);
        }
        
        // 生成乡村风格邮箱主体
        BlockPos mailboxPos = postPos.above();
        if (random.nextFloat() < 0.8f) {
            level.setBlock(mailboxPos, Blocks.BARREL.defaultBlockState(), 3);
        } else {
            level.setBlock(mailboxPos, Blocks.CHEST.defaultBlockState(), 3);
        }
        
        // 生成乡村风格装饰（藤蔓）
        if (random.nextFloat() < 0.3f) {
            BlockPos vinePos = postPos.offset(1, 0, 0);
            level.setBlock(vinePos, Blocks.VINE.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成邮箱周围的装饰
     */
    private void generateMailboxSurroundings(LevelAccessor level, BlockPos pos, Random random) {
        // 50%概率在邮箱旁生成小花坛
        if (random.nextBoolean()) {
            generateMailboxFlowerBed(level, pos, random);
        }
        
        // 30%概率在邮箱旁生成小路径
        if (random.nextFloat() < 0.3f) {
            generateMailboxPath(level, pos, random);
        }
        
        // 20%概率在邮箱旁生成小灯笼
        if (random.nextFloat() < 0.2f) {
            BlockPos lanternPos = pos.offset(1, 1, 0);
            level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState(), 3);
        }
    }
    
    /**
     * 生成邮箱花坛
     */
    private void generateMailboxFlowerBed(LevelAccessor level, BlockPos pos, Random random) {
        // 在邮箱旁生成小型花坛
        BlockPos flowerBedPos = pos.offset(-1, 0, 0);
        
        // 生成花坛基础（泥土）
        for (int x = -1; x <= 0; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos dirtPos = flowerBedPos.offset(x, 0, z);
                level.setBlock(dirtPos, Blocks.DIRT.defaultBlockState(), 3);
            }
        }
        
        // 生成花朵
        BlockPos[] flowerPositions = {
            flowerBedPos.offset(-1, 1, -1),
            flowerBedPos.offset(-1, 1, 0),
            flowerBedPos.offset(-1, 1, 1),
            flowerBedPos.offset(0, 1, -1),
            flowerBedPos.offset(0, 1, 1)
        };
        
        for (BlockPos flowerPos : flowerPositions) {
            if (random.nextFloat() < 0.7f) {
                // 随机选择花朵类型
                switch (random.nextInt(6)) {
                    case 0:
                        level.setBlock(flowerPos, Blocks.POPPY.defaultBlockState(), 3);
                        break;
                    case 1:
                        level.setBlock(flowerPos, Blocks.DANDELION.defaultBlockState(), 3);
                        break;
                    case 2:
                        level.setBlock(flowerPos, Blocks.BLUE_ORCHID.defaultBlockState(), 3);
                        break;
                    case 3:
                        level.setBlock(flowerPos, Blocks.ALLIUM.defaultBlockState(), 3);
                        break;
                    case 4:
                        level.setBlock(flowerPos, Blocks.AZURE_BLUET.defaultBlockState(), 3);
                        break;
                    case 5:
                        level.setBlock(flowerPos, Blocks.OXEYE_DAISY.defaultBlockState(), 3);
                        break;
                }
            }
        }
    }
    
    /**
     * 生成邮箱路径
     */
    private void generateMailboxPath(LevelAccessor level, BlockPos pos, Random random) {
        // 从邮箱到道路生成小路径
        int pathLength = 3 + random.nextInt(3); // 3-5格长
        
        for (int i = 1; i <= pathLength; i++) {
            BlockPos pathPos = pos.offset(0, 0, i);
            if (random.nextFloat() < 0.8f) {
                level.setBlock(pathPos, Blocks.GRAVEL.defaultBlockState(), 3);
            } else {
                level.setBlock(pathPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 检查是否靠近居民区道路
     */
    private boolean isCloseToResidentialRoad(BlockPos pos, Road road) {
        // 检查位置是否在居民区道路附近（距离道路中心不超过3格）
        return road.getDistanceToRoad(pos) <= 3 && 
               road.getRoadLevel().isResidential();
    }
    
    /**
     * 检查平坦区域
     */
    private boolean checkFlatArea(LevelAccessor level, BlockPos pos, int width, int depth) {
        int baseY = pos.getY();
        
        for (int x = -width/2; x <= width/2; x++) {
            for (int z = -depth/2; z <= depth/2; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                int terrainY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, checkPos.getX(), checkPos.getZ());
                
                if (Math.abs(terrainY - baseY) > 1) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 检查是否太靠近其他邮箱
     */
    private boolean isTooCloseToOtherMailbox(LevelAccessor level, BlockPos pos, int minDistance) {
        // 这里可以集成现有的装饰检测系统
        // 暂时返回false，假设没有冲突
        return false;
    }
}
