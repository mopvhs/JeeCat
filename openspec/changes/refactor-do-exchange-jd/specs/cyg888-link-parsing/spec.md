## ADDED Requirements

### Requirement: cyg888.cn 链接解析
系统 SHALL 解析 s.cyg888.cn 域名的链接获取淘宝口令。

#### Scenario: cyg888 链接识别
- **WHEN** 重定向 URL 包含 `s.cyg888.cn`
- **AND** URL 包含参数 `sn`
- **THEN** 系统 SHALL 调用 cyg888 API 获取页面详情

#### Scenario: cyg888 API 调用
- **WHEN** 解析 cyg888 链接
- **THEN** 系统 SHALL 调用 `http://s.cyg888.cn/api_v3/middle.php`
- **AND** 传递参数 `sn=<sn参数值>`, `type=get_page_detail`, `password=`
- **AND** 设置 HTTP Header `Referer` 为原始 URL

#### Scenario: 口令提取成功
- **WHEN** API 返回成功
- **AND** 返回 `data` 为 JSONArray 且长度大于 0
- **THEN** 系统 SHALL 提取 `data[0].content` 作为口令内容
- **AND** 将内容中的 `<br>` 替换为空字符串
- **AND** 将处理后的内容传递给维易 API 进行转链

#### Scenario: API 返回为空
- **WHEN** cyg888 API 返回空响应
- **THEN** 系统 SHALL 返回 null 表示处理失败

#### Scenario: 返回数据格式错误
- **WHEN** API 返回的 `data` 不是 JSONArray 或长度为 0
- **THEN** 系统 SHALL 返回 null 表示处理失败

#### Scenario: sn 参数缺失
- **WHEN** URL 不包含 `sn` 参数
- **THEN** 系统 SHALL 返回 null 不处理该链接
