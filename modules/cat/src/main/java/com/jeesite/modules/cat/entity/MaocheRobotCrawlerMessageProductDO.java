package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

/**
 * 信息采集商品表Entity
 * @author YHQ
 * @version 2023-11-01
 */
@Table(name="maoche_robot_crawler_message_product", alias="a", label="信息采集商品表信息", columns={
		@Column(name="id", attrName="uiid", label="id", isPK=true),
		@Column(name="robot_msg_id", attrName="robotMsgId", label="机器人抓取消息id"),
		@Column(name="msg_id", attrName="msgId", label="机器人抓取消息的sync表的id"),
		@Column(name="aff_type", attrName="affType", label="jd / tb"),
		@Column(name="resource_id", attrName="resourceId", label="资源id"),
		@Column(name="inner_id", attrName="innerId", label="内部资源id"),
		@Column(name="api_content", attrName="apiContent", label="消息内容 口令信息采集 不会很长"),
		@Column(name="category", attrName="category", label="类目"),
		@Column(name="title", attrName="title", label="标题", queryType=QueryType.LIKE),
		@Column(name="short_title", attrName="shortTitle", label="短标题", queryType=QueryType.LIKE),
		@Column(name="shop_dsr", attrName="shopDsr", label="店铺分"),
		@Column(name="shop_name", attrName="shopName", label="店铺名称", queryType=QueryType.LIKE),
		@Column(name="seller_id", attrName="sellerId", label="商家id"),
		@Column(name="pict_url", attrName="pictUrl", label="图片地址"),
		@Column(name="commission_rate", attrName="commissionRate", label="佣金比例"),
		@Column(name="price", attrName="price", label="价格"),
		@Column(name="volume", attrName="volume", label="销量"),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheRobotCrawlerMessageProductDO extends DataEntity<MaocheRobotCrawlerMessageProductDO> {
	
	private static final long serialVersionUID = 1L;

	private Long uiid;		// id
	private Long robotMsgId;		// 机器人抓取消息id
	private Long msgId;		// 机器人抓取消息的sync表的id
	private String affType;		// jd / tb
	private String resourceId;		// 资源id
	private String innerId;		// 内部资源id
	private String apiContent;		// 消息内容 口令信息采集 不会很长
	private String category;		// 类目
	private String title;		// 标题
	private String shortTitle;		// 短标题
	private String shopDsr;		// 店铺分
	private String shopName;		// 店铺名称
	private String sellerId;		// 商家id

	private Long commissionRate;	// 佣金比例
	private String pictUrl;			// 图片地址
	private Long price;		// 价格
	private Long volume;		// 销量

	public MaocheRobotCrawlerMessageProductDO() {
		this(null);
	}
	
	public MaocheRobotCrawlerMessageProductDO(String id){
		super(id);
	}

	/**
	 * 重载默认方法，主键类型互转，方便操作
	 * 如果需要在 insert 后返回 自增ID，请设置 mybatis-config.xml 的 useGeneratedKeys="true"
	 */
	@Override
	public String getId() {
		return ObjectUtils.toString(getUiid());
	}

	/**
	 * 重载默认方法，主键类型互转，方便操作
	 */
	@Override
	public void setId(String id) {
		setUiid(StringUtils.isNotBlank(id) ? NumberUtils.toLong(id) : null);
	}

	public Long getUiid() {
		return uiid;
	}

	public void setUiid(Long uiid) {
		this.uiid = uiid;
	}
	
	@NotNull(message="机器人抓取消息id不能为空")
	public Long getRobotMsgId() {
		return robotMsgId;
	}

	public void setRobotMsgId(Long robotMsgId) {
		this.robotMsgId = robotMsgId;
	}
	
	@NotNull(message="机器人抓取消息的sync表的id不能为空")
	public Long getMsgId() {
		return msgId;
	}

	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}
	
	@Size(min=0, max=32, message="jd / tb长度不能超过 32 个字符")
	public String getAffType() {
		return affType;
	}

	public void setAffType(String affType) {
		this.affType = affType;
	}
	
	@NotBlank(message="资源id不能为空")
	@Size(min=0, max=64, message="资源id长度不能超过 64 个字符")
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@NotBlank(message="内部资源id不能为空")
	@Size(min=0, max=64, message="内部资源id长度不能超过 64 个字符")
	public String getInnerId() {
		return innerId;
	}

	public void setInnerId(String innerId) {
		this.innerId = innerId;
	}
	
	@NotBlank(message="消息内容 口令信息采集 不会很长不能为空")
	public String getApiContent() {
		return apiContent;
	}

	public void setApiContent(String apiContent) {
		this.apiContent = apiContent;
	}
	
	@NotBlank(message="类目不能为空")
	@Size(min=0, max=64, message="类目长度不能超过 64 个字符")
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	@NotBlank(message="标题不能为空")
	@Size(min=0, max=256, message="标题长度不能超过 256 个字符")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@NotBlank(message="短标题不能为空")
	@Size(min=0, max=256, message="短标题长度不能超过 256 个字符")
	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	
	@NotBlank(message="店铺分不能为空")
	@Size(min=0, max=256, message="店铺分长度不能超过 256 个字符")
	public String getShopDsr() {
		return shopDsr;
	}

	public void setShopDsr(String shopDsr) {
		this.shopDsr = shopDsr;
	}
	
	@NotBlank(message="店铺名称不能为空")
	@Size(min=0, max=256, message="店铺名称长度不能超过 256 个字符")
	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	
	@NotBlank(message="商家id不能为空")
	@Size(min=0, max=256, message="商家id长度不能超过 256 个字符")
	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}
	
	@NotNull(message="价格不能为空")
	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}
	
	@NotNull(message="销量不能为空")
	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public String getPictUrl() {
		return pictUrl;
	}

	public void setPictUrl(String pictUrl) {
		this.pictUrl = pictUrl;
	}

	public Long getCommissionRate() {
		return commissionRate;
	}

	public void setCommissionRate(Long commissionRate) {
		this.commissionRate = commissionRate;
	}
	public void setMsgId_in(List<Long> ids) {
		this.sqlMap.getWhere().and("msg_id", QueryType.IN, ids);
	}
	
}