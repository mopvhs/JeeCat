## Why

`AiCommandService#doExchangeJd` 方法当前约 330 行代码，存在过度嵌套（最深 7-8 层）、重复代码块（淘宝转链处理逻辑重复 4+ 次）、职责不单一（单方法处理 URL 提取、重定向、京东转链、淘宝转链、快站解析等多种逻辑）等问题。代码难以维护、测试和扩展，新增链接类型支持需要修改核心方法，风险较高。现在需要重构以提高代码可维护性和可扩展性。

## What Changes

- 将 `doExchangeJd` 方法从 330 行精简到约 50 行，仅保留主流程编排逻辑
- 引入 `UrlLinkProcessor` 策略接口，支持多种链接类型处理器
- 创建 5 个具体处理器实现类，分别处理不同类型的链接转换
- 抽取公共方法 `handleTbApiSuccess`，消除淘宝转链后的重复处理代码
- 创建 `LinkProcessResult` DTO 封装处理结果
- **功能行为保持完全不变**，仅优化代码结构

## Capabilities

### New Capabilities
- `url-link-processor`: 链接处理器策略模式架构，定义统一的链接转换处理接口和结果封装
- `jd-link-conversion`: 京东链接转换能力，包含 JD API 调用和优惠券直通处理
- `tb-link-conversion`: 淘宝链接转换能力，包含 taowords 参数解析和口令转换
- `kuaizhan-link-parsing`: 快站页面解析能力，从快站页面提取淘宝口令并转链
- `cyg888-link-parsing`: cyg888.cn 特殊链接解析能力

### Modified Capabilities
<!-- 本次为纯重构，不修改任何业务需求 -->

## Impact

### 影响的代码文件
- **修改**: `modules/cat/src/main/java/com/jeesite/modules/cat/service/toolbox/AiCommandService.java`
- **新增**: `UrlLinkProcessor.java` - 处理器接口
- **新增**: `LinkProcessResult.java` - 处理结果 DTO
- **新增**: `JdUnionApiProcessor.java` - 京东 API 处理器
- **新增**: `JdCouponLinkProcessor.java` - 京东优惠券直通处理器
- **新增**: `TbWordLinkProcessor.java` - 淘宝口令处理器
- **新增**: `KuaizhanLinkProcessor.java` - 快站处理器
- **新增**: `Cyg888LinkProcessor.java` - cyg888 处理器

### 影响的 API
- 无 - 对外接口保持不变

### 依赖关系
- 无新增依赖
- 内部依赖：新增的处理器类将依赖 `TbApiService`, `DingDanXiaApiService`, `OkHttpService`, `FlameHttpService` 等现有服务

### 风险点
- 需要完整的回归测试确保功能不变
- 处理器链的执行顺序必须与原代码一致
