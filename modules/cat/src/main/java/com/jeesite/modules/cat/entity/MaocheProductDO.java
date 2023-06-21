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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * maoche_productEntity
 * @author YHQ
 * @version 2023-06-16
 */
@Table(name="maoche_product", alias="a", label="maoche_product信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="content", attrName="content", label="原始信息"),
		@Column(name="item_id", attrName="itemId", label="item_id"),
		@Column(name="item_id_suffix", attrName="itemIdSuffix", label="item_id_suffix"),
		@Column(name="unique_hash", attrName="uniqueHash", label="unique_hash"),
		@Column(name="content_tk", attrName="contentTk", label="订单侠原始信息", comment="订单侠原始信息（淘客）"),
		@Column(name="content_new", attrName="contentNew", label="转链后的信息"),
		@Column(name="create_time", attrName="createTime", label="create_time", isUpdateForce=true),
		@Column(name="update_time", attrName="updateTime", label="update_time", isUpdateForce=true),
		@Column(name="sync_time", attrName="syncTime", label="sync_time", isUpdateForce=true),
		@Column(name="aff_link_conv_time", attrName="affLinkConvTime", label="转链时间", isUpdateForce=true),
		@Column(name="aff_type", attrName="affType", label="tb / jd"),
		@Column(name="status", attrName="status", label="NORMAL / DELETE", isUpdate=false),
		@Column(name="title", attrName="title", label="商品标题", queryType=QueryType.LIKE),
		@Column(name="image_url", attrName="imageUrl", label="图片地址"),
		@Column(name="processed", attrName="processed", label="0 未处理 / 1 已处理", isUpdateForce=true),
	}, orderBy="a.id DESC"
)
public class MaocheProductDO extends DataEntity<MaocheProductDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private String content;		// 原始信息
	private String itemId;		// item_id
	private String itemIdSuffix;		// item_id_suffix
	private String uniqueHash;		// unique_hash
	private String contentTk;		// 订单侠原始信息（淘客）
	private String contentNew;		// 转链后的信息
	private Date createTime;		// create_time
	private Date updateTime;		// update_time
	private Date syncTime;		// sync_time
	private Date affLinkConvTime;		// 转链时间
	private String affType;		// tb / jd
	private String title;		// 商品标题
	private String imageUrl;		// 图片地址
	private Long processed;		// 0 未处理 / 1 已处理

	public Long getIid() {
		return iid;
	}

	public void setIid(Long iid) {
		this.iid = iid;
	}

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
	public MaocheProductDO() {
		this(null);
	}
	
	public MaocheProductDO(String id){
		super(id);
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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
	
	@Size(min=0, max=128, message="unique_hash长度不能超过 128 个字符")
	public String getUniqueHash() {
		return uniqueHash;
	}

	public void setUniqueHash(String uniqueHash) {
		this.uniqueHash = uniqueHash;
	}
	
	public String getContentTk() {
		return contentTk;
	}

	public void setContentTk(String contentTk) {
		this.contentTk = contentTk;
	}
	
	public String getContentNew() {
		return contentNew;
	}

	public void setContentNew(String contentNew) {
		this.contentNew = contentNew;
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
	public Date getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getAffLinkConvTime() {
		return affLinkConvTime;
	}

	public void setAffLinkConvTime(Date affLinkConvTime) {
		this.affLinkConvTime = affLinkConvTime;
	}
	
	@Size(min=0, max=128, message="tb / jd长度不能超过 128 个字符")
	public String getAffType() {
		return affType;
	}

	public void setAffType(String affType) {
		this.affType = affType;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Size(min=0, max=1000, message="图片地址长度不能超过 1000 个字符")
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public Long getProcessed() {
		return processed;
	}

	public void setProcessed(Long processed) {
		this.processed = processed;
	}

	public void setIid_in(Long[] ids) {
		this.sqlMap.getWhere().and("id", QueryType.IN, ids);
	}

	public void setItemIdSuffix_in(String[] ids) {
		this.sqlMap.getWhere().and("item_id_suffix", QueryType.IN, ids);
	}
}