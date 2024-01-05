package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.checkerframework.checker.units.qual.C;

import java.util.Date;

/**
 * 任务Entity
 * @author YHQ
 * @version 2023-08-04
 */
@Table(name="maoche_task", alias="a", label="任务信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="title", attrName="title", label="标题", queryType=QueryType.LIKE),
		@Column(name="sub_title", attrName="subTitle", label="副标题", queryType=QueryType.LIKE),
		@Column(name="task_type", attrName="taskType", label="任务类型"),
		@Column(includeEntity=DataEntity.class),
		@Column(name="task_switch", attrName="taskSwitch", label="是否开启"),
		@Column(name="switch_date", attrName="switchDate", label="开关时间"),
		@Column(name="content", attrName="content", label="内容"),
		@Column(name="time_type", attrName="timeType", label="时间类型"),
		@Column(name="publish_date", attrName="publishDate", label="发布时间"),
		@Column(name="finished_date", attrName="finishedDate", label="完成时间"),
	}, orderBy="a.update_date DESC"
)
public class MaocheTaskDO extends DataEntity<MaocheTaskDO> {
	
	private static final long serialVersionUID = 1L;
	private String title;		// 标题
	private String subTitle;		// 副标题
	private String taskType;		// 任务类型
	private String taskSwitch;		// 是否开启

	private String timeType;		// 时间类型

	private Date switchDate;		// 开关时间
	private String content;		// 内容

	private Date publishDate;	// 发布时间
	private Date finishedDate;	// 发布时间

	public MaocheTaskDO() {
		this(null);
	}
	
	public MaocheTaskDO(String id){
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
	
	@NotBlank(message="任务类型不能为空")
	@Size(min=0, max=32, message="任务类型长度不能超过 32 个字符")
	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	
	@NotBlank(message="是否开启不能为空")
	@Size(min=0, max=32, message="是否开启长度不能超过 32 个字符")
	public String getTaskSwitch() {
		return taskSwitch;
	}

	public void setTaskSwitch(String taskSwitch) {
		this.taskSwitch = taskSwitch;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getSwitchDate() {
		return switchDate;
	}

	public void setSwitchDate(Date switchDate) {
		this.switchDate = switchDate;
	}

	public String getTimeType() {
		return timeType;
	}

	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public Date getFinishedDate() {
		return finishedDate;
	}

	public void setFinishedDate(Date finishedDate) {
		this.finishedDate = finishedDate;
	}
}