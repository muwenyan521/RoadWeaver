package net.shiroha233.roadweaver.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 宽度等级配置
 * 定义特定宽度等级的道路装饰规则和材质算法
 */
public class WidthLevelConfig {
    
    private final List<List<BlockState>> artificialMaterials;
    private final List<List<BlockState>> naturalMaterials;
    private final double decorationDensity;
    private final int minDecorationSpacing;
    private final double largeDecorationProbability;
    
    public static final Codec<WidthLevelConfig> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            BlockState.CODEC.listOf().listOf().fieldOf("artificialMaterials").forGetter(WidthLevelConfig::getArtificialMaterials),
            BlockState.CODEC.listOf().listOf().fieldOf("naturalMaterials").forGetter(WidthLevelConfig::getNaturalMaterials),
            Codec.DOUBLE.fieldOf("decorationDensity").forGetter(WidthLevelConfig::getDecorationDensity),
            Codec.INT.fieldOf("minDecorationSpacing").forGetter(WidthLevelConfig::getMinDecorationSpacing),
            Codec.DOUBLE.fieldOf("largeDecorationProbability").forGetter(WidthLevelConfig::getLargeDecorationProbability)
        ).apply(instance, WidthLevelConfig::new)
    );
    
    public WidthLevelConfig(List<List<BlockState>> artificialMaterials,
                           List<List<BlockState>> naturalMaterials,
                           double decorationDensity,
                           int minDecorationSpacing,
                           double largeDecorationProbability) {
        this.artificialMaterials = artificialMaterials;
        this.naturalMaterials = naturalMaterials;
        this.decorationDensity = decorationDensity;
        this.minDecorationSpacing = minDecorationSpacing;
        this.largeDecorationProbability = largeDecorationProbability;
    }
    
    public List<List<BlockState>> getArtificialMaterials() {
        return artificialMaterials;
    }
    
    public List<List<BlockState>> getNaturalMaterials() {
        return naturalMaterials;
    }
    
    public double getDecorationDensity() {
        return decorationDensity;
    }
    
    public int getMinDecorationSpacing() {
        return minDecorationSpacing;
    }
    
    public double getLargeDecorationProbability() {
        return largeDecorationProbability;
    }
    
    /**
     * 获取装饰间距（基于宽度等级计算）
     */
    public int getDecorationSpacing(int width) {
        return Math.max(minDecorationSpacing, width * 2);
    }
    
    /**
     * 获取路灯间距（基于宽度等级计算）
     */
    public int getLamppostSpacing(int width) {
        return Math.max(30, width * 10);
    }
    
    /**
     * 获取围栏间距（基于宽度等级计算）
     */
    public int getFenceSpacing(int width) {
        return Math.max(10, width * 3);
    }
    
    /**
     * 获取大型装饰间距（基于宽度等级计算）
     */
    public int getLargeDecorationSpacing(int width) {
        return Math.max(50, width * 15);
    }
    
    /**
     * 检查是否应该放置装饰
     */
    public boolean shouldPlaceDecoration(int segmentIndex, int width) {
        return segmentIndex % getDecorationSpacing(width) == 0;
    }
    
    /**
     * 检查是否应该放置路灯
     */
    public boolean shouldPlaceLamppost(int segmentIndex, int width) {
        return segmentIndex % getLamppostSpacing(width) == 0;
    }
    
    /**
     * 检查是否应该放置围栏
     */
    public boolean shouldPlaceFence(int segmentIndex, int width) {
        return segmentIndex % getFenceSpacing(width) == 0;
    }
    
    /**
     * 检查是否应该放置大型装饰
     */
    public boolean shouldPlaceLargeDecoration(int segmentIndex, int width) {
        return segmentIndex % getLargeDecorationSpacing(width) == 0;
    }
}
