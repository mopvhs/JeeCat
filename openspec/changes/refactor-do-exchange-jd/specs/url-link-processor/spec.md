## ADDED Requirements

### Requirement: 链接处理器接口定义
系统 SHALL 提供 `UrlLinkProcessor` 接口，定义链接处理器的标准契约。

#### Scenario: 接口方法完整性
- **WHEN** 实现 `UrlLinkProcessor` 接口
- **THEN** 必须实现 `supports(ProcessingContext)` 方法判断是否支持处理
- **AND** 必须实现 `process(ProcessingContext)` 方法执行处理逻辑
- **AND** 可选实现 `getOrder()` 方法指定处理优先级

### Requirement: 处理结果封装
系统 SHALL 提供 `LinkProcessResult` 类封装处理结果。

#### Scenario: 结果字段完整性
- **WHEN** 处理器返回处理结果
- **THEN** 结果 MUST 包含 `handled` 标识是否已处理
- **AND** 结果 MUST 包含 `success` 标识处理是否成功
- **AND** 结果 MUST 包含 `fatal` 标识是否为致命错误需中断流程
- **AND** 结果 MAY 包含 `replacedContent` 替换后的内容
- **AND** 结果 MAY 包含 `product` 商品信息

### Requirement: 处理上下文封装
系统 SHALL 提供 `ProcessingContext` 类封装处理上下文。

#### Scenario: 上下文字段完整性
- **WHEN** 创建处理上下文
- **THEN** 上下文 MUST 包含原始 URL
- **AND** 上下文 MUST 包含 `ShortUrlDetail` 详情对象
- **AND** 上下文 MUST 包含 `CommandContext` 命令上下文引用
- **AND** 上下文 MAY 包含重定向后的 URL

### Requirement: 处理链执行顺序
系统 SHALL 按固定顺序执行处理器链。

#### Scenario: 正常执行顺序
- **WHEN** 开始处理一个 URL
- **THEN** 系统按以下顺序尝试各处理器：
  1. JdCouponLinkProcessor（京东优惠券直通）
  2. JdUnionApiProcessor（京东 API 转链）
  3. Cyg888LinkProcessor（cyg888 特殊处理）
  4. TbWordLinkProcessor（淘宝口令转链）
  5. KuaizhanLinkProcessor（快站解析）

#### Scenario: 处理中断
- **WHEN** 某个处理器返回 `fatal=true`
- **THEN** 系统 SHALL 立即中断处理链
- **AND** 返回当前处理结果

#### Scenario: 处理器匹配
- **WHEN** 某个处理器的 `supports()` 返回 `true` 且 `handled=true`
- **THEN** 系统 SHALL 停止尝试后续处理器
