package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * maoche_tagEntity
 * @author YHQ
 * @version 2023-10-29
 */
@Table(name="maoche_tag", alias="a", label="maoche_tag信息", columns={
		@Column(name="id", attrName="iid", label="id", isPK=true),
		@Column(name="level", attrName="level", label="level"),
		@Column(name="parent_id", attrName="parentId", label="parent_id"),
		@Column(name="tag_name", attrName="tagName", label="tag_name", queryType=QueryType.LIKE),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheTagDO extends DataEntity<MaocheTagDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private Long level;		// level
	private Long parentId;		// parent_id
	private String tagName;		// tag_name


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


	public MaocheTagDO() {
		this(null);
	}
	
	public MaocheTagDO(String id){
		super(id);
	}
	
	@NotNull(message="level不能为空")
	public Long getLevel() {
		return level;
	}

	public void setLevel(Long level) {
		this.level = level;
	}
	
	@NotNull(message="parent_id不能为空")
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	@NotBlank(message="tag_name不能为空")
	@Size(min=0, max=255, message="tag_name长度不能超过 255 个字符")
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setIid_in(Long[] ids) {
		this.sqlMap.getWhere().and("id", QueryType.IN, ids);
	}

}