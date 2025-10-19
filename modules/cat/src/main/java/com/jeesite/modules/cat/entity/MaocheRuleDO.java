package com.jeesite.modules.cat.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 操作日志表Entity
 * @author YHQ
 * @version 2023-10-21
 */
@Table(name="maoche_rule", alias="a", label="操作日志表信息", columns={
		@Column(name="id", attrName="id", label="编号", isPK=true),
		@Column(name="key_name", attrName="keyName", label="操作类型"),
		@Column(name="content", attrName="content", label="业务类型"),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheRuleDO extends DataEntity<MaocheRuleDO> {

	private static final long serialVersionUID = 1L;
	private String keyName;		// 操作类型
	private String content;		// 业务类型

	public MaocheRuleDO() {
		this(null);
	}

	public MaocheRuleDO(String id){
		super(id);
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}