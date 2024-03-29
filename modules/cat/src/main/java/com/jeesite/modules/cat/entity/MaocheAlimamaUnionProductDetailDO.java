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

/**
 * maoche_alimama_union_product_detailEntity
 * @author YHQ
 * @version 2023-05-28
 */
@Table(name="maoche_alimama_union_product_detail", alias="a", label="maoche_alimama_union_product_detail信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="iid", attrName="iid", label="iid"),
		@Column(name="item_id", attrName="itemId", label="item_id"),
		@Column(name="item_id_suffix", attrName="itemIdSuffix", label="item_id_suffix"),
		@Column(name="orig_content", attrName="origContent", label="orig_content"),


		@Column(name="props", attrName="props", label="props"),
		@Column(name="seller", attrName="seller", label="seller"),
		@Column(name="rate", attrName="rate", label="rate"),
		@Column(name="sku_base", attrName="skuBase", label="sku_base"),


		@Column(name="create_time", attrName="createTime", label="create_time", isUpdateForce=true),
		@Column(name="update_time", attrName="updateTime", label="update_time", isUpdateForce=true),
	}, orderBy="a.id DESC"
)
public class MaocheAlimamaUnionProductDetailDO extends DataEntity<MaocheAlimamaUnionProductDetailDO> {
	
	private static final long serialVersionUID = 1L;
	private String iid;		// iid
	private String itemId;		// item_id
	private String itemIdSuffix;		// item_id_suffix
	private String origContent;		// orig_content
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

	private String props;		// props
	private String seller;		// seller
	private String rate;		// rate
	private String skuBase;		// sku_base

	public MaocheAlimamaUnionProductDetailDO() {
		this(null);
	}
	
	public MaocheAlimamaUnionProductDetailDO(String id){
		super(id);
	}
	
	@Size(min=0, max=128, message="iid长度不能超过 128 个字符")
	public String getIid() {
		return iid;
	}

	public void setIid(String iid) {
		this.iid = iid;
	}
	
	@Size(min=0, max=128, message="item_id长度不能超过 128 个字符")
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	
	@Size(min=0, max=128, message="item_id_suffix长度不能超过 128 个字符")
	public String getItemIdSuffix() {
		return itemIdSuffix;
	}

	public void setItemIdSuffix(String itemIdSuffix) {
		this.itemIdSuffix = itemIdSuffix;
	}
	
	public String getOrigContent() {
		return origContent;
	}

	public void setOrigContent(String origContent) {
		this.origContent = origContent;
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

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getSkuBase() {
		return skuBase;
	}

	public void setSkuBase(String skuBase) {
		this.skuBase = skuBase;
	}

	public void setItemIdSuffix_in(String[] ids) {
		this.sqlMap.getWhere().and("item_id_suffix", QueryType.IN, ids);
	}

	public void setIid_in(String[] ids) {
		this.sqlMap.getWhere().and("iid", QueryType.IN, ids);
	}

}