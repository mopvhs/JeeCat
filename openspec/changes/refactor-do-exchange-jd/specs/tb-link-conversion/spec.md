## ADDED Requirements

### Requirement: 淘宝口令参数解析
系统 SHALL 解析 URL 中的 taowords 或 word 参数进行淘宝转链。

#### Scenario: 单个口令转链
- **WHEN** URL 参数中存在 taowords 或 word 参数
- **AND** 参数值只有一个
- **THEN** 系统 SHALL 构造口令格式 `7(<口令>)/ CA1500`
- **AND** 调用维易 API `generalConvert` 进行转链
- **AND** 使用返回的 `tbkPwd` 替换原链接

#### Scenario: 多个口令转链
- **WHEN** URL 参数中存在多个 taowords 值
- **THEN** 系统 SHALL 依次对每个口令进行转链
- **AND** 如果存在 tips 参数，将对应的 tip 添加在每个口令前
- **AND** 拼接所有转链结果替换原链接

#### Scenario: 多口令部分失败
- **WHEN** 多个口令转链过程中任一口令转链失败
- **THEN** 系统 SHALL 将整个处理标记为失败
- **AND** 停止处理剩余口令

### Requirement: 淘宝转链通用处理
系统 SHALL 对所有淘宝转链成功的结果进行统一处理。

#### Scenario: 转链成功后处理
- **WHEN** 淘宝 API 转链成功
- **THEN** 系统 SHALL 设置 `detail.apiRes = true`
- **AND** 设置 `detail.replaceUrl` 为转链后的口令
- **AND** 调用 `detail.addExchangeLog` 记录口令
- **AND** 设置 `detail.tbProduct` 为完整的返回对象

### Requirement: 淘宝转链 API 参数配置
系统 SHALL 使用正确的 API 参数调用维易接口。

#### Scenario: API 参数配置
- **WHEN** 调用 `tbApiService.generalConvert`
- **THEN** 系统 SHALL 设置 `detail = 2`
- **AND** 设置 `deepcoupon = 1`
- **AND** 设置 `couponId = 1`
