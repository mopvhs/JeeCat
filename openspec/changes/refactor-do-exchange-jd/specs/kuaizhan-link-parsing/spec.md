## ADDED Requirements

### Requirement: 快站页面解析
系统 SHALL 解析快站（kuaizhan.com）页面获取淘宝口令并转链。

#### Scenario: 快站链接识别
- **WHEN** 重定向 URL 包含 `.kuaizhan.com`
- **AND** URL 包含参数 `k`
- **THEN** 系统 SHALL 调用快站 API 获取页面内容

#### Scenario: 快站 API 调用
- **WHEN** 解析快站页面
- **THEN** 系统 SHALL 调用 `https://api.cmsv5.iyunzk.com/apis/SuperPage/get` API
- **AND** 传递参数 `cms_request=1`, `device_type=web`, `key=<k参数值>`, `redirect_url=<原URL编码>`

#### Scenario: 淘客口令提取
- **WHEN** 快站 API 返回成功（code=200）
- **AND** 返回数据包含 `data.list[].form[]` 数组
- **THEN** 系统 SHALL 查找 `field="tkl"` 的元素
- **AND** 提取其 `value` 字段作为淘客口令富文本
- **AND** 将整个富文本内容传递给维易 API 进行转链

#### Scenario: 转链成功
- **WHEN** 维易 API 转链成功
- **THEN** 系统 SHALL 使用 `tbkPwd` 替换原链接
- **AND** 发送钉钉消息记录转链结果

#### Scenario: 参数 k 缺失
- **WHEN** 快站 URL 不包含参数 `k`
- **THEN** 系统 SHALL 记录错误日志 "解析获取不到k的参数，不执行转链"
- **AND** 发送钉钉告警
- **AND** 不进行转链处理

#### Scenario: API 调用失败
- **WHEN** 快站 API 调用返回空或失败
- **THEN** 系统 SHALL 将处理标记为失败
- **AND** 记录错误日志
