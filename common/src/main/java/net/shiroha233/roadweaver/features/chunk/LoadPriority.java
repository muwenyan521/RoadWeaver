package net.shiroha233.roadweaver.features.chunk;

/**
 * 区块加载优先级枚举
 */
public enum LoadPriority {
    /**
     * 最高优先级 - 用于紧急道路放置操作
     */
    HIGH(3),
    
    /**
     * 标准优先级 - 用于常规道路生成
     */
    STANDARD(2),
    
    /**
     * 低优先级 - 用于后台区块预加载
     */
    LOW(1);
    
    private final int priorityValue;
    
    LoadPriority(int priorityValue) {
        this.priorityValue = priorityValue;
    }
    
    /**
     * 获取优先级数值（数值越高优先级越高）
     */
    public int getPriorityValue() {
        return priorityValue;
    }
    
    /**
     * 比较两个优先级的相对顺序
     */
    public boolean isHigherThan(LoadPriority other) {
        return this.priorityValue > other.priorityValue;
    }
    
    /**
     * 比较两个优先级的相对顺序
     */
    public boolean isLowerThan(LoadPriority other) {
        return this.priorityValue < other.priorityValue;
    }
    
    /**
     * 比较两个优先级的相对顺序
     */
    public boolean isSameAs(LoadPriority other) {
        return this.priorityValue == other.priorityValue;
    }
}
