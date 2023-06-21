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
 * @version 2023-06-21
 */
@Table(name="maoche_chatroom_info", alias="a", label="群组消息信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="wxid", attrName="wxid", label="wxid"),
		@Column(name="gid", attrName="gid", label="gid"),
		@Column(name="gname", attrName="gname", label="gname"),
		@Column(name="create_time", attrName="createTime", label="create_time"),
		@Column(name="update_time", attrName="updateTime", label="update_time"),
	}, orderBy="a.id DESC"
)
public class MaocheChatroomInfoDO extends DataEntity<MaocheChatroomInfoDO> {
	
	private static final long serialVersionUID = 1L;
	private String wxid;		// wxid
	private String gid;		// gid
	private String gname;		// gname
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

	public MaocheChatroomInfoDO() {
		this(null);
	}
	
	public MaocheChatroomInfoDO(String id){
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
	
	@NotBlank(message="gid不能为空")
	@Size(min=0, max=256, message="gid长度不能超过 256 个字符")
	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}
	
	@NotBlank(message="gname不能为空")
	@Size(min=0, max=256, message="gname长度不能超过 256 个字符")
	public String getGname() {
		return gname;
	}

	public void setGname(String gname) {
		this.gname = gname;
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