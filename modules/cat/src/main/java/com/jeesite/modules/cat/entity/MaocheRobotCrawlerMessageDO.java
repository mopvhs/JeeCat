package com.jeesite.modules.cat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 信息采集表Entity
 * @author YHQ
 * @version 2023-04-30
 */
@Table(name="maoche_robot_crawler_message", alias="a", label="信息采集表信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="fromgid", attrName="fromgid", label="发消息的群标识"),
		@Column(name="fromid", attrName="fromid", label="发消息的用户标识"),
		@Column(name="toid", attrName="toid", label="收消息的用户标识"),
		@Column(name="msg", attrName="msg", label="消息内容 口令信息采集 不会很长"),
		@Column(name="msg_new", attrName="msgNew", label="msg_new"),
		@Column(name="image_url", attrName="imageUrl", label="image_url"),
		@Column(name="msgsvrid", attrName="msgsvrid", label="微信消息id"),
		@Column(name="fromtype", attrName="fromtype", label="微信fromtype"),
		@Column(name="msgtype", attrName="msgtype", label="微信msgtype"),
		@Column(name="time", attrName="time", label="微信time"),
		@Column(name="remark", attrName="remark", label="备用字段"),
		@Column(name="create_time", attrName="createTime", label="create_time"),
		@Column(name="update_time", attrName="updateTime", label="update_time"),
	}, orderBy="a.id DESC"
)
public class MaocheRobotCrawlerMessageDO extends DataEntity<MaocheRobotCrawlerMessageDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private String fromgid;		// 发消息的群标识
	private String fromid;		// 发消息的用户标识
	private String toid;		// 收消息的用户标识
	private String msg;		// 消息内容 口令信息采集 不会很长

	private String msgNew;

	private String imageUrl;
	private String msgsvrid;		// 微信消息id
	private String fromtype;		// 微信fromtype
	private String msgtype;		// 微信msgtype
	private String time;		// 微信time
	private String remark;		// 备用字段
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

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

	public Long getIid() {
		return iid;
	}

	public void setIid(Long iid) {
		this.iid = iid;
	}

	public MaocheRobotCrawlerMessageDO() {
		this(null);
	}
	
	public MaocheRobotCrawlerMessageDO(String id){
		super(id);
	}
	
	@NotBlank(message="发消息的群标识不能为空")
	@Size(min=0, max=256, message="发消息的群标识长度不能超过 256 个字符")
	public String getFromgid() {
		return fromgid;
	}

	public void setFromgid(String fromgid) {
		this.fromgid = fromgid;
	}
	
	@NotBlank(message="发消息的用户标识不能为空")
	@Size(min=0, max=256, message="发消息的用户标识长度不能超过 256 个字符")
	public String getFromid() {
		return fromid;
	}

	public void setFromid(String fromid) {
		this.fromid = fromid;
	}
	
	@NotBlank(message="收消息的用户标识不能为空")
	@Size(min=0, max=256, message="收消息的用户标识长度不能超过 256 个字符")
	public String getToid() {
		return toid;
	}

	public void setToid(String toid) {
		this.toid = toid;
	}
	
	@NotBlank(message="消息内容 口令信息采集 不会很长不能为空")
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	@NotBlank(message="微信消息id不能为空")
	@Size(min=0, max=256, message="微信消息id长度不能超过 256 个字符")
	public String getMsgsvrid() {
		return msgsvrid;
	}

	public void setMsgsvrid(String msgsvrid) {
		this.msgsvrid = msgsvrid;
	}
	
	@NotBlank(message="微信fromtype不能为空")
	@Size(min=0, max=256, message="微信fromtype长度不能超过 256 个字符")
	public String getFromtype() {
		return fromtype;
	}

	public void setFromtype(String fromtype) {
		this.fromtype = fromtype;
	}
	
	@NotBlank(message="微信msgtype不能为空")
	@Size(min=0, max=256, message="微信msgtype长度不能超过 256 个字符")
	public String getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}
	
	@NotBlank(message="微信time不能为空")
	@Size(min=0, max=256, message="微信time长度不能超过 256 个字符")
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	@NotBlank(message="备用字段不能为空")
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

	public String getMsgNew() {
		return msgNew;
	}

	public void setMsgNew(String msgNew) {
		this.msgNew = msgNew;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}