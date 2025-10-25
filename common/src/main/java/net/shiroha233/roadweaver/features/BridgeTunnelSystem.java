package net.shiroha233.roadweaver.features;

import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.shiroha233.roadweaver.helpers.Records;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 桥梁隧道系统
 * 负责生成河流高架桥、峡谷隧道和自然遗迹断桥
 */
public class BridgeTunnelSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");
    
    // 桥梁类型枚举
    public enum BridgeType {
        RIVER_BRIDGE,      // 河流桥梁
        VIADUCT,           // 高架桥
        RUINED_BRIDGE,     // 遗迹断桥
        CANYON_BRIDGE      // 峡谷桥梁
    }
    
    // 隧道类型枚举
    public enum TunnelType {
        CANYON_TUNNEL,     // 峡谷隧道
        MOUNTAIN_TUNNEL,   // 山体隧道
        UNDERWATER_TUNNEL  // 水下隧道
    }
    
    // 桥梁配置
    public static class BridgeConfig {
        public final BridgeType type;
        public final int minLength;
        public final int maxLength;
        public final int minHeight;
        public final int maxHeight;
        public final List<BlockState> materials;
        public final boolean supportsNavigation;
        
        public BridgeConfig(BridgeType type, int minLength, int maxLength, int minHeight, int maxHeight, 
                           List<BlockState> materials, boolean supportsNavigation) {
            this.type = type;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.materials = materials;
            this.supportsNavigation = supportsNavigation;
        }
    }
    
    // 隧道配置
    public static class TunnelConfig {
        public final TunnelType type;
        public final int minLength;
        public final int maxLength;
        public final int width;
        public final int height;
        public final List<BlockState> materials;
        
        public TunnelConfig(TunnelType type, int minLength, int maxLength, int width, int height, 
                           List<BlockState> materials) {
            this.type = type;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.width = width;
            this.height = height;
            this.materials = materials;
        }
    }
    
    // 桥梁生成结果
    public static class BridgeGenerationResult {
        public final boolean success;
        public final List<BlockPos> bridgePositions;
        public final BridgeType bridgeType;
        public final int bridgeLength;
        public final int bridgeHeight;
        
        public BridgeGenerationResult(boolean success, List<BlockPos> bridgePositions, 
                                     BridgeType bridgeType, int bridgeLength, int bridgeHeight) {
            this.success = success;
            this.bridgePositions = bridgePositions;
            this.bridgeType = bridgeType;
            this.bridgeLength = bridgeLength;
            this.bridgeHeight = bridgeHeight;
        }
    }
    
    // 隧道生成结果
    public static class TunnelGenerationResult {
        public final boolean success;
        public final List<BlockPos> tunnelPositions;
        public final TunnelType tunnelType;
        public final int tunnelLength;
        
        public TunnelGenerationResult(boolean success, List<BlockPos> tunnelPositions, 
                                     TunnelType tunnelType, int tunnelLength) {
            this.success = success;
            this.tunnelPositions = tunnelPositions;
            this.tunnelType = tunnelType;
            this.tunnelLength = tunnelLength;
        }
    }
    
    // 桥梁材料配置
    private static final Map<BridgeType, List<BlockState>> BRIDGE_MATERIALS = Map.of(
        BridgeType.RIVER_BRIDGE, List.of(
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
        ),
        BridgeType.VIADUCT, List.of(
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.SMOOTH_STONE.defaultBlockState(),
            Blocks.IRON_BARS.defaultBlockState()
        ),
        BridgeType.RUINED_BRIDGE, List.of(
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
            Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.AIR.defaultBlockState() // 用于断桥的空隙
        ),
        BridgeType.CANYON_BRIDGE, List.of(
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState()
        )
    );
    
    // 隧道材料配置
    private static final Map<TunnelType, List<BlockState>> TUNNEL_MATERIALS = Map.of(
        TunnelType.CANYON_TUNNEL, List.of(
            Blocks.STONE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.STONE_BRICKS.defaultBlockState()
        ),
        TunnelType.MOUNTAIN_TUNNEL, List.of(
            Blocks.STONE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.COBBLED_DEEPSLATE.defaultBlockState()
        ),
        TunnelType.UNDERWATER_TUNNEL, List.of(
            Blocks.PRISMARINE.defaultBlockState(),
            Blocks.DARK_PRISMARINE.defaultBlockState(),
            Blocks.SEA_LANTERN.defaultBlockState()
        )
    );
    
    public BridgeTunnelSystem() {
        LOGGER.debug("BridgeTunnelSystem initialized");
    }
    
    /**
     * 检测是否需要桥梁
     */
    public Optional<BridgeConfig> detectBridgeNeed(LevelAccessor level, BlockPos startPos, BlockPos endPos, 
                                                   int roadType, RandomSource random) {
        IModConfig config = ConfigProvider.get();
        
        if (!config.enableBridgeGeneration()) {
            return Optional.empty();
        }
        
        // 检测河流
        BridgeConfig riverBridge = detectRiverBridge(level, startPos, endPos, roadType, random);
        if (riverBridge != null) {
            return Optional.of(riverBridge);
        }
        
        // 检测峡谷
        BridgeConfig canyonBridge = detectCanyonBridge(level, startPos, endPos, roadType, random);
        if (canyonBridge != null) {
            return Optional.of(canyonBridge);
        }
        
        // 检测高架桥需求
        BridgeConfig viaduct = detectViaductNeed(level, startPos, endPos, roadType, random);
        if (viaduct != null) {
            return Optional.of(viaduct);
        }
        
        return Optional.empty();
    }
    
    /**
     * 检测是否需要隧道
     */
    public Optional<TunnelConfig> detectTunnelNeed(LevelAccessor level, BlockPos startPos, BlockPos endPos, 
                                                   int roadType, RandomSource random) {
        IModConfig config = ConfigProvider.get();
        
        // 检测峡谷隧道
        TunnelConfig canyonTunnel = detectCanyonTunnel(level, startPos, endPos, roadType, random);
        if (canyonTunnel != null) {
            return Optional.of(canyonTunnel);
        }
        
        // 检测山体隧道
        TunnelConfig mountainTunnel = detectMountainTunnel(level, startPos, endPos, roadType, random);
        if (mountainTunnel != null) {
            return Optional.of(mountainTunnel);
        }
        
        return Optional.empty();
    }
    
    /**
     * 生成桥梁
     */
    public BridgeGenerationResult generateBridge(LevelAccessor level, BridgeConfig bridgeConfig, 
                                                BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> bridgePositions = new ArrayList<>();
        
        switch (bridgeConfig.type) {
            case RIVER_BRIDGE:
                bridgePositions = generateRiverBridge(level, bridgeConfig, startPos, endPos, random);
                break;
            case VIADUCT:
                bridgePositions = generateViaduct(level, bridgeConfig, startPos, endPos, random);
                break;
            case RUINED_BRIDGE:
                bridgePositions = generateRuinedBridge(level, bridgeConfig, startPos, endPos, random);
                break;
            case CANYON_BRIDGE:
                bridgePositions = generateCanyonBridge(level, bridgeConfig, startPos, endPos, random);
                break;
        }
        
        boolean success = !bridgePositions.isEmpty();
        int bridgeLength = bridgePositions.size();
        int bridgeHeight = calculateAverageBridgeHeight(level, bridgePositions);
        
        LOGGER.debug("Generated {} bridge with {} segments, height: {}", 
                    bridgeConfig.type, bridgeLength, bridgeHeight);
        
        return new BridgeGenerationResult(success, bridgePositions, bridgeConfig.type, bridgeLength, bridgeHeight);
    }
    
    /**
     * 生成隧道
     */
    public TunnelGenerationResult generateTunnel(LevelAccessor level, TunnelConfig tunnelConfig, 
                                                BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> tunnelPositions = new ArrayList<>();
        
        switch (tunnelConfig.type) {
            case CANYON_TUNNEL:
                tunnelPositions = generateCanyonTunnel(level, tunnelConfig, startPos, endPos, random);
                break;
            case MOUNTAIN_TUNNEL:
                tunnelPositions = generateMountainTunnel(level, tunnelConfig, startPos, endPos, random);
                break;
            case UNDERWATER_TUNNEL:
                tunnelPositions = generateUnderwaterTunnel(level, tunnelConfig, startPos, endPos, random);
                break;
        }
        
        boolean success = !tunnelPositions.isEmpty();
        int tunnelLength = tunnelPositions.size();
        
        LOGGER.debug("Generated {} tunnel with {} segments", tunnelConfig.type, tunnelLength);
        
        return new TunnelGenerationResult(success, tunnelPositions, tunnelConfig.type, tunnelLength);
    }
    
    // 私有方法实现
    
    private BridgeConfig detectRiverBridge(LevelAccessor level, BlockPos startPos, BlockPos endPos, 
                                          int roadType, RandomSource random) {
        // 检测路径上的水体
        List<BlockPos> waterPositions = detectWaterObstacles(level, startPos, endPos);
        
        if (waterPositions.size() > 3) { // 连续水体超过3格
            int bridgeLength = waterPositions.size() + 2; // 桥梁比水体稍长
            int bridgeHeight = calculateBridgeHeightOverWater(level, waterPositions);
            
            return new BridgeConfig(BridgeType.RIVER_BRIDGE, bridgeLength, bridgeLength + 5, 
                                  bridgeHeight, bridgeHeight + 2, BRIDGE_MATERIALS.get(BridgeType.RIVER_BRIDGE), true);
        }
        
        return null;
    }
    
    private BridgeConfig detectCanyonBridge(LevelAccessor level, BlockPos startPos, BlockPos endPos, 
                                           int roadType, RandomSource random) {
        // 检测峡谷地形
        List<BlockPos> ravinePositions = detectRavineObstacles(level, startPos, endPos);
        
        if (ravinePositions.size() > 5) { // 峡谷宽度超过5格
            int bridgeLength = ravinePositions.size();
            int bridgeHeight = calculateBridgeHeightOverCanyon(level, ravinePositions);
            
            return new BridgeConfig(BridgeType.CANYON_BRIDGE, bridgeLength, bridgeLength + 3, 
                                  bridgeHeight, bridgeHeight + 3, BRIDGE_MATERIALS.get(BridgeType.CANYON_BRIDGE), false);
        }
        
        return null;
    }
    
    private BridgeConfig detectViaductNeed(LevelAccessor level, BlockPos startPos, BlockPos endPos, 
                                          int roadType, RandomSource random) {
        // 检测需要高架桥的地形（如沼泽、复杂地形）
        if (isComplexTerrain(level, startPos, endPos)) {
            int bridgeLength = calculateDistance(startPos, endPos);
            int bridgeHeight = 4; // 标准高架桥高度
            
            return new BridgeConfig(BridgeType.VIADUCT, bridgeLength, bridgeLength, 
                                  bridgeHeight, bridgeHeight, BRIDGE_MATERIALS.get(BridgeType.VIADUCT), false);
        }
        
        return null;
    }
    
    private TunnelConfig detectCanyonTunnel(LevelAccessor level, BlockPos startPos, BlockPos endPos, 
                                           int roadType, RandomSource random) {
        // 检测适合隧道穿行的峡谷
        if (isSuitableForCanyonTunnel(level, startPos, endPos)) {
            int tunnelLength = calculateDistance(startPos, endPos);
            
            return new TunnelConfig(TunnelType.CANYON_TUNNEL, tunnelLength, tunnelLength, 
                                  3, 3, TUNNEL_MATERIALS.get(TunnelType.CANYON_TUNNEL));
        }
        
        return null;
    }
    
    private TunnelConfig detectMountainTunnel(LevelAccessor level, BlockPos startPos, BlockPos endPos, 
                                             int roadType, RandomSource random) {
        // 检测需要穿山的路径
        if (isMountainObstacle(level, startPos, endPos)) {
            int tunnelLength = calculateDistance(startPos, endPos);
            
            return new TunnelConfig(TunnelType.MOUNTAIN_TUNNEL, tunnelLength, tunnelLength, 
                                  3, 3, TUNNEL_MATERIALS.get(TunnelType.MOUNTAIN_TUNNEL));
        }
        
        return null;
    }
    
    private List<BlockPos> generateRiverBridge(LevelAccessor level, BridgeConfig config, 
                                              BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> bridgePositions = new ArrayList<>();
        int bridgeLength = random.nextInt(config.minLength, config.maxLength + 1);
        int bridgeHeight = random.nextInt(config.minHeight, config.maxHeight + 1);
        
        // 计算桥梁方向
        int dx = endPos.getX() - startPos.getX();
        int dz = endPos.getZ() - startPos.getZ();
        
        for (int i = 0; i < bridgeLength; i++) {
            double progress = (double) i / (bridgeLength - 1);
            int x = startPos.getX() + (int)(dx * progress);
            int z = startPos.getZ() + (int)(dz * progress);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + bridgeHeight;
            
            BlockPos bridgePos = new BlockPos(x, y, z);
            bridgePositions.add(bridgePos);
            
            // 放置桥梁支撑结构
            placeBridgeSupport(level, bridgePos, config.materials, random);
        }
        
        return bridgePositions;
    }
    
    private List<BlockPos> generateViaduct(LevelAccessor level, BridgeConfig config, 
                                          BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> bridgePositions = new ArrayList<>();
        int bridgeHeight = config.minHeight;
        
        // 计算高架桥路径
        int steps = calculateDistance(startPos, endPos);
        int dx = (endPos.getX() - startPos.getX()) / steps;
        int dz = (endPos.getZ() - startPos.getZ()) / steps;
        
        for (int i = 0; i <= steps; i++) {
            int x = startPos.getX() + dx * i;
            int z = startPos.getZ() + dz * i;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + bridgeHeight;
            
            BlockPos bridgePos = new BlockPos(x, y, z);
            bridgePositions.add(bridgePos);
            
            // 放置高架桥支柱
            placeViaductPillar(level, bridgePos, config.materials, random);
        }
        
        return bridgePositions;
    }
    
    private List<BlockPos> generateRuinedBridge(LevelAccessor level, BridgeConfig config, 
                                               BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> bridgePositions = new ArrayList<>();
        int bridgeLength = random.nextInt(config.minLength, config.maxLength + 1);
        
        // 计算桥梁方向
        int dx = endPos.getX() - startPos.getX();
        int dz = endPos.getZ() - startPos.getZ();
        
        for (int i = 0; i < bridgeLength; i++) {
            double progress = (double) i / (bridgeLength - 1);
            int x = startPos.getX() + (int)(dx * progress);
            int z = startPos.getZ() + (int)(dz * progress);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            
            BlockPos bridgePos = new BlockPos(x, y, z);
            
            // 随机决定是否放置断桥段（创建遗迹效果）
            if (random.nextDouble() > 0.3) { // 70%概率放置桥梁段
                bridgePositions.add(bridgePos);
                placeRuinedBridgeSegment(level, bridgePos, config.materials, random);
            }
        }
        
        return bridgePositions;
    }
    
    private List<BlockPos> generateCanyonBridge(LevelAccessor level, BridgeConfig config, 
                                               BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> bridgePositions = new ArrayList<>();
        int bridgeLength = random.nextInt(config.minLength, config.maxLength + 1);
        int bridgeHeight = random.nextInt(config.minHeight, config.maxHeight + 1);
        
        // 计算桥梁方向
        int dx = endPos.getX() - startPos.getX();
        int dz = endPos.getZ() - startPos.getZ();
        
        for (int i = 0; i < bridgeLength; i++) {
            double progress = (double) i / (bridgeLength - 1);
            int x = startPos.getX() + (int)(dx * progress);
            int z = startPos.getZ() + (int)(dz * progress);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + bridgeHeight;
            
            BlockPos bridgePos = new BlockPos(x, y, z);
            bridgePositions.add(bridgePos);
            
            // 放置峡谷桥梁结构
            placeCanyonBridgeSegment(level, bridgePos, config.materials, random);
        }
        
        return bridgePositions;
    }
    
    // 隧道生成方法
    private List<BlockPos> generateCanyonTunnel(LevelAccessor level, TunnelConfig config, 
                                               BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> tunnelPositions = new ArrayList<>();
        int tunnelLength = random.nextInt(config.minLength, config.maxLength + 1);
        
        // 计算隧道方向
        int dx = endPos.getX() - startPos.getX();
        int dz = endPos.getZ() - startPos.getZ();
        
        for (int i = 0; i < tunnelLength; i++) {
            double progress = (double) i / (tunnelLength - 1);
            int x = startPos.getX() + (int)(dx * progress);
            int z = startPos.getZ() + (int)(dz * progress);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            
            BlockPos tunnelPos = new BlockPos(x, y, z);
            tunnelPositions.add(tunnelPos);
            
            // 挖掘隧道
            excavateTunnel(level, tunnelPos, config.width, config.height, config.materials, random);
        }
        
        return tunnelPositions;
    }
    
    private List<BlockPos> generateMountainTunnel(LevelAccessor level, TunnelConfig config, 
                                                 BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> tunnelPositions = new ArrayList<>();
        int tunnelLength = random.nextInt(config.minLength, config.maxLength + 1);
        
        // 计算隧道方向
        int dx = endPos.getX() - startPos.getX();
        int dz = endPos.getZ() - startPos.getZ();
        
        for (int i = 0; i < tunnelLength; i++) {
            double progress = (double) i / (tunnelLength - 1);
            int x = startPos.getX() + (int)(dx * progress);
            int z = startPos.getZ() + (int)(dz * progress);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 2; // 山体隧道在地下
            
            BlockPos tunnelPos = new BlockPos(x, y, z);
            tunnelPositions.add(tunnelPos);
            
            // 挖掘山体隧道
            excavateTunnel(level, tunnelPos, config.width, config.height, config.materials, random);
        }
        
        return tunnelPositions;
    }
    
    private List<BlockPos> generateUnderwaterTunnel(LevelAccessor level, TunnelConfig config, 
                                                   BlockPos startPos, BlockPos endPos, RandomSource random) {
        List<BlockPos> tunnelPositions = new ArrayList<>();
        int tunnelLength = random.nextInt(config.minLength, config.maxLength + 1);
        
        // 计算隧道方向
        int dx = endPos.getX() - startPos.getX();
        int dz = endPos.getZ() - startPos.getZ();
        
        for (int i = 0; i < tunnelLength; i++) {
            double progress = (double) i / (tunnelLength - 1);
            int x = startPos.getX() + (int)(dx * progress);
            int z = startPos.getZ() + (int)(dz * progress);
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) + 1; // 水下隧道在海床之上
            
            BlockPos tunnelPos = new BlockPos(x, y, z);
            tunnelPositions.add(tunnelPos);
            
            // 建造水下隧道
            buildUnderwaterTunnel(level, tunnelPos, config.width, config.height, config.materials, random);
        }
        
        return tunnelPositions;
    }
    
    // 辅助方法
    private void placeBridgeSupport(LevelAccessor level, BlockPos pos, List<BlockState> materials, RandomSource random) {
        BlockState material = materials.get(random.nextInt(materials.size()));
        
        // 放置桥梁路面
        level.setBlock(pos, material, 3);
        
        // 放置支撑柱
        for (int i = 1; i <= 3; i++) {
            BlockPos supportPos = pos.below(i);
            if (level.getBlockState(supportPos).isAir() || level.getBlockState(supportPos).getBlock() == Blocks.WATER) {
                level.setBlock(supportPos, material, 3);
            }
        }
    }
    
    private void placeViaductPillar(LevelAccessor level, BlockPos pos, List<BlockState> materials, RandomSource random) {
        BlockState material = materials.get(random.nextInt(materials.size()));
        
        // 放置高架桥路面
        level.setBlock(pos, material, 3);
        
        // 放置高架桥支柱
        for (int i = 1; i <= 6; i++) {
            BlockPos pillarPos = pos.below(i);
            if (level.getBlockState(pillarPos).isAir() || level.isEmptyBlock(pillarPos)) {
                level.setBlock(pillarPos, material, 3);
            } else {
                break; // 到达地面
            }
        }
    }
    
    private void placeRuinedBridgeSegment(LevelAccessor level, BlockPos pos, List<BlockState> materials, RandomSource random) {
        BlockState material = materials.get(random.nextInt(materials.size()));
        
        // 随机决定是否放置断桥段
        if (material.getBlock() == Blocks.AIR) {
            return; // 不放置，创建断桥效果
        }
        
        // 放置破损的桥梁段
        level.setBlock(pos, material, 3);
        
        // 随机添加破损细节
        if (random.nextDouble() < 0.2) {
            level.setBlock(pos.above(), Blocks.VINE.defaultBlockState(), 3); // 藤蔓
        }
        if (random.nextDouble() < 0.1) {
            level.setBlock(pos.above(), Blocks.COBWEB.defaultBlockState(), 3); // 蜘蛛网
        }
    }
    
    private void placeCanyonBridgeSegment(LevelAccessor level, BlockPos pos, List<BlockState> materials, RandomSource random) {
        BlockState material = materials.get(random.nextInt(materials.size()));
        
        // 放置峡谷桥梁路面
        level.setBlock(pos, material, 3);
        
        // 放置峡谷桥梁支撑
        for (int i = 1; i <= 2; i++) {
            BlockPos supportPos = pos.below(i);
            if (level.getBlockState(supportPos).isAir()) {
                level.setBlock(supportPos, material, 3);
            }
        }
    }
    
    private void excavateTunnel(LevelAccessor level, BlockPos center, int width, int height, 
                               List<BlockState> materials, RandomSource random) {
        BlockState wallMaterial = materials.get(random.nextInt(materials.size()));
        
        // 挖掘隧道空间
        for (int dx = -width/2; dx <= width/2; dx++) {
            for (int dy = 0; dy < height; dy++) {
                for (int dz = -width/2; dz <= width/2; dz++) {
                    BlockPos tunnelPos = center.offset(dx, dy, dz);
                    
                    if (dx == -width/2 || dx == width/2 || dz == -width/2 || dz == width/2 || dy == height-1) {
                        // 放置隧道墙壁
                        level.setBlock(tunnelPos, wallMaterial, 3);
                    } else {
                        // 挖掘隧道内部
                        level.setBlock(tunnelPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // 放置隧道地面
        level.setBlock(center, materials.get(random.nextInt(materials.size())), 3);
    }
    
    private void buildUnderwaterTunnel(LevelAccessor level, BlockPos center, int width, int height, 
                                      List<BlockState> materials, RandomSource random) {
        BlockState wallMaterial = materials.get(random.nextInt(materials.size()));
        
        // 建造水下隧道结构
        for (int dx = -width/2; dx <= width/2; dx++) {
            for (int dy = 0; dy < height; dy++) {
                for (int dz = -width/2; dz <= width/2; dz++) {
                    BlockPos tunnelPos = center.offset(dx, dy, dz);
                    
                    if (dx == -width/2 || dx == width/2 || dz == -width/2 || dz == width/2 || dy == height-1) {
                        // 放置隧道墙壁
                        level.setBlock(tunnelPos, wallMaterial, 3);
                    } else {
                        // 清除内部空间
                        level.setBlock(tunnelPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // 放置隧道地面
        level.setBlock(center, materials.get(random.nextInt(materials.size())), 3);
    }
    
    // 地形检测辅助方法
    private List<BlockPos> detectWaterObstacles(LevelAccessor level, BlockPos startPos, BlockPos endPos) {
        List<BlockPos> waterPositions = new ArrayList<>();
        int steps = calculateDistance(startPos, endPos);
        int dx = (endPos.getX() - startPos.getX()) / steps;
        int dz = (endPos.getZ() - startPos.getZ()) / steps;
        
        for (int i = 0; i <= steps; i++) {
            int x = startPos.getX() + dx * i;
            int z = startPos.getZ() + dz * i;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockState blockState = level.getBlockState(checkPos);
            
            if (blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.LAVA) {
                waterPositions.add(checkPos);
            }
        }
        
        return waterPositions;
    }
    
    private List<BlockPos> detectRavineObstacles(LevelAccessor level, BlockPos startPos, BlockPos endPos) {
        List<BlockPos> ravinePositions = new ArrayList<>();
        int steps = calculateDistance(startPos, endPos);
        int dx = (endPos.getX() - startPos.getX()) / steps;
        int dz = (endPos.getZ() - startPos.getZ()) / steps;
        
        for (int i = 0; i <= steps; i++) {
            int x = startPos.getX() + dx * i;
            int z = startPos.getZ() + dz * i;
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            int groundY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            
            // 检测峡谷（地表与地面高度差较大）
            if (surfaceY - groundY > 10) {
                BlockPos ravinePos = new BlockPos(x, surfaceY, z);
                ravinePositions.add(ravinePos);
            }
        }
        
        return ravinePositions;
    }
    
    private int calculateBridgeHeightOverWater(LevelAccessor level, List<BlockPos> waterPositions) {
        int maxWaterDepth = 0;
        for (BlockPos pos : waterPositions) {
            int depth = calculateWaterDepth(level, pos);
            maxWaterDepth = Math.max(maxWaterDepth, depth);
        }
        return maxWaterDepth + 3; // 桥梁高度为最大水深+3格
    }
    
    private int calculateBridgeHeightOverCanyon(LevelAccessor level, List<BlockPos> ravinePositions) {
        int maxDepth = 0;
        for (BlockPos pos : ravinePositions) {
            int groundY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ());
            int depth = pos.getY() - groundY;
            maxDepth = Math.max(maxDepth, depth);
        }
        return Math.max(5, maxDepth / 2); // 峡谷桥梁高度为峡谷深度的一半，至少5格
    }
    
    private int calculateWaterDepth(LevelAccessor level, BlockPos pos) {
        int depth = 0;
        BlockPos checkPos = pos;
        
        while (level.getBlockState(checkPos).getBlock() == Blocks.WATER) {
            depth++;
            checkPos = checkPos.below();
        }
        
        return depth;
    }
    
    private boolean isComplexTerrain(LevelAccessor level, BlockPos startPos, BlockPos endPos) {
        int steps = calculateDistance(startPos, endPos);
        int dx = (endPos.getX() - startPos.getX()) / steps;
        int dz = (endPos.getZ() - startPos.getZ()) / steps;
        
        int heightVariation = 0;
        int prevY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, startPos.getX(), startPos.getZ());
        
        for (int i = 1; i <= steps; i++) {
            int x = startPos.getX() + dx * i;
            int z = startPos.getZ() + dz * i;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            
            heightVariation += Math.abs(y - prevY);
            prevY = y;
        }
        
        return heightVariation > steps * 2; // 如果高度变化超过步数的2倍，认为是复杂地形
    }
    
    private boolean isSuitableForCanyonTunnel(LevelAccessor level, BlockPos startPos, BlockPos endPos) {
        // 检测峡谷地形是否适合隧道
        List<BlockPos> ravinePositions = detectRavineObstacles(level, startPos, endPos);
        return ravinePositions.size() > 8; // 峡谷长度超过8格适合隧道
    }
    
    private boolean isMountainObstacle(LevelAccessor level, BlockPos startPos, BlockPos endPos) {
        int steps = calculateDistance(startPos, endPos);
        int dx = (endPos.getX() - startPos.getX()) / steps;
        int dz = (endPos.getZ() - startPos.getZ()) / steps;
        
        int mountainHeight = 0;
        
        for (int i = 0; i <= steps; i++) {
            int x = startPos.getX() + dx * i;
            int z = startPos.getZ() + dz * i;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            
            if (y > 100) { // 高度超过100格认为是山体
                mountainHeight++;
            }
        }
        
        return mountainHeight > steps / 2; // 超过一半路径是山体
    }
    
    private int calculateDistance(BlockPos startPos, BlockPos endPos) {
        return (int) Math.sqrt(
            Math.pow(endPos.getX() - startPos.getX(), 2) + 
            Math.pow(endPos.getZ() - startPos.getZ(), 2)
        );
    }
    
    private int calculateAverageBridgeHeight(LevelAccessor level, List<BlockPos> bridgePositions) {
        if (bridgePositions.isEmpty()) return 0;
        
        int totalHeight = 0;
        for (BlockPos pos : bridgePositions) {
            totalHeight += pos.getY();
        }
        
        return totalHeight / bridgePositions.size();
    }
}
