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
 * maoche_categoryEntity
 * @author YHQ
 * @version 2023-05-24
 */
@Table(name="maoche_category", alias="a", label="maoche_category信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="parent_id", attrName="parentId", label="parent_id", isUpdateForce=true),
		@Column(name="name", attrName="name", label="name", queryType=QueryType.LIKE),
		@Column(name="level", attrName="level", label="level", isUpdateForce=true),
	}, orderBy="a.id DESC"
)
public class MaocheCategoryDO extends DataEntity<MaocheCategoryDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private Long parentId;		// parent_id
	private String name;		// name
	private Long level;		// level

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

	public MaocheCategoryDO() {
		this(null);
	}
	
	public MaocheCategoryDO(String id){
		super(id);
	}
	
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	@Size(min=0, max=128, message="name长度不能超过 128 个字符")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Long getLevel() {
		return level;
	}

	public void setLevel(Long level) {
		this.level = level;
	}

	public void setItemIdSuffix_in(String[] ids) {
		this.sqlMap.getWhere().and("item_id_suffix", QueryType.IN, ids);
	}
	
}