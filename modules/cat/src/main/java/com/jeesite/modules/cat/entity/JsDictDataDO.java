package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.Extend;
import java.util.Date;
import com.jeesite.common.mybatis.annotation.JoinTable;
import com.jeesite.common.mybatis.annotation.JoinTable.Type;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.entity.TreeEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 字典数据表Entity
 * @author YHQ
 * @version 2024-01-12
 */
@Table(name="${_prefix}sys_dict_data", alias="a", label="字典数据表信息", columns={
		@Column(name="dict_code", attrName="dictCode", label="字典编码", isPK=true),
		@Column(includeEntity=TreeEntity.class),
		@Column(name="dict_label", attrName="dictLabel", label="字典标签"),
		@Column(name="dict_value", attrName="dictValue", label="字典键值"),
		@Column(name="dict_icon", attrName="dictIcon", label="字典图标"),
		@Column(name="dict_type", attrName="dictType", label="字典类型"),
		@Column(name="is_sys", attrName="isSys", label="系统内置", comment="系统内置（1是 0否）"),
		@Column(name="description", attrName="description", label="字典描述"),
		@Column(name="css_style", attrName="cssStyle", label="css样式", comment="css样式（如：color:red)"),
		@Column(name="css_class", attrName="cssClass", label="css类名", comment="css类名（如：red）"),
		@Column(includeEntity=DataEntity.class),
		@Column(includeEntity=BaseEntity.class),
		@Column(includeEntity=Extend.class, attrName="extend"),
	}, orderBy="a.tree_sorts, a.dict_code"
)
public class JsDictDataDO extends TreeEntity<JsDictDataDO> {
	
	private static final long serialVersionUID = 1L;
	private String dictCode;		// 字典编码
	private String dictLabel;		// 字典标签
	private String dictValue;		// 字典键值
	private String dictIcon;		// 字典图标
	private String dictType;		// 字典类型
	private String isSys;		// 系统内置（1是 0否）
	private String description;		// 字典描述
	private String cssStyle;		// css样式（如：color:red)
	private String cssClass;		// css类名（如：red）
	private Extend extend;		// 扩展字段

	public JsDictDataDO() {
		this(null);
	}
	
	public JsDictDataDO(String id){
		super(id);
	}
	
	@Override
	public JsDictDataDO getParent() {
		return parent;
	}

	@Override
	public void setParent(JsDictDataDO parent) {
		this.parent = parent;
	}
	
	public String getDictCode() {
		return dictCode;
	}

	public void setDictCode(String dictCode) {
		this.dictCode = dictCode;
	}
	
	@NotBlank(message="字典标签不能为空")
	@Size(min=0, max=100, message="字典标签长度不能超过 100 个字符")
	public String getDictLabel() {
		return dictLabel;
	}

	public void setDictLabel(String dictLabel) {
		this.dictLabel = dictLabel;
	}
	
	@NotBlank(message="字典键值不能为空")
	@Size(min=0, max=100, message="字典键值长度不能超过 100 个字符")
	public String getDictValue() {
		return dictValue;
	}

	public void setDictValue(String dictValue) {
		this.dictValue = dictValue;
	}
	
	@Size(min=0, max=100, message="字典图标长度不能超过 100 个字符")
	public String getDictIcon() {
		return dictIcon;
	}

	public void setDictIcon(String dictIcon) {
		this.dictIcon = dictIcon;
	}
	
	@NotBlank(message="字典类型不能为空")
	@Size(min=0, max=100, message="字典类型长度不能超过 100 个字符")
	public String getDictType() {
		return dictType;
	}

	public void setDictType(String dictType) {
		this.dictType = dictType;
	}
	
	@NotBlank(message="系统内置不能为空")
	@Size(min=0, max=1, message="系统内置长度不能超过 1 个字符")
	public String getIsSys() {
		return isSys;
	}

	public void setIsSys(String isSys) {
		this.isSys = isSys;
	}
	
	@Size(min=0, max=500, message="字典描述长度不能超过 500 个字符")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Size(min=0, max=500, message="css样式长度不能超过 500 个字符")
	public String getCssStyle() {
		return cssStyle;
	}

	public void setCssStyle(String cssStyle) {
		this.cssStyle = cssStyle;
	}
	
	@Size(min=0, max=500, message="css类名长度不能超过 500 个字符")
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	
	public Extend getExtend() {
		return extend;
	}

	public void setExtend(Extend extend) {
		this.extend = extend;
	}
	
}