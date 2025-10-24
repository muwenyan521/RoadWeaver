本人将不会对该项目做任何后续的维护 具体可查看[B站作者"冷喵越龙门"的视频](https://bilibili.com/video/BV1sBsFz9EDf)
# 🛤️ RoadWeaver

自动在结构之间编织道路的 Minecraft 模组  
Automatically weave roads between structures in Minecraft

[中文](#中文) | [English](#english)

---

<a name="english"></a>
## 📖 Introduction (Features)
RoadWeaver automatically generates beautiful road networks between structures (e.g., villages, outposts), focusing on practical, stable, and visually pleasing roads.

### ✨ Core Features
- 🗺️ Intelligent Path Generation: A* pathfinding to avoid steep or dangerous areas; terrain/biome/stability-aware routing; supports E-W/N-S/diagonal directions
- 🎨 Road Types: Artificial roads (stone bricks/slabs), natural roads (dirt/gravel), biome-adaptive materials
- 🏮 Decoration System: Lampposts (redstone lamps with day/night auto control), intermittent fences, distance signs, wayfinding; large decorations (swings, benches, gazebos) with random placement
- 🧭 Visual Debugging: Road network map; status colors (planned/generating/completed/failed); interactions (drag/zoom/click-to-teleport); statistics for counts, length, and states
- 🚀 Performance: Multi-threaded async generation with concurrency control; height/terrain caching to reduce redundant computations
- 📚 Multi-Structure Support (1.0.2 or later)
- ⚡ Async Structure Search (1.0.5 or later): Non-blocking structure search with configurable thread pool (1-8 threads); round-robin search to avoid cache overflow; batch planning to prevent messy connections
- 🌳 Minimum Spanning Tree (1.0.5 or later): Kruskal's algorithm ensures all structures are connected with shortest total path length

### 🗺️ Roadmap
- More decorations? Enrich roadside and pathway ornamentation
- Link more structure types? Support broader vanilla/modded structure connectivity
- Link biomes? Strategy-level connections across biome regions
- More landmark buildings? High-quality scenic builds along roads
- Journey events? Lightweight encounters while traveling
- Custom links? Player/datapack-defined connection rules

### 📚 Multi-Structure Support（1.0.2 or later）
Now supports path-formatted structure IDs (e.g., `mvs:houses/azelea_house`) and wildcard matching:
- `mvs:houses/*` - Matches all houses from MVS
- `mvs:*` - Matches all MVS structures

Example:
```json
{
  "structuresToLocate": [
    "#minecraft:village",
    "mvs:houses/*",
    "mvs:shops/*"
  ]
}
```

### ⚠️ Notes
- The higher the "structures to locate on world load" value, the longer new world creation will take, but the initial road network completeness increases. Adjust based on your hardware and needs.

### ❓ Why another mod?
- The author finds Countered's Settlement Roads too limited in scope, while RoadArchitect currently impacts performance more. The goal is to enable diverse, beautiful roadside builds and explore experimental ideas—hence this standalone project.And also created a version that natively supports Forge。

### 🙏 Acknowledgments (References & Licenses)
This project incorporates code and concepts from the following open source projects:

**RoadArchitect** (Apache-2.0)  
https://github.com/Shadscure/RoadArchitect  
- A* pathfinding algorithm implementation (PathFinder.java → RoadPathCalculator.java)
- Cost calculation system (elevation, biome, terrain stability)
- Grid-based pathfinding approach

See the `NOTICE` file for detailed attribution and license information.

---

**Note**: This project's package name (`net.shiroha233.roadweaver`) is inspired by Countered's Settlement Roads mod, but does not use code from that project. The core pathfinding implementation is derived from RoadArchitect.

---

<a name="中文"></a>
## 📖 简介（功能介绍）
RoadWeaver 能在世界中的结构（如村庄、前哨站等）之间自动生成美观的道路网络，专注"生成好看、实用、稳定的道路"。

### ✨ 核心功能
- 🗺️ 智能路径生成：A* 寻路算法，避开陡峭与危险区域；根据地形高度、生物群系与地面稳定性调整路线；支持东西/南北/对角线方向
- 🎨 道路类型：人工道路（石砖、石板）、自然道路（泥土、砂砾）、按生物群系自适应材料
- 🏮 装饰系统：路灯（红石灯与昼夜自动控制）、间断式栏杆、距离标志、路标指引；大型点缀（秋千、长椅、凉亭）随机生成
- 🧭 可视化调试：道路网络地图；状态颜色（计划/生成/完成/失败）；交互（拖拽、缩放、点击传送）；统计道路数量、长度与状态
- 🚀 性能优化：多线程异步生成并发控制（最高128线程）；高度与地形缓存减少重复计算
- 📚 多结构同时链接支持（1.0.2版本以上）
- ⚡ 异步结构搜索（1.0.5版本以上）：非阻塞式结构搜索，可配置线程池（1-8线程）；轮询搜索避免缓存溢出；批量规划防止混乱连接
- 🌳 最小生成树算法（1.0.5版本以上）：Kruskal 算法确保所有结构连通且总路径最短

### 🗺️ 未来更新计划（Roadmap）
- 更多装饰？引入更丰富的道路与路边装饰元素
- 链接多种结构？支持更多原版/模组结构类型互联√
- 链接群系？在群系层级建立策略性连接
- 更多精美建筑？在道路沿线生成高质量景观建筑
- 路途事件？在旅行途中触发小型事件或遭遇
- 自定义链接？允许玩家/数据包定义特定连接规则

### 📚 多结构支持（1.0.2版本以上）
现在支持路径格式的结构ID（例如 `mvs:houses/azelea_house`）和通配符匹配：
- `mvs:houses/*` - 匹配所有MVS房屋
- `mvs:*` - 匹配所有MVS结构

示例配置：
```json
{
  "structuresToLocate": [
    "#minecraft:village",
    "mvs:houses/*",
    "mvs:shops/*"
  ]
}
```

### ⚠️ 注意事项（Notes）
- 设置中"加载世界时定位的结构数量"越多，创建新世界所需时间越久，但道路网络的初始完整度也更高。请根据设备性能与需求权衡。

### ❓ 有类似的模组为什么还要做？（Why another mod?）
- 作者认为 Countered's Settlement Roads 的功能偏少，RoadArchitect 在当前阶段对性能影响较大；同时作者希望在道路上看到各类精美建筑，并实现一些更大胆的玩法点子，因此决定开启独立项目以探索这些方向，并且制作了原生支持forge的版本。

### 🙏 致谢（参考与许可）
本项目借鉴了以下开源项目的代码和概念：

**RoadArchitect**（Apache-2.0）  
https://github.com/Shadscure/RoadArchitect  
- A*寻路算法实现（PathFinder.java → RoadPathCalculator.java）
- 成本计算系统（高度、生物群系、地形稳定性）
- 基于网格的寻路方法

详细归属和许可信息请参见 `NOTICE` 文件。

---

让 RoadWeaver 为你的 Minecraft 世界编织出美丽的道路网络！  
Let RoadWeaver weave beautiful road networks for your Minecraft world! 🛤️✨
