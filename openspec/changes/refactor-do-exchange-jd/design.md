## Context

### 背景
`AiCommandService#doExchangeJd` 是口令转链服务的核心方法，负责处理京东和淘宝链接的转换。当前实现存在以下问题：

1. **代码复杂度高**：约 330 行代码，嵌套层级最深达 7-8 层
2. **重复代码多**：淘宝转链成功后的处理逻辑重复 4+ 次
3. **可扩展性差**：新增链接类型需修改核心方法
4. **测试困难**：无法对单个处理逻辑进行单元测试

### 当前状态
```
doExchangeJd()
  ├── 参数校验
  ├── URL 提取（正则匹配）
  └── for each URL:
      ├── 获取重定向 URL
      ├── if coupon.m.jd.com → 直接通过
      ├── if 淘客链接 → 跳过
      ├── else → 调用订单侠 API
      │   ├── 成功 → 构建京东商品
      │   └── 失败 → 
      │       ├── if 需要解析淘宝 →
      │       │   ├── if cyg888.cn → 特殊处理
      │       │   ├── if taowords 参数 → 淘宝转链
      │       │   └── else → 直接替换
      │       └── if 快站 → 快站解析
      └── 更新 content
```

### 约束
- 必须保持功能完全不变
- 必须兼容现有的 `CommandContext` 和 `ShortUrlDetail` 数据结构
- 处理器执行顺序必须与原代码一致

## Goals / Non-Goals

**Goals:**
- 将 `doExchangeJd` 方法精简到 50 行以内
- 消除所有重复代码
- 实现处理器的可插拔架构
- 每个处理器可独立进行单元测试
- 代码可读性显著提升

**Non-Goals:**
- 不修改任何业务逻辑
- 不修改对外接口
- 不新增依赖库
- 不修改 `CommandContext`、`ShortUrlDetail` 等现有数据结构
- 不涉及性能优化

## Decisions

### Decision 1: 使用责任链模式而非策略模式

**选择**: 责任链模式（Chain of Responsibility）

**理由**:
- 原代码中处理器有明确的优先级和匹配顺序
- 一个 URL 可能需要多步处理（如先获取重定向再转链）
- 策略模式适合互斥选择，责任链更适合有序尝试

**备选方案**:
| 方案 | 优点 | 缺点 |
|------|------|------|
| 策略模式 | 实现简单 | 不支持顺序处理 |
| 责任链模式 ✓ | 支持优先级、可中断 | 稍复杂 |
| 规则引擎 | 灵活配置 | 过度设计 |

### Decision 2: 处理器注册方式

**选择**: 手动注册 + 固定顺序列表

**理由**:
- 处理器数量固定（5个），不需要动态发现
- 执行顺序关键，必须精确控制
- 避免 Spring 自动注入顺序不确定的问题

**实现**:
```java
private List<UrlLinkProcessor> processors = Arrays.asList(
    new JdCouponLinkProcessor(),      // 1. 优惠券直通
    new JdUnionApiProcessor(),         // 2. 京东 API
    new Cyg888LinkProcessor(),         // 3. cyg888 特殊处理
    new TbWordLinkProcessor(),         // 4. 淘宝口令
    new KuaizhanLinkProcessor()        // 5. 快站解析
);
```

### Decision 3: 处理结果传递

**选择**: 使用 `LinkProcessResult` 封装处理结果

**理由**:
- 需要传递多种信息：是否成功、替换内容、商品信息、是否致命错误
- 比使用 `Result<>` + 状态码更清晰
- 避免原代码中大量的状态标志变量

**结构设计**:
```java
@Data
public class LinkProcessResult {
    private boolean handled;        // 是否已处理（即使失败也算处理）
    private boolean success;        // 处理是否成功
    private boolean fatal;          // 是否致命错误，需中断整个流程
    private String replacedContent; // 替换后的内容
    private CommandDTO.Product product;      // 京东商品信息
    private GeneralConvertResp tbProduct;    // 淘宝商品信息
    private String errorMessage;    // 错误信息
}
```

### Decision 4: 处理器接口设计

**选择**: 三阶段接口（supports + process + postProcess）

**理由**:
- `supports()`: 快速判断是否应该处理
- `process()`: 执行主要处理逻辑
- `postProcess()`: 可选的后处理（如更新 detail）

**接口定义**:
```java
public interface UrlLinkProcessor {
    /**
     * 判断此处理器是否支持处理该 URL
     */
    boolean supports(ProcessingContext ctx);
    
    /**
     * 执行处理
     */
    LinkProcessResult process(ProcessingContext ctx);
    
    /**
     * 获取处理器优先级（数字越小优先级越高）
     */
    default int getOrder() {
        return 100;
    }
}
```

### Decision 5: 上下文传递

**选择**: 创建 `ProcessingContext` 封装处理上下文

**理由**:
- 避免方法参数过多
- 便于在处理链中共享状态
- 包含原始 URL、重定向 URL、CommandContext 引用等

## Risks / Trade-offs

### 风险
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 处理顺序错误 | 功能异常 | 编写对比测试用例 |
| 遗漏边界条件 | 特定链接转换失败 | 代码逐行对照审查 |
| 状态传递遗漏 | detail 信息不完整 | 验证所有 addExchangeLog 调用 |

### 权衡
- **复杂度 vs 可维护性**: 引入处理器模式增加了类数量，但大幅提升可维护性
- **灵活性 vs 简单性**: 手动注册处理器降低了灵活性，但保证了顺序可控

## Migration Plan

### 部署步骤
1. 创建新的处理器类（不影响现有代码）
2. 在 `AiCommandService` 中添加新的 `doExchangeJdRefactored` 方法
3. 进行 A/B 测试对比两个方法的输出
4. 确认无差异后，替换原方法调用
5. 删除原 `doExchangeJd` 方法

### 回滚策略
- 保留原方法一个版本周期
- 通过配置开关控制使用新/旧方法
- 回滚时仅需修改开关配置

## Open Questions

1. ~~是否需要为处理器添加 Spring 注入支持？~~ → **决定不需要**，保持简单
2. 是否需要添加处理耗时统计？→ 可作为后续优化
3. 原代码中的 `DingDingService.sendParseDingDingMsg` 调用是否需要保持在相同位置？→ 需验证
