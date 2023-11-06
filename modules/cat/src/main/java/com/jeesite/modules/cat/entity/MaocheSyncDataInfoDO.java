package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 数据同步位点表Entity
 * @author YHQ
 * @version 2023-10-31
 */
@Table(name="maoche_sync_data_info", alias="a", label="数据同步位点表信息", columns={
		@Column(name="id", attrName="iid", label="id", isPK=true),
		@Column(name="sync_max_id", attrName="syncMaxId", label="sync_max_id"),
		@Column(name="step", attrName="step", label="step"),
		@Column(name="table_name", attrName="tableName", label="table_name"),
		@Column(name="sync_time", attrName="syncTime", label="sync_time"),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheSyncDataInfoDO extends DataEntity<MaocheSyncDataInfoDO> {
	
	private static final long serialVersionUID = 1L;

	private Long iid;
	private String syncMaxId;		// sync_max_id
	private Integer step;		// step
	private String tableName;		// table_name
	private Long syncTime;		// sync_time

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

	public MaocheSyncDataInfoDO() {
		this(null);
	}
	
	public MaocheSyncDataInfoDO(String id){
		super(id);
	}
	
	@NotBlank(message="sync_max_id不能为空")
	@Size(min=0, max=255, message="sync_max_id长度不能超过 255 个字符")
	public String getSyncMaxId() {
		return syncMaxId;
	}

	public void setSyncMaxId(String syncMaxId) {
		this.syncMaxId = syncMaxId;
	}
	
	@NotNull(message="step不能为空")
	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}
	
	@NotBlank(message="table_name不能为空")
	@Size(min=0, max=32, message="table_name长度不能超过 32 个字符")
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@NotNull(message="sync_time不能为空")
	public Long getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Long syncTime) {
		this.syncTime = syncTime;
	}
	
}