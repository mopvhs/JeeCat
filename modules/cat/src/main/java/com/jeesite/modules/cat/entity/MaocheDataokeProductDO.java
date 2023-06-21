package com.jeesite.modules.cat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.junit.Ignore;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * maoche_dataoke_productEntity
 * @author YHQ
 * @version 2023-06-04
 */
@Table(name="maoche_dataoke_product", alias="a", label="maoche_dataoke_product信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="dtk_id", attrName="dtkId", label="dtk_id"),
		@Column(name="goods_id", attrName="goodsId", label="goods_id"),
		@Column(name="orig_content", attrName="origContent", label="原始JSON数据"),
		@Column(name="original_price", attrName="originalPrice", label="原价"),
		@Column(name="actual_price", attrName="actualPrice", label="券后价"),
		@Column(name="coupon_price", attrName="couponPrice", label="优惠券金额"),
		@Column(name="commission_rate", attrName="commissionRate", label="佣金比率"),
		@Column(name="month_sales", attrName="monthSales", label="30天销量"),
		@Column(name="hot_push", attrName="hotPush", label="热推值"),
		@Column(name="special_text", attrName="specialText", label="特色文案"),
		@Column(name="coupon_remain_count", attrName="couponRemainCount", label="优惠券剩余量"),
		@Column(name="coupon_receive_num", attrName="couponReceiveNum", label="优惠券领券量"),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
@JsonIgnoreProperties(ignoreUnknown=true)
public class MaocheDataokeProductDO extends DataEntity<MaocheDataokeProductDO> {
	
	private static final long serialVersionUID = 1L;
	private Long dtkId;		// dtk_id
	private String goodsId;		// goods_id
	private String origContent;		// 原始JSON数据

	private Long originalPrice;
	private Long actualPrice;
	private Long monthSales;
	private Long couponReceiveNum;
	private Long couponPrice;
	private Long commissionRate;
	private Long hotPush;
	private Long couponRemainCount;
	private String specialText;

	public MaocheDataokeProductDO() {
		this(null);
	}
	
	public MaocheDataokeProductDO(String id){
		super(id);
	}
	
	@NotNull(message="dtk_id不能为空")
	public Long getDtkId() {
		return dtkId;
	}

	public void setDtkId(Long dtkId) {
		this.dtkId = dtkId;
	}
	
	@Size(min=0, max=128, message="goods_id长度不能超过 128 个字符")
	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	
	public String getOrigContent() {
		return origContent;
	}

	public void setOrigContent(String origContent) {
		this.origContent = origContent;
	}

	public Long getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(Long originalPrice) {
		this.originalPrice = originalPrice;
	}

	public Long getActualPrice() {
		return actualPrice;
	}

	public void setActualPrice(Long actualPrice) {
		this.actualPrice = actualPrice;
	}

	public Long getMonthSales() {
		return monthSales;
	}

	public void setMonthSales(Long monthSales) {
		this.monthSales = monthSales;
	}

	public Long getCouponReceiveNum() {
		return couponReceiveNum;
	}

	public void setCouponReceiveNum(Long couponReceiveNum) {
		this.couponReceiveNum = couponReceiveNum;
	}

	public Long getCouponPrice() {
		return couponPrice;
	}

	public void setCouponPrice(Long couponPrice) {
		this.couponPrice = couponPrice;
	}

	public Long getCommissionRate() {
		return commissionRate;
	}

	public void setCommissionRate(Long commissionRate) {
		this.commissionRate = commissionRate;
	}

	public Long getHotPush() {
		return hotPush;
	}

	public void setHotPush(Long hotPush) {
		this.hotPush = hotPush;
	}

	public Long getCouponRemainCount() {
		return couponRemainCount;
	}

	public void setCouponRemainCount(Long couponRemainCount) {
		this.couponRemainCount = couponRemainCount;
	}

	public String getSpecialText() {
		return specialText;
	}

	public void setSpecialText(String specialText) {
		this.specialText = specialText;
	}

	public void setDtkId_in(Long[] ids) {
		this.sqlMap.getWhere().and("dtk_id", QueryType.IN, ids);
	}
}