package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 品牌库关键词Entity
 * @author YHQ
 * @version 2024-08-18
 */
@Table(name="maoche_brand_lib_keyword", alias="a", label="品牌库关键词信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="brand_lib_id", attrName="brandLibId", label="品牌库id"),
		@Column(name="keyword", attrName="keyword", label="关键词"),
		@Column(name="category_name", attrName="categoryName", label="子类目", queryType=QueryType.LIKE),
		@Column(name="level_one_category_name", attrName="levelOneCategoryName", label="父类目", queryType=QueryType.LIKE),
		@Column(name="alias_names", attrName="aliasNames", label="别名", queryType=QueryType.LIKE),
		@Column(name="tags", attrName="tags", label="标签id"),
		@Column(name="special_tags", attrName="specialTags", label="标签id"),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheBrandLibKeywordDO extends DataEntity<MaocheBrandLibKeywordDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private Long brandLibId;		// 品牌库id
	private String keyword;		// 关键词
	private String categoryName;		// 子类目
	private String levelOneCategoryName;		// 父类目
	private String aliasNames;		// 别名
	private String tags;		// 标签id
	private String specialTags;		// 标签id

	public MaocheBrandLibKeywordDO() {
		this(null);
	}
	
	public MaocheBrandLibKeywordDO(String id){
		super(id);
	}

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
	@NotNull(message="品牌库id不能为空")
	public Long getBrandLibId() {
		return brandLibId;
	}

	public void setBrandLibId(Long brandLibId) {
		this.brandLibId = brandLibId;
	}
	
	@Size(min=0, max=512, message="关键词长度不能超过 512 个字符")
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	@Size(min=0, max=1024, message="子类目长度不能超过 1024 个字符")
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	@Size(min=0, max=1024, message="父类目长度不能超过 1024 个字符")
	public String getLevelOneCategoryName() {
		return levelOneCategoryName;
	}

	public void setLevelOneCategoryName(String levelOneCategoryName) {
		this.levelOneCategoryName = levelOneCategoryName;
	}
	
	@Size(min=0, max=1024, message="别名长度不能超过 1024 个字符")
	public String getAliasNames() {
		return aliasNames;
	}

	public void setAliasNames(String aliasNames) {
		this.aliasNames = aliasNames;
	}
	
	@Size(min=0, max=2048, message="标签id长度不能超过 2048 个字符")
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	@Size(min=0, max=1024, message="标签id长度不能超过 1024 个字符")
	public String getSpecialTags() {
		return specialTags;
	}

	public void setSpecialTags(String specialTags) {
		this.specialTags = specialTags;
	}
	
}