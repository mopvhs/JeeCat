package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import com.jeesite.common.mybatis.annotation.JoinTable;
import com.jeesite.common.mybatis.annotation.JoinTable.Type;
import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.NotNull;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 企微配置详情数据Entity
 * @author YHQ
 * @version 2023-06-21
 */
@Table(name="qw_config_info", alias="a", label="企微配置详情数据信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="unique_id", attrName="uniqueId", label="exe唯一标识售卖订单号"),
		@Column(name="machine_id", attrName="machineId", label="绑定机器号 一个exe绑定一个机器"),
		@Column(name="client_config", attrName="clientConfig", label="客户端配置"),
		@Column(name="client_account_info", attrName="clientAccountInfo", label="客户端登录账号信息存储"),
		@Column(name="remark", attrName="remark", label="备注"),
		@Column(name="create_time", attrName="createTime", label="create_time"),
		@Column(name="update_time", attrName="updateTime", label="update_time"),
	}, orderBy="a.id DESC"
)
public class QwConfigInfoDO extends DataEntity<QwConfigInfoDO> {
	
	private static final long serialVersionUID = 1L;
	private String uniqueId;		// exe唯一标识售卖订单号
	private String machineId;		// 绑定机器号 一个exe绑定一个机器
	private String clientConfig;		// 客户端配置
	private String clientAccountInfo;		// 客户端登录账号信息存储
	private String remark;		// 备注
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

	public QwConfigInfoDO() {
		this(null);
	}
	
	public QwConfigInfoDO(String id){
		super(id);
	}
	
	@NotBlank(message="exe唯一标识售卖订单号不能为空")
	@Size(min=0, max=256, message="exe唯一标识售卖订单号长度不能超过 256 个字符")
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	@NotBlank(message="绑定机器号 一个exe绑定一个机器不能为空")
	@Size(min=0, max=256, message="绑定机器号 一个exe绑定一个机器长度不能超过 256 个字符")
	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	
	@NotBlank(message="客户端配置不能为空")
	@Size(min=0, max=4096, message="客户端配置长度不能超过 4096 个字符")
	public String getClientConfig() {
		return clientConfig;
	}

	public void setClientConfig(String clientConfig) {
		this.clientConfig = clientConfig;
	}
	
	@NotBlank(message="客户端登录账号信息存储不能为空")
	@Size(min=0, max=8192, message="客户端登录账号信息存储长度不能超过 8192 个字符")
	public String getClientAccountInfo() {
		return clientAccountInfo;
	}

	public void setClientAccountInfo(String clientAccountInfo) {
		this.clientAccountInfo = clientAccountInfo;
	}
	
	@NotBlank(message="备注不能为空")
	@Size(min=0, max=2048, message="备注长度不能超过 2048 个字符")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="create_time不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="update_time不能为空")
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}