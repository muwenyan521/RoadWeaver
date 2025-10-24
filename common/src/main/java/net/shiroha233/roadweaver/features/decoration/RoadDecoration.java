package net.shiroha233.roadweaver.features.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.shiroha233.roadweaver.features.roadlogic.Road;
import net.shiroha233.roadweaver.features.biome.BiomeConnectionStrategy;

import java.util.List;

/**
 * 道路装饰接口 - 定义所有装饰类型必须实现的方法
 */
public interface RoadDecoration {
    
    /**
     * 获取装饰名称
     */
    String getName();
    
    /**
     * 获取装饰类型
     */
    DecorationType getType();
    
    /**
     * 检查是否可以在指定位置放置装饰
     */
    boolean canPlaceAt(LevelAccessor level, BlockPos pos, Road road, BiomeConnectionStrategy biomeStrategy);
    
    /**
     * 生成装饰
     */
    List<BlockPlacement> generateDecoration(LevelAccessor level, BlockPos pos, Road road, BiomeConnectionStrategy biomeStrategy);
    
    /**
     * 获取放置概率
     */
    double getPlacementProbability(Road road, BiomeConnectionStrategy biomeStrategy);
    
    /**
     * 获取最小放置距离
     */
    int getMinDistance();
    
    /**
     * 检查与其他装饰的兼容性
     */
    boolean isCompatibleWith(RoadDecoration other);
}
