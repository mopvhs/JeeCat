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
 * 品牌库Entity
 * @author YHQ
 * @version 2024-08-18
 */
@Table(name="maoche_brand_lib", alias="a", label="品牌库信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="brand_id", attrName="brandId", label="品牌id"),
		@Column(name="product_name", attrName="productName", label="品名", queryType=QueryType.LIKE),
		@Column(name="alias_names", attrName="aliasNames", label="别名", queryType=QueryType.LIKE),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheBrandLibDO extends DataEntity<MaocheBrandLibDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private Long brandId;		// 品牌id
	private String productName;		// 品名
	private String aliasNames;		// 别名

	public MaocheBrandLibDO() {
		this(null);
	}
	
	public MaocheBrandLibDO(String id){
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
	@NotNull(message="品牌id不能为空")
	public Long getBrandId() {
		return brandId;
	}

	public void setBrandId(Long brandId) {
		this.brandId = brandId;
	}
	
	@Size(min=0, max=256, message="品名长度不能超过 256 个字符")
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	@Size(min=0, max=1024, message="别名长度不能超过 1024 个字符")
	public String getAliasNames() {
		return aliasNames;
	}

	public void setAliasNames(String aliasNames) {
		this.aliasNames = aliasNames;
	}
	
}