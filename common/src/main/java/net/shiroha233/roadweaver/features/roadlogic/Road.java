package net.shiroha233.roadweaver.features.roadlogic;

import net.shiroha233.roadweaver.config.ConfigProvider;
import net.shiroha233.roadweaver.config.IModConfig;
import net.shiroha233.roadweaver.features.chunk.ChunkStateManager;
import net.shiroha233.roadweaver.features.chunk.ConflictResolutionStrategy;
import net.shiroha233.roadweaver.features.config.RoadFeatureConfig;
import net.shiroha233.roadweaver.features.config.RoadWidthManager;
import net.shiroha233.roadweaver.helpers.Records;
import net.shiroha233.roadweaver.persistence.WorldDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Road {

    private static final Logger LOGGER = LoggerFactory.getLogger("roadweaver");

    private final ServerLevel serverWorld;
    private final Records.StructureConnection structureConnection;
    private final RoadFeatureConfig context;

    public Road(ServerLevel serverWorld,
                Records.StructureConnection structureConnection,
                RoadFeatureConfig config) {
        this.serverWorld = serverWorld;
        this.structureConnection = structureConnection;
        this.context = config;
    }

    public void generateRoad(int maxSteps){
        // 更新连接状态为"生成中"
        updateConnectionStatus(Records.ConnectionStatus.GENERATING);

        RandomSource random = RandomSource.create();
        
        // 使用新的宽度配置系统
        IModConfig cfg = ConfigProvider.get();
        RoadWidthManager widthManager = RoadWidthManager.getInstance();
        
        int width;
        if (cfg.enableWidthConfiguration()) {
            // 根据结构连接确定道路等级
            int roadGrade = widthManager.determineRoadGrade(structureConnection);
            width = widthManager.getRandomWidthForGrade(roadGrade, random);
            LOGGER.debug("Using width configuration: grade={}, width={}", roadGrade, width);
        } else {
            // 回退到旧的宽度选择逻辑
            width = getRandomWidth(random, context.getWidths());
        }

        int type = allowedRoadTypes(random, cfg);
        if (type == -1) {
            updateConnectionStatus(Records.ConnectionStatus.FAILED);
            return;
        }
        List<BlockState> material = (type == 1)
                ? getRandomMaterials(random, context.getNaturalMaterials())
                : getRandomMaterials(random, context.getArtificialMaterials());

        BlockPos start = structureConnection.from();
        BlockPos end = structureConnection.to();

        int maxHeightDiff = cfg.maxHeightDifference();
        int maxStability = cfg.maxTerrainStability();
        boolean ignoreWater = false;

        List<Records.RoadSegmentPlacement> roadSegmentPlacementList = EnhancedRoadPathCalculator.calculateEnhancedAStarRoadPath(
                start, end, width, serverWorld, maxSteps, maxHeightDiff, maxStability, ignoreWater, 
                StructureDiscoveryCallback.defaultCallback());

        if (roadSegmentPlacementList.isEmpty()) {
            updateConnectionStatus(Records.ConnectionStatus.FAILED);
            return;
        }

        // 使用区块状态管理器验证和准备区块
        ChunkStateManager chunkManager = ChunkStateManager.getInstance();
        List<Records.RoadSegmentPlacement> safeSegments = chunkManager.prepareChunksForRoadPlacement(
            serverWorld, roadSegmentPlacementList
        );
        
        if (safeSegments.isEmpty()) {
            LOGGER.warn("No safe segments available for road placement after chunk validation");
            updateConnectionStatus(Records.ConnectionStatus.FAILED);
            return;
        }

        WorldDataProvider dataProvider = WorldDataProvider.getInstance();
        List<Records.RoadData> roadDataList = dataProvider.getRoadDataList(serverWorld);
        // 创建可变副本以避免 UnsupportedOperationException
        List<Records.RoadData> mutableList = new ArrayList<>(roadDataList != null ? roadDataList : new ArrayList<>());
        mutableList.add(new Records.RoadData(width, type, material, safeSegments));
        dataProvider.setRoadDataList(serverWorld, mutableList);

        // 完成
        updateConnectionStatus(Records.ConnectionStatus.COMPLETED);
    }

    private void updateConnectionStatus(Records.ConnectionStatus newStatus) {
        WorldDataProvider dataProvider = WorldDataProvider.getInstance();
        List<Records.StructureConnection> connections = dataProvider.getStructureConnections(serverWorld);
        // 创建可变副本以避免 UnsupportedOperationException
        List<Records.StructureConnection> mutableConnections = new ArrayList<>(connections != null ? connections : new ArrayList<>());
        
        for (int i = 0; i < mutableConnections.size(); i++) {
            Records.StructureConnection conn = mutableConnections.get(i);
            if ((conn.from().equals(structureConnection.from()) && conn.to().equals(structureConnection.to())) ||
                (conn.from().equals(structureConnection.to()) && conn.to().equals(structureConnection.from()))) {
                mutableConnections.set(i, new Records.StructureConnection(conn.from(), conn.to(), newStatus));
                dataProvider.setStructureConnections(serverWorld, mutableConnections);
                break;
            }
        }
    }

    private static int allowedRoadTypes(RandomSource deterministicRandom, IModConfig cfg) {
        if (cfg.allowArtificial() && cfg.allowNatural()) {
            return getRandomRoadType(deterministicRandom);
        } else if (cfg.allowArtificial()) {
            return 0;
        } else if (cfg.allowNatural()) {
            return 1;
        } else {
            return -1;
        }
    }

    private static int getRandomRoadType(RandomSource random) {
        return random.nextInt(2);
    }

    private static List<BlockState> getRandomMaterials(RandomSource random, List<List<BlockState>> materialsList) {
        return materialsList.get(random.nextInt(materialsList.size()));
    }

    private static int getRandomWidth(RandomSource random, List<Integer> widthList) {
        return widthList.get(random.nextInt(widthList.size()));
    }
}
