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
 * maoche_push_task_ruleEntity
 * @author YHQ
 * @version 2023-10-28
 */
@Table(name="maoche_push_task_rule", alias="a", label="maoche_push_task_rule信息", columns={
		@Column(name="id", attrName="uiid", label="id", isPK=true),
		@Column(name="brand", attrName="brand", label="brand"),
		@Column(name="english_brand", attrName="englishBrand", label="brand"),
		@Column(name="product_name", attrName="productName", label="product_name", queryType=QueryType.LIKE),
		@Column(name="keyword", attrName="keyword", label="keyword"),
		@Column(name="category_id", attrName="categoryId", label="category_id", isUpdateForce=true),
		@Column(name="category_name", attrName="categoryName", label="category_name", queryType=QueryType.LIKE),
		@Column(name="level_one_category_id", attrName="levelOneCategoryId", label="level_one_category_id", isUpdateForce=true),
		@Column(name="level_one_category_name", attrName="levelOneCategoryName", label="level_one_category_name", queryType=QueryType.LIKE),
		@Column(name="star", attrName="star", label="star", isUpdateForce=true),
		@Column(name="description", attrName="description", label="description"),
		@Column(name="polling", attrName="polling", label="polling", isUpdateForce=true),
		@Column(name="specifications", attrName="specifications", label="specifications"),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaochePushTaskRuleDO extends DataEntity<MaochePushTaskRuleDO> {
	
	private static final long serialVersionUID = 1L;

	private Long uiid;
	private String brand;		// brand
	private String productName;		// product_name

	private String englishBrand;		// englishBrand
	private String keyword;		// keyword
	private Long categoryId;		// category_id
	private String categoryName;		// category_name
	private Long levelOneCategoryId;		// level_one_category_id
	private String levelOneCategoryName;		// level_one_category_name
	private Long star;		// star
	private String description;		// description
	private Long polling;		// polling
	private String specifications;		// specifications

	public MaochePushTaskRuleDO() {
		this(null);
	}
	
	public MaochePushTaskRuleDO(String id){
		super(id);
	}

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
	
	@Size(min=0, max=128, message="brand长度不能超过 128 个字符")
	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}
	
	@Size(min=0, max=255, message="product_name长度不能超过 255 个字符")
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	@Size(min=0, max=255, message="keyword长度不能超过 255 个字符")
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
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
	
	public Long getStar() {
		return star;
	}

	public void setStar(Long star) {
		this.star = star;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Long getPolling() {
		return polling;
	}

	public void setPolling(Long polling) {
		this.polling = polling;
	}
	
	public String getSpecifications() {
		return specifications;
	}

	public void setSpecifications(String specifications) {
		this.specifications = specifications;
	}

	public String getEnglishBrand() {
		return englishBrand;
	}

	public void setEnglishBrand(String englishBrand) {
		this.englishBrand = englishBrand;
	}
}