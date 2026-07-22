## ADDED Requirements

### Requirement: 京东优惠券链接直通
系统 SHALL 对 `coupon.m.jd.com` 域名的链接直接通过，不做转换。

#### Scenario: 优惠券链接识别
- **WHEN** URL 的重定向结果包含 `coupon.m.jd.com`
- **THEN** 系统 SHALL 将该 URL 标记为已处理成功
- **AND** 使用重定向后的 URL 作为替换内容
- **AND** 不调用任何外部 API

### Requirement: 京东 API 转链
系统 SHALL 调用订单侠 API 进行京东链接转换。

#### Scenario: 成功转链
- **WHEN** 调用订单侠 API `jdByUnionidPromotionWithCoupon` 成功
- **THEN** 系统 SHALL 使用返回的 `shortURL` 替换原链接
- **AND** 构建 `CommandDTO.Product` 商品信息
- **AND** 标记处理成功

#### Scenario: 转链失败重试
- **WHEN** 订单侠 API 调用失败
- **AND** URL 包含可重试域名（3.cn, m.cute-cat.cn）
- **THEN** 系统 SHALL 重新获取重定向 URL
- **AND** 使用新 URL 再次调用订单侠 API

#### Scenario: 淘客链接识别
- **WHEN** URL 被识别为淘客链接（needAnalysisTbUrl 返回 true）
- **THEN** 系统 SHALL 跳过京东 API 调用
- **AND** 返回状态码 304 表示需要淘宝处理

#### Scenario: 快站链接特殊处理
- **WHEN** 订单侠 API 调用成功
- **AND** 重定向 URL 包含 `.kuaizhan.com`
- **AND** 返回结果中 skuId 为空
- **THEN** 系统 SHALL 将结果标记为失败
- **AND** 交由后续处理器处理

### Requirement: 京东商品信息构建
系统 SHALL 从订单侠 API 返回结果构建商品信息。

#### Scenario: 完整商品信息
- **WHEN** API 返回包含完整商品信息
- **THEN** 系统 SHALL 提取以下字段：
  - 商品名称（skuName）
  - 价格信息（priceInfo.price, priceInfo.lowestPrice）
  - 佣金信息（commissionInfo.commissionShare, commissionInfo.commission）
  - 商品图片（imageInfo.imageList[0].url）
  - 店铺名称（shopInfo.shopName）

#### Scenario: 商品名称为空
- **WHEN** API 返回的 skuName 为空
- **THEN** 系统 SHALL 返回 null 不构建商品信息
