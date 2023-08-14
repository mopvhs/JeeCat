package com.jeesite.modules.cat.entity;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import com.jeesite.common.mybatis.annotation.JoinTable;
import com.jeesite.common.mybatis.annotation.JoinTable.Type;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.mybatis.mapper.query.QueryWhere;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * maoche_alimama_union_product_bihaohuoEntity
 * @author YHQ
 * @version 2023-07-22
 */
@Table(name="maoche_alimama_union_product_bihaohuo", alias="a", label="maoche_alimama_union_product_bihaohuo信息", columns={
		@Column(name="id", attrName="uiid", label="id", isPK=true),
		@Column(name="product_id", attrName="productId", label="product_id", isUpdateForce=true),
		@Column(name="iid", attrName="iid", label="iid"),
		@Column(name="item_id", attrName="itemId", label="item_id"),
		@Column(name="item_id_suffix", attrName="itemIdSuffix", label="item_id_suffix"),
		@Column(name="orig_content", attrName="origContent", label="orig_content"),
		@Column(name="create_time", attrName="createTime", label="create_time", isUpdateForce=true),
		@Column(name="update_time", attrName="updateTime", label="update_time", isUpdateForce=true),
		@Column(name="last_sync_time", attrName="lastSyncTime", label="last_sync_time", isUpdateForce=true),
		@Column(name="sync_date", attrName="syncDate", label="sync_date", isUpdateForce=true),
	}, orderBy="a.id DESC"
)
public class MaocheAlimamaUnionProductBihaohuoDO extends DataEntity<MaocheAlimamaUnionProductBihaohuoDO> {
	
	private static final long serialVersionUID = 1L;

	private Long uiid;	// 数值主键id
	private Long productId;		// product_id
	private String iid;		// iid
	private String itemId;		// item_id
	private String itemIdSuffix;		// item_id_suffix
	private String origContent;		// orig_content
	private Date createTime;		// create_time
	private Date updateTime;		// update_time
	private Date lastSyncTime;		// last_sync_time
	private Date syncDate;		// sync_date

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

	public MaocheAlimamaUnionProductBihaohuoDO() {
		this(null);
	}
	
	public MaocheAlimamaUnionProductBihaohuoDO(String id){
		super(id);
	}
	
	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
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
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getLastSyncTime() {
		return lastSyncTime;
	}

	public void setLastSyncTime(Date lastSyncTime) {
		this.lastSyncTime = lastSyncTime;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getSyncDate() {
		return syncDate;
	}

	public void setSyncDate(Date syncDate) {
		this.syncDate = syncDate;
	}

	public void setUiid_in(List<Long> ids) {
		this.sqlMap.getWhere().and("id", QueryType.IN, ids);
	}
	
}