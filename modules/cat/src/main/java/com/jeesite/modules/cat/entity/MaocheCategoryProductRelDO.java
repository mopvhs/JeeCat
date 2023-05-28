package com.jeesite.modules.cat.entity;

import javax.validation.constraints.Size;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * maoche_category_product_relEntity
 * @author YHQ
 * @version 2023-05-24
 */
@Table(name="maoche_category_product_rel", alias="a", label="maoche_category_product_rel信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="item_id", attrName="itemId", label="item_id"),
		@Column(name="item_id_suffix", attrName="itemIdSuffix", label="item_id_suffix"),
		@Column(name="category_id", attrName="categoryId", label="category_id", isUpdateForce=true),
	}, orderBy="a.id DESC"
)
public class MaocheCategoryProductRelDO extends DataEntity<MaocheCategoryProductRelDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private String itemId;		// item_id
	private String itemIdSuffix;		// item_id_suffix
	private Long categoryId;		// category_id

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

	public MaocheCategoryProductRelDO() {
		this(null);
	}
	
	public MaocheCategoryProductRelDO(String id){
		super(id);
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
	
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public void setItemIdSuffix_in(String[] ids) {
		this.sqlMap.getWhere().and("item_id_suffix", QueryType.IN, ids);
	}
	
}