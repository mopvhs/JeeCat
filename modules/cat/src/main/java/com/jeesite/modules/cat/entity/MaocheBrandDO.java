package com.jeesite.modules.cat.entity;

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
 * maoche_brandEntity
 * @author YHQ
 * @version 2024-08-19
 */
@Table(name="maoche_brand", alias="a", label="maoche_brand信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="name", attrName="name", label="name", queryType=QueryType.LIKE),
		@Column(name="create_time", attrName="createTime", label="create_time", isUpdateForce=true),
		@Column(name="update_time", attrName="updateTime", label="update_time", isUpdateForce=true),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheBrandDO extends DataEntity<MaocheBrandDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private String name;		// name
	private Date createTime;		// create_time
	private Date updateTime;		// update_time

	public MaocheBrandDO() {
		this(null);
	}
	
	public MaocheBrandDO(String id){
		super(id);
	}

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
	
	@Size(min=0, max=128, message="name长度不能超过 128 个字符")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}