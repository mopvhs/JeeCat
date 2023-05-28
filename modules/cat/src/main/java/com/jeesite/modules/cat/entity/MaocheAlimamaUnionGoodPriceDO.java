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
 * maoche_alimama_union_good_priceEntity
 * @author YHQ
 * @version 2023-05-14
 */
@Table(name="maoche_alimama_union_good_price", alias="a", label="maoche_alimama_union_good_price信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="item_id", attrName="itemId", label="item_id"),
		@Column(name="item_id_suffix", attrName="itemIdSuffix", label="item_id_suffix"),
		@Column(name="keyword", attrName="keyword", label="keyword"),
		@Column(name="content", attrName="content", label="content"),
		@Column(name="create_time", attrName="createTime", label="create_time", isUpdateForce=true),
		@Column(name="update_time", attrName="updateTime", label="update_time", isUpdateForce=true),
	}, orderBy="a.id DESC"
)
public class MaocheAlimamaUnionGoodPriceDO extends DataEntity<MaocheAlimamaUnionGoodPriceDO> {
	
	private static final long serialVersionUID = 1L;
	private Long iid;
	private String itemId;		// item_id
	private String itemIdSuffix;
	private String keyword;		// keyword
	private String content;		// content
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

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

	public MaocheAlimamaUnionGoodPriceDO() {
		this(null);
	}
	
	public MaocheAlimamaUnionGoodPriceDO(String id){
		super(id);
	}
	
	@Size(min=0, max=128, message="item_id长度不能超过 128 个字符")
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

	@Size(min=0, max=128, message="keyword长度不能超过 128 个字符")
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public void setItemId_in(String[] itemIds) {
		this.sqlMap.getWhere().and("item_id", QueryType.IN, itemIds);
	}

	public void setItemIdSuffix_in(String[] itemIds) {
		this.sqlMap.getWhere().and("item_id_suffix", QueryType.IN, itemIds);
	}


}