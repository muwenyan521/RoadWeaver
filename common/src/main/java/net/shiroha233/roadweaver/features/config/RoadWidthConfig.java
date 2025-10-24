package net.shiroha233.roadweaver.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

/**
 * 道路宽度配置类
 * 支持1-7格宽度的分级配置，包含装饰规则和路面材质算法
 */
public class RoadWidthConfig {
    
    // 宽度等级定义
    public static final int MIN_WIDTH = 1;
    public static final int MAX_WIDTH = 7;
    
    // 默认宽度配置
    public static final RoadWidthConfig DEFAULT = new RoadWidthConfig(
        Map.of(
            1, new WidthLevelConfig(
                List.of(List.of()), // 人工材质
                List.of(List.of()), // 自然材质
                0.1, // 装饰密度
                1,   // 最小装饰间距
                0.0  // 大型装饰概率
            ),
            3, new WidthLevelConfig(
                List.of(List.of()), // 人工材质
                List.of(List.of()), // 自然材质
                0.3, // 装饰密度
                3,   // 最小装饰间距
                0.1  // 大型装饰概率
            ),
            5, new WidthLevelConfig(
                List.of(List.of()), // 人工材质
                List.of(List.of()), // 自然材质
                0.5, // 装饰密度
                5,   // 最小装饰间距
                0.3  // 大型装饰概率
            ),
            7, new WidthLevelConfig(
                List.of(List.of()), // 人工材质
                List.of(List.of()), // 自然材质
                0.7, // 装饰密度
                7,   // 最小装饰间距
                0.5  // 大型装饰概率
            )
        ),
        Map.of(
            0, List.of(1, 3),    // 低等级道路使用1-3格宽度
            1, List.of(3, 5),    // 中等级道路使用3-5格宽度
            2, List.of(5, 7)     // 高等级道路使用5-7格宽度
        )
    );
    
    private final Map<Integer, WidthLevelConfig> widthLevels;
    private final Map<Integer, List<Integer>> roadGradeToWidthMapping;
    
    public static final Codec<RoadWidthConfig> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.unboundedMap(Codec.INT, WidthLevelConfig.CODEC).fieldOf("widthLevels").forGetter(RoadWidthConfig::getWidthLevels),
            Codec.unboundedMap(Codec.INT, Codec.INT.listOf()).fieldOf("roadGradeToWidthMapping").forGetter(RoadWidthConfig::getRoadGradeToWidthMapping)
        ).apply(instance, RoadWidthConfig::new)
    );
    
    public RoadWidthConfig(Map<Integer, WidthLevelConfig> widthLevels, 
                          Map<Integer, List<Integer>> roadGradeToWidthMapping) {
        this.widthLevels = widthLevels;
        this.roadGradeToWidthMapping = roadGradeToWidthMapping;
    }
    
    public Map<Integer, WidthLevelConfig> getWidthLevels() {
        return widthLevels;
    }
    
    public Map<Integer, List<Integer>> getRoadGradeToWidthMapping() {
        return roadGradeToWidthMapping;
    }
    
    /**
     * 获取指定宽度等级的配置
     */
    public WidthLevelConfig getWidthLevel(int width) {
        return widthLevels.getOrDefault(width, widthLevels.values().iterator().next());
    }
    
    /**
     * 获取道路等级对应的可用宽度列表
     */
    public List<Integer> getAvailableWidthsForGrade(int roadGrade) {
        return roadGradeToWidthMapping.getOrDefault(roadGrade, List.of(3)); // 默认3格宽度
    }
    
    /**
     * 验证宽度是否在有效范围内
     */
    public boolean isValidWidth(int width) {
        return width >= MIN_WIDTH && width <= MAX_WIDTH;
    }
    
    /**
     * 获取所有支持的宽度等级
     */
    public List<Integer> getSupportedWidths() {
        return List.copyOf(widthLevels.keySet());
    }
}
