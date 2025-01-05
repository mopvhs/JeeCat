package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 订阅表Entity
 * @author YhQ
 * @version 2024-11-30
 */
@Table(name="maoche_subscribe", alias="a", label="订阅表信息", columns={
		@Column(name="id", attrName="id", label="id", isPK=true),
		@Column(name="user_id", attrName="userId", label="用户csaas.user.id"),
		@Column(name="subscribe_id", attrName="subscribeId", label="订阅id，品牌库id"),
		@Column(name="subscribe_type", attrName="subscribeType", label="订阅类型"),
		@Column(name="open_switch", attrName="openSwitch", label="是否开启"),
		@Column(name="category_name", attrName="categoryName", label="子类目", queryType=QueryType.LIKE),
		@Column(name="level_one_category_name", attrName="levelOneCategoryName", label="父类目", queryType=QueryType.LIKE),
		@Column(name="cid1", attrName="cid1", label="一级类目", isUpdateForce=true),
		@Column(name="cid2", attrName="cid2", label="二级类目", isUpdateForce=true),
		@Column(includeEntity=DataEntity.class),
	}, orderBy="a.update_date DESC"
)
public class MaocheSubscribeDO extends DataEntity<MaocheSubscribeDO> {
	
	private static final long serialVersionUID = 1L;
	private String userId;		// 用户csaas.user.id
	private String subscribeId;		// 订阅id，品牌库id
	private String subscribeType;		// 订阅类型
	private String openSwitch;		// 是否开启
	private Long cid1;		// 一级类目
	private Long cid2;		// 二级类目

	private String categoryName;

	private String levelOneCategoryName;

	public MaocheSubscribeDO() {
		this(null);
	}
	
	public MaocheSubscribeDO(String id){
		super(id);
	}
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@NotBlank(message="订阅id，品牌库id不能为空")
	@Size(min=0, max=128, message="订阅id，品牌库id长度不能超过 128 个字符")
	public String getSubscribeId() {
		return subscribeId;
	}

	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}
	
	@NotBlank(message="订阅类型不能为空")
	@Size(min=0, max=36, message="订阅类型长度不能超过 36 个字符")
	public String getSubscribeType() {
		return subscribeType;
	}

	public void setSubscribeType(String subscribeType) {
		this.subscribeType = subscribeType;
	}
	
	@NotBlank(message="是否开启不能为空")
	@Size(min=0, max=32, message="是否开启长度不能超过 32 个字符")
	public String getOpenSwitch() {
		return openSwitch;
	}

	public void setOpenSwitch(String openSwitch) {
		this.openSwitch = openSwitch;
	}
	
	public Long getCid1() {
		return cid1;
	}

	public void setCid1(Long cid1) {
		this.cid1 = cid1;
	}
	
	public Long getCid2() {
		return cid2;
	}

	public void setCid2(Long cid2) {
		this.cid2 = cid2;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getLevelOneCategoryName() {
		return levelOneCategoryName;
	}

	public void setLevelOneCategoryName(String levelOneCategoryName) {
		this.levelOneCategoryName = levelOneCategoryName;
	}
}