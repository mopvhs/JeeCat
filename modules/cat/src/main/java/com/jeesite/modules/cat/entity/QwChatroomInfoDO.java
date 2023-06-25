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
 * 群组消息Entity
 * @author YHQ
 * @version 2023-06-23
 */
@Table(name="qw_chatroom_info", alias="a", label="群组消息信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="wxid", attrName="wxid", label="wxid"),
		@Column(name="room_chat_id", attrName="roomChatId", label="room_chat_id"),
		@Column(name="room_name", attrName="roomName", label="room_name", queryType=QueryType.LIKE),
		@Column(name="room_owner_id", attrName="roomOwnerId", label="room_owner_id"),
		@Column(name="room_create_time", attrName="roomCreateTime", label="room_create_time"),
		@Column(name="create_time", attrName="createTime", label="create_time"),
		@Column(name="update_time", attrName="updateTime", label="update_time"),
	}, orderBy="a.id DESC"
)
public class QwChatroomInfoDO extends DataEntity<QwChatroomInfoDO> {
	
	private static final long serialVersionUID = 1L;
	private String wxid;		// wxid
	private String roomChatId;		// room_chat_id
	private String roomName;		// room_name
	private String roomOwnerId;		// room_owner_id
	private String roomCreateTime;		// room_create_time
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

	public QwChatroomInfoDO() {
		this(null);
	}
	
	public QwChatroomInfoDO(String id){
		super(id);
	}
	
	@NotBlank(message="wxid不能为空")
	@Size(min=0, max=256, message="wxid长度不能超过 256 个字符")
	public String getWxid() {
		return wxid;
	}

	public void setWxid(String wxid) {
		this.wxid = wxid;
	}
	
	@NotBlank(message="room_chat_id不能为空")
	@Size(min=0, max=256, message="room_chat_id长度不能超过 256 个字符")
	public String getRoomChatId() {
		return roomChatId;
	}

	public void setRoomChatId(String roomChatId) {
		this.roomChatId = roomChatId;
	}
	
	@NotBlank(message="room_name不能为空")
	@Size(min=0, max=256, message="room_name长度不能超过 256 个字符")
	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	@NotBlank(message="room_owner_id不能为空")
	@Size(min=0, max=256, message="room_owner_id长度不能超过 256 个字符")
	public String getRoomOwnerId() {
		return roomOwnerId;
	}

	public void setRoomOwnerId(String roomOwnerId) {
		this.roomOwnerId = roomOwnerId;
	}
	
	@NotBlank(message="room_create_time不能为空")
	@Size(min=0, max=256, message="room_create_time长度不能超过 256 个字符")
	public String getRoomCreateTime() {
		return roomCreateTime;
	}

	public void setRoomCreateTime(String roomCreateTime) {
		this.roomCreateTime = roomCreateTime;
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