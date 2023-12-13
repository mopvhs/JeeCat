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
import com.jeesite.common.shiro.realms.IiIiIiiIiiii;

/**
 * 推送任务Entity
 * @author YHQ
 * @version 2023-08-04
 */
@Table(name="maoche_push_task", alias="a", label="推送任务信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="title", attrName="title", label="标题", queryType=QueryType.LIKE),
		@Column(name="sub_title", attrName="subTitle", label="副标题", queryType=QueryType.LIKE),
		@Column(name="task_id", attrName="taskId", label="任务id"),
		@Column(name="push_type", attrName="pushType", label="任务类型"),
		@Column(name="finished_date", attrName="finishedDate", label="创建时间"),
		@Column(name="publish_date", attrName="publishDate", label="创建时间"),
		@Column(name="resource_id", attrName="resourceId", label="资源id"),
		@Column(name="resource_type", attrName="resourceType", label="资源类型"),
		@Column(name="detail", attrName="detail", label="详情"),
//		@Column(name="item_snapshot", attrName="itemSnapshot", label="商品快照"),
		@Column(includeEntity=DataEntity.class),
		@Column(name="content", attrName="content", label="内容"),
	}, orderBy="a.id ASC"
)
public class MaochePushTaskDO extends DataEntity<MaochePushTaskDO> {
	
	private static final long serialVersionUID = 1L;
	private String title;		// 标题
	private String subTitle;		// 副标题
	private String taskId;		// 任务id
	private String pushType;		// 任务类型
	private Date finishedDate;		// 创建时间
	private Date publishDate;		// 创建时间
	private String resourceId;		// 资源id
	private String resourceType;		// 资源类型
	private String content;		// 内容

	private String detail;		// 详细内容

	private String itemSnapshot;		// 商品快照（京东的话会包含券）

	public MaochePushTaskDO() {
		this(null);
	}
	
	public MaochePushTaskDO(String id){
		super(id);
	}
	
	@NotBlank(message="标题不能为空")
	@Size(min=0, max=128, message="标题长度不能超过 128 个字符")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@NotBlank(message="副标题不能为空")
	@Size(min=0, max=128, message="副标题长度不能超过 128 个字符")
	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	
	@NotBlank(message="任务id不能为空")
	@Size(min=0, max=36, message="任务id长度不能超过 36 个字符")
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	@NotBlank(message="任务类型不能为空")
	@Size(min=0, max=32, message="任务类型长度不能超过 32 个字符")
	public String getPushType() {
		return pushType;
	}

	public void setPushType(String pushType) {
		this.pushType = pushType;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getFinishedDate() {
		return finishedDate;
	}

	public void setFinishedDate(Date finishedDate) {
		this.finishedDate = finishedDate;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	
	@NotBlank(message="资源id不能为空")
	@Size(min=0, max=128, message="资源id长度不能超过 128 个字符")
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@NotBlank(message="资源类型不能为空")
	@Size(min=0, max=32, message="资源类型长度不能超过 32 个字符")
	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getItemSnapshot() {
		return itemSnapshot;
	}

	public void setItemSnapshot(String itemSnapshot) {
		this.itemSnapshot = itemSnapshot;
	}

	public void setPublishDate_lte(Date date) {
		this.sqlMap.getWhere().and("publish_date", QueryType.LTE, date);
	}

	public void setTaskId_in(String[] taskIds) {
		this.sqlMap.getWhere().and("task_id", QueryType.IN, taskIds);
	}

}