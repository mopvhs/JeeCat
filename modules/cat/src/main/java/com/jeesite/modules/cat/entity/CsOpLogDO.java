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
@Table(name="cs_op_log", alias="a", label="操作日志表信息", columns={
		@Column(name="id", attrName="id", label="编号", isPK=true),
		@Column(name="op_type", attrName="opType", label="操作类型"),
		@Column(name="biz_type", attrName="bizType", label="业务类型"),
		@Column(name="describe", attrName="describe", label="日志标题"),
		@Column(name="resource_id", attrName="resourceId", label="业务主键"),
		@Column(name="resource_type", attrName="resourceType", label="业务主键"),
		@Column(name="origion_content", attrName="origionContent", label="正常、冻结等"),
		@Column(name="change_content", attrName="changeContent", label="正常、冻结等"),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class CsOpLogDO extends DataEntity<CsOpLogDO> {
	
	private static final long serialVersionUID = 1L;
	private String opType;		// 操作类型
	private String bizType;		// 业务类型
	private String describe;		// 日志标题
	private String resourceId;		// 业务主键
	private String resourceType;		// 业务主键
	private String origionContent;		// 正常、冻结等
	private String changeContent;		// 正常、冻结等

	public CsOpLogDO() {
		this(null);
	}
	
	public CsOpLogDO(String id){
		super(id);
	}
	
	@NotBlank(message="操作类型不能为空")
	@Size(min=0, max=50, message="操作类型长度不能超过 50 个字符")
	public String getOpType() {
		return opType;
	}

	public void setOpType(String opType) {
		this.opType = opType;
	}
	
	@Size(min=0, max=64, message="业务类型长度不能超过 64 个字符")
	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	
	@NotBlank(message="日志标题不能为空")
	@Size(min=0, max=500, message="日志标题长度不能超过 500 个字符")
	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}
	
	@Size(min=0, max=64, message="业务主键长度不能超过 64 个字符")
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@Size(min=0, max=64, message="业务主键长度不能超过 64 个字符")
	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public String getOrigionContent() {
		return origionContent;
	}

	public void setOrigionContent(String origionContent) {
		this.origionContent = origionContent;
	}
	
	public String getChangeContent() {
		return changeContent;
	}

	public void setChangeContent(String changeContent) {
		this.changeContent = changeContent;
	}
	
}