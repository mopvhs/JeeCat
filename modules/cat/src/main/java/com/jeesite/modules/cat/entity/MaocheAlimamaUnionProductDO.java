package com.jeesite.modules.cat.entity;

import javax.validation.constraints.Size;
import java.util.Date;
import com.jeesite.common.mybatis.annotation.JoinTable;
import com.jeesite.common.mybatis.annotation.JoinTable.Type;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * maoche_alimama_union_productEntity
 * @author YHQ
 * @version 2023-05-05
 */
@Table(name="maoche_alimama_union_product", alias="a", label="maoche_alimama_union_product信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="orig_content", attrName="origContent", label="原始JSON数据"),
		@Column(name="category_id", attrName="categoryId", label="category_id", isUpdateForce=true),
		@Column(name="category_name", attrName="categoryName", label="category_name", queryType=QueryType.LIKE),
		@Column(name="commission_rate", attrName="commissionRate", label="commission_rate"),
		@Column(name="commission_type", attrName="commissionType", label="commission_type"),
		@Column(name="coupon_amount", attrName="couponAmount", label="coupon_amount", isUpdateForce=true),
		@Column(name="coupon_end_time", attrName="couponEndTime", label="coupon_end_time", isUpdateForce=true),
		@Column(name="coupon_id", attrName="couponId", label="coupon_id"),
		@Column(name="coupon_info", attrName="couponInfo", label="coupon_info"),
		@Column(name="coupon_remain_count", attrName="couponRemainCount", label="coupon_remain_count", isUpdateForce=true),
		@Column(name="coupon_share_url", attrName="couponShareUrl", label="coupon_share_url"),
		@Column(name="coupon_start_fee", attrName="couponStartFee", label="coupon_start_fee", isUpdateForce=true),
		@Column(name="coupon_start_time", attrName="couponStartTime", label="coupon_start_time", isUpdateForce=true),
		@Column(name="coupon_total_count", attrName="couponTotalCount", label="coupon_total_count", isUpdateForce=true),
		@Column(name="include_dxjh", attrName="includeDxjh", label="include_dxjh"),
		@Column(name="include_mkt", attrName="includeMkt", label="include_mkt"),
		@Column(name="info_dxjh", attrName="infoDxjh", label="info_dxjh"),
		@Column(name="item_description", attrName="itemDescription", label="item_description"),
		@Column(name="item_id", attrName="itemId", label="item_id"),
		@Column(name="item_id_suffix", attrName="itemIdSuffix", label="item_id_suffix"),
		@Column(name="item_url", attrName="itemUrl", label="item_url"),
		@Column(name="level_one_category_id", attrName="levelOneCategoryId", label="level_one_category_id", isUpdateForce=true),
		@Column(name="level_one_category_name", attrName="levelOneCategoryName", label="level_one_category_name", queryType=QueryType.LIKE),
		@Column(name="nick", attrName="nick", label="nick"),
		@Column(name="num_iid", attrName="numIid", label="num_iid"),
		@Column(name="pict_url", attrName="pictUrl", label="pict_url"),
		@Column(name="presale_deposit", attrName="presaleDeposit", label="presale_deposit"),
		@Column(name="provcity", attrName="provcity", label="provcity"),
		@Column(name="real_post_fee", attrName="realPostFee", label="real_post_fee", isUpdateForce=true),
		@Column(name="reserve_price", attrName="reservePrice", label="reserve_price", isUpdateForce=true),
		@Column(name="seller_id", attrName="sellerId", label="seller_id", isUpdateForce=true),
		@Column(name="shop_dsr", attrName="shopDsr", label="shop_dsr", isUpdateForce=true),
		@Column(name="shop_title", attrName="shopTitle", label="shop_title", queryType=QueryType.LIKE),
		@Column(name="short_title", attrName="shortTitle", label="short_title", queryType=QueryType.LIKE),
		@Column(name="small_images", attrName="smallImages", label="small_images"),
		@Column(name="superior_brand", attrName="superiorBrand", label="superior_brand"),
		@Column(name="title", attrName="title", label="title", queryType=QueryType.LIKE),
		@Column(name="tk_total_commi", attrName="tkTotalCommi", label="tk_total_commi"),
		@Column(name="tk_total_sales", attrName="tkTotalSales", label="tk_total_sales"),
		@Column(name="url", attrName="url", label="url"),
		@Column(name="audit_status", attrName="auditStatus", label="audit_status"),
		@Column(name="sale_status", attrName="saleStatus", label="sale_status"),
		@Column(name="user_type", attrName="userType", label="user_type", isUpdateForce=true),
		@Column(name="volume", attrName="volume", label="volume", isUpdateForce=true),
		@Column(name="white_image", attrName="whiteImage", label="white_image"),
		@Column(name="x_id", attrName="xid", label="x_id"),
		@Column(name="zk_final_price", attrName="zkFinalPrice", label="zk_final_price", isUpdateForce=true),
		@Column(name="coupon", attrName="coupon", label="coupon", isUpdateForce=true),
		@Column(name="sync_mark", attrName="syncMark", label="sync_mark", isUpdateForce=true),
		@Column(name="create_time", attrName="createTime", label="create_time", isUpdateForce=true),
		@Column(name="update_time", attrName="updateTime", label="update_time", isUpdateForce=true),
		@Column(name="sale_status_date", attrName="saleStatusDate", label="sale_status_date", isUpdateForce=true),
	}, orderBy="a.id DESC"
)
public class MaocheAlimamaUnionProductDO extends DataEntity<MaocheAlimamaUnionProductDO> {
	
	private static final long serialVersionUID = 1L;
	private Long iid;
	private String origContent;		// 原始JSON数据 https://www.veapi.cn/apidoc/taobaolianmeng/86
	private Long categoryId;		// category_id
	private String categoryName;		// category_name
	private String commissionRate;		// commission_rate
	private String commissionType;		// commission_type
	private Double couponAmount;		// coupon_amount
	private Date couponEndTime;		// coupon_end_time
	private String couponId;		// coupon_id
	private String couponInfo;		// coupon_info
	private Long couponRemainCount;		// coupon_remain_count
	private String couponShareUrl;		// coupon_share_url
	private Double couponStartFee;		// coupon_start_fee
	private Date couponStartTime;		// coupon_start_time
	private Long couponTotalCount;		// coupon_total_count
	private String includeDxjh;		// include_dxjh
	private String includeMkt;		// include_mkt
	private String infoDxjh;		// info_dxjh
	private String itemDescription;		// item_description
	private String itemId;		// item_id
	private String itemIdSuffix;
	private String itemUrl;		// item_url
	private Long levelOneCategoryId;		// level_one_category_id
	private String levelOneCategoryName;		// level_one_category_name
	private String nick;		// nick
	private String numIid;		// num_iid
	private String pictUrl;		// pict_url
	private String presaleDeposit;		// presale_deposit
	private String provcity;		// provcity
	private Double realPostFee;		// real_post_fee
	private Double reservePrice;		// reserve_price
	private Long sellerId;		// seller_id
	private Long shopDsr;		// shop_dsr
	private String shopTitle;		// shop_title
	private String shortTitle;		// short_title
	private String smallImages;		// small_images
	private String superiorBrand;		// superior_brand
	private String title;		// title
	private String tkTotalCommi;		// tk_total_commi
	private String tkTotalSales;		// tk_total_sales
	private String url;		// url
	private Long userType;		// user_type
	private Long volume;		// volume
	private String whiteImage;		// white_image
	private String xid;		// x_id
	private Double zkFinalPrice;		// zk_final_price
	private Double coupon;		// coupon
	// 审核状态
	/**
	 * {@link AuditStatusEnum}
	 */
	private Long auditStatus;
	/**
	 * {@link com.jeesite.modules.cat.enums.SaleStatusEnum}
	 */
	private Long saleStatus;
	private Date saleStatusDate;
	// 入库时间
	private Date createTime;
	// 更新时间
	private Date updateTime;
	// 更新标记
	private Long syncMark;

	/**
	 * 重载默认方法，主键类型互转，方便操作
	 * 如果需要在 insert 后返回 自增ID，请设置 mybatis-config.xml 的 useGeneratedKeys="true"
	 */
	@Override
	public String getId() {
		return ObjectUtils.toString(getIid());
	}

	/**
	 * 重载默认方法，主键类型互转，方便操作
	 */
	@Override
	public void setId(String id) {
		setIid(StringUtils.isNotBlank(id) ? NumberUtils.toLong(id) : null);
	}

	public Long getIid() {
		return iid;
	}

	public void setIid(Long iid) {
		this.iid = iid;
	}

	public MaocheAlimamaUnionProductDO() {
		this(null);
	}
	
	public MaocheAlimamaUnionProductDO(String id){
		super(id);
	}
	
	public String getOrigContent() {
		return origContent;
	}

	public void setOrigContent(String origContent) {
		this.origContent = origContent;
	}
	
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	
	@Size(min=0, max=255, message="category_name长度不能超过 255 个字符")
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	@Size(min=0, max=255, message="commission_rate长度不能超过 255 个字符")
	public String getCommissionRate() {
		return commissionRate;
	}

	public void setCommissionRate(String commissionRate) {
		this.commissionRate = commissionRate;
	}
	
	@Size(min=0, max=255, message="commission_type长度不能超过 255 个字符")
	public String getCommissionType() {
		return commissionType;
	}

	public void setCommissionType(String commissionType) {
		this.commissionType = commissionType;
	}
	
	public Double getCouponAmount() {
		return couponAmount;
	}

	public void setCouponAmount(Double couponAmount) {
		this.couponAmount = couponAmount;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCouponEndTime() {
		return couponEndTime;
	}

	public void setCouponEndTime(Date couponEndTime) {
		this.couponEndTime = couponEndTime;
	}
	
	@Size(min=0, max=255, message="coupon_id长度不能超过 255 个字符")
	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}
	
	@Size(min=0, max=255, message="coupon_info长度不能超过 255 个字符")
	public String getCouponInfo() {
		return couponInfo;
	}

	public void setCouponInfo(String couponInfo) {
		this.couponInfo = couponInfo;
	}
	
	public Long getCouponRemainCount() {
		return couponRemainCount;
	}

	public void setCouponRemainCount(Long couponRemainCount) {
		this.couponRemainCount = couponRemainCount;
	}
	
	public String getCouponShareUrl() {
		return couponShareUrl;
	}

	public void setCouponShareUrl(String couponShareUrl) {
		this.couponShareUrl = couponShareUrl;
	}
	
	public Double getCouponStartFee() {
		return couponStartFee;
	}

	public void setCouponStartFee(Double couponStartFee) {
		this.couponStartFee = couponStartFee;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCouponStartTime() {
		return couponStartTime;
	}

	public void setCouponStartTime(Date couponStartTime) {
		this.couponStartTime = couponStartTime;
	}
	
	public Long getCouponTotalCount() {
		return couponTotalCount;
	}

	public void setCouponTotalCount(Long couponTotalCount) {
		this.couponTotalCount = couponTotalCount;
	}
	
	@Size(min=0, max=255, message="include_dxjh长度不能超过 255 个字符")
	public String getIncludeDxjh() {
		return includeDxjh;
	}

	public void setIncludeDxjh(String includeDxjh) {
		this.includeDxjh = includeDxjh;
	}
	
	@Size(min=0, max=255, message="include_mkt长度不能超过 255 个字符")
	public String getIncludeMkt() {
		return includeMkt;
	}

	public void setIncludeMkt(String includeMkt) {
		this.includeMkt = includeMkt;
	}
	
	public String getInfoDxjh() {
		return infoDxjh;
	}

	public void setInfoDxjh(String infoDxjh) {
		this.infoDxjh = infoDxjh;
	}
	
	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	
	@Size(min=0, max=255, message="item_id长度不能超过 255 个字符")
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemIdSuffix() {
		return itemIdSuffix;
	}

	public void setItemIdSuffix(String itemIdSuffix) {
		this.itemIdSuffix = itemIdSuffix;
	}

	public String getItemUrl() {
		return itemUrl;
	}

	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}
	
	public Long getLevelOneCategoryId() {
		return levelOneCategoryId;
	}

	public void setLevelOneCategoryId(Long levelOneCategoryId) {
		this.levelOneCategoryId = levelOneCategoryId;
	}
	
	@Size(min=0, max=255, message="level_one_category_name长度不能超过 255 个字符")
	public String getLevelOneCategoryName() {
		return levelOneCategoryName;
	}

	public void setLevelOneCategoryName(String levelOneCategoryName) {
		this.levelOneCategoryName = levelOneCategoryName;
	}
	
	@Size(min=0, max=255, message="nick长度不能超过 255 个字符")
	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}
	
	@Size(min=0, max=255, message="num_iid长度不能超过 255 个字符")
	public String getNumIid() {
		return numIid;
	}

	public void setNumIid(String numIid) {
		this.numIid = numIid;
	}
	
	public String getPictUrl() {
		return pictUrl;
	}

	public void setPictUrl(String pictUrl) {
		this.pictUrl = pictUrl;
	}
	
	@Size(min=0, max=255, message="presale_deposit长度不能超过 255 个字符")
	public String getPresaleDeposit() {
		return presaleDeposit;
	}

	public void setPresaleDeposit(String presaleDeposit) {
		this.presaleDeposit = presaleDeposit;
	}
	
	@Size(min=0, max=255, message="provcity长度不能超过 255 个字符")
	public String getProvcity() {
		return provcity;
	}

	public void setProvcity(String provcity) {
		this.provcity = provcity;
	}
	
	public Double getRealPostFee() {
		return realPostFee;
	}

	public void setRealPostFee(Double realPostFee) {
		this.realPostFee = realPostFee;
	}
	
	public Double getReservePrice() {
		return reservePrice;
	}

	public void setReservePrice(Double reservePrice) {
		this.reservePrice = reservePrice;
	}
	
	public Long getSellerId() {
		return sellerId;
	}

	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}
	
	public Long getShopDsr() {
		return shopDsr;
	}

	public void setShopDsr(Long shopDsr) {
		this.shopDsr = shopDsr;
	}
	
	@Size(min=0, max=255, message="shop_title长度不能超过 255 个字符")
	public String getShopTitle() {
		return shopTitle;
	}

	public void setShopTitle(String shopTitle) {
		this.shopTitle = shopTitle;
	}
	
	@Size(min=0, max=255, message="short_title长度不能超过 255 个字符")
	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	
	public String getSmallImages() {
		return smallImages;
	}

	public void setSmallImages(String smallImages) {
		this.smallImages = smallImages;
	}
	
	@Size(min=0, max=255, message="superior_brand长度不能超过 255 个字符")
	public String getSuperiorBrand() {
		return superiorBrand;
	}

	public void setSuperiorBrand(String superiorBrand) {
		this.superiorBrand = superiorBrand;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Size(min=0, max=255, message="tk_total_commi长度不能超过 255 个字符")
	public String getTkTotalCommi() {
		return tkTotalCommi;
	}

	public void setTkTotalCommi(String tkTotalCommi) {
		this.tkTotalCommi = tkTotalCommi;
	}
	
	@Size(min=0, max=255, message="tk_total_sales长度不能超过 255 个字符")
	public String getTkTotalSales() {
		return tkTotalSales;
	}

	public void setTkTotalSales(String tkTotalSales) {
		this.tkTotalSales = tkTotalSales;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Long getUserType() {
		return userType;
	}

	public void setUserType(Long userType) {
		this.userType = userType;
	}
	
	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}
	
	public String getWhiteImage() {
		return whiteImage;
	}

	public void setWhiteImage(String whiteImage) {
		this.whiteImage = whiteImage;
	}
	
	@Size(min=0, max=255, message="x_id长度不能超过 255 个字符")
	public String getXid() {
		return xid;
	}

	public void setXid(String xid) {
		this.xid = xid;
	}
	
	public Double getZkFinalPrice() {
		return zkFinalPrice;
	}

	public void setZkFinalPrice(Double zkFinalPrice) {
		this.zkFinalPrice = zkFinalPrice;
	}
	
	public Double getCoupon() {
		return coupon;
	}

	public void setCoupon(Double coupon) {
		this.coupon = coupon;
	}

	public Long getAuditStatus() {
		return auditStatus;
	}

	public void setAuditStatus(Long auditStatus) {
		this.auditStatus = auditStatus;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Long getSaleStatus() {
		return saleStatus;
	}

	public void setSaleStatus(Long saleStatus) {
		this.saleStatus = saleStatus;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getSaleStatusDate() {
		return saleStatusDate;
	}

	public void setSaleStatusDate(Date saleStatusDate) {
		this.saleStatusDate = saleStatusDate;
	}

	public Long getSyncMark() {
		return syncMark;
	}

	public void setSyncMark(Long syncMark) {
		this.syncMark = syncMark;
	}

	public void setId_in(Long[] ids) {
		this.sqlMap.getWhere().and("id", QueryType.IN, ids);
	}
}