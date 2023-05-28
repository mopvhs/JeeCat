package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import com.jeesite.common.mybatis.annotation.JoinTable;
import com.jeesite.common.mybatis.annotation.JoinTable.Type;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 主动发布任务Entity
 * @author YHQ
 * @version 2023-05-28
 */
@Table(name="maoche_sender_task", alias="a", label="主动发布任务信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="robot_id", attrName="robotId", label="maoche_robot_info.id"),
		@Column(name="chatroom_id", attrName="chatroomId", label="maoche_chatroom_info.id"),
		@Column(name="content_type", attrName="contentType", label="1 普通消息 2 文章消息"),
		@Column(name="status", attrName="status", label="0 未启动 1启动", isUpdate=false),
		@Column(name="content_json", attrName="contentJson", label="需要发送的内容"),
		@Column(name="next_execute_time", attrName="nextExecuteTime", label="下次执行时间"),
		@Column(name="interval", attrName="interval", label="间隔时间"),
		@Column(name="create_time", attrName="createTime", label="create_time"),
		@Column(name="update_time", attrName="updateTime", label="update_time"),
	}, orderBy="a.id DESC"
)
public class MaocheSenderTaskDO extends DataEntity<MaocheSenderTaskDO> {
	
	private static final long serialVersionUID = 1L;
	private Long iid;
	private Long robotId;		// maoche_robot_info.id
	private Long chatroomId;		// maoche_chatroom_info.id
	private Long contentType;		// 1 普通消息 2 文章消息
	private String contentJson;		// 需要发送的内容
	private Long nextExecuteTime;		// 下次执行时间
	private Long interval;		// 间隔时间
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

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

	public MaocheSenderTaskDO() {
		this(null);
	}
	
	public MaocheSenderTaskDO(String id){
		super(id);
	}
	
	public Long getRobotId() {
		return robotId;
	}

	public void setRobotId(Long robotId) {
		this.robotId = robotId;
	}
	
	public Long getChatroomId() {
		return chatroomId;
	}

	public void setChatroomId(Long chatroomId) {
		this.chatroomId = chatroomId;
	}
	
	@NotNull(message="1 普通消息 2 文章消息不能为空")
	public Long getContentType() {
		return contentType;
	}

	public void setContentType(Long contentType) {
		this.contentType = contentType;
	}
	
	@NotBlank(message="需要发送的内容不能为空")
	@Size(min=0, max=2048, message="需要发送的内容长度不能超过 2048 个字符")
	public String getContentJson() {
		return contentJson;
	}

	public void setContentJson(String contentJson) {
		this.contentJson = contentJson;
	}
	
	@NotNull(message="下次执行时间不能为空")
	public Long getNextExecuteTime() {
		return nextExecuteTime;
	}

	public void setNextExecuteTime(Long nextExecuteTime) {
		this.nextExecuteTime = nextExecuteTime;
	}
	
	@NotNull(message="间隔时间不能为空")
	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
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