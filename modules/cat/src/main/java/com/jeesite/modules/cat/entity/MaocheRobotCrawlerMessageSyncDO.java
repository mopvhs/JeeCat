package com.jeesite.modules.cat.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.collect.MapUtils;
import com.jeesite.common.mybatis.annotation.JoinTable;
import com.jeesite.common.mybatis.annotation.JoinTable.Type;
import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.service.toolbox.dto.CommandContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 信息采集表Entity
 * @author YHQ
 * @version 2023-11-01
 */
@Slf4j
@Table(name="maoche_robot_crawler_message_sync", alias="a", label="信息采集表信息", columns={
		@Column(name="id", attrName="uiid", label="id", isPK=true),
		@Column(name="robot_msg_id", attrName="robotMsgId", label="机器人抓取消息id"),
		@Column(name="msg", attrName="msg", label="消息内容 口令信息采集 不会很长"),
		@Column(name="wx_time", attrName="wxTime", label="微信time"),
		@Column(name="processed", attrName="processed", label="0 未处理 1 已处理", isUpdateForce=true),
		@Column(name="aff_type", attrName="affType", label="jd / tb"),
		@Column(name="resource_ids", attrName="resourceIds", label="资源id"),
		@Column(name="unique_hash", attrName="uniqueHash", label="hash"),
		@Column(name="ori_unique_hash", attrName="oriUniqueHash", label="hash"),
		@Column(includeEntity=DataEntity.class),
}, orderBy="a.update_date DESC"
)
public class MaocheRobotCrawlerMessageSyncDO extends DataEntity<MaocheRobotCrawlerMessageSyncDO> {

	private static final long serialVersionUID = 1L;

	private Long uiid;        // uiid
	private Long robotMsgId;        // 机器人抓取消息id
	private String msg;        // 消息内容 口令信息采集 不会很长
	private Date wxTime;        // 微信time
	private Long processed;        // 0 未处理 1 已处理
	private String affType;        // jd / tb
	private String resourceIds;        // 资源id
	private String uniqueHash;        // hash-md5
	private String oriUniqueHash;        // hash-md5

	public MaocheRobotCrawlerMessageSyncDO() {
		this(null);
	}

	public MaocheRobotCrawlerMessageSyncDO(String id){
		super(id);
	}

	/**
	 * 重载默认方法，主键类型互转，方便操作
	 * 如果需要在 insert 后返回 自增ID，请设置 mybatis-config.xml 的 useGeneratedKeys="true"
	 */
	@Override
	public String getId() {
		return ObjectUtils.toString(getUiid());
	}

	/**
	 * 重载默认方法，主键类型互转，方便操作
	 */
	@Override
	public void setId(String id) {
		setUiid(StringUtils.isNotBlank(id) ? NumberUtils.toLong(id) : null);
	}

	public Long getUiid() {
		return uiid;
	}

	public void setUiid(Long uiid) {
		this.uiid = uiid;
	}

	@NotNull(message="机器人抓取消息id不能为空")
	public Long getRobotMsgId() {
		return robotMsgId;
	}

	public void setRobotMsgId(Long robotMsgId) {
		this.robotMsgId = robotMsgId;
	}

	@NotBlank(message="消息内容 口令信息采集 不会很长不能为空")
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="微信time不能为空")
	public Date getWxTime() {
		return wxTime;
	}

	public void setWxTime(Date wxTime) {
		this.wxTime = wxTime;
	}

	public Long getProcessed() {
		return processed;
	}

	public void setProcessed(Long processed) {
		this.processed = processed;
	}

	@Size(min=0, max=128, message="jd / tb长度不能超过 128 个字符")
	public String getAffType() {
		return affType;
	}

	public void setAffType(String affType) {
		this.affType = affType;
	}

	@NotBlank(message="资源id不能为空")
	@Size(min=0, max=2048, message="资源id长度不能超过 2048 个字符")
	public String getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}

	@Size(min=0, max=128, message="hash长度不能超过 128 个字符")
	public String getUniqueHash() {
		return uniqueHash;
	}

	public void setUniqueHash(String uniqueHash) {
		this.uniqueHash = uniqueHash;
	}

	public String getOriUniqueHash() {
		return oriUniqueHash;
	}

	public void setOriUniqueHash(String oriUniqueHash) {
		this.oriUniqueHash = oriUniqueHash;
	}

	public void addCommandContext(CommandContext commandContext) {
		addRemarks("commandContext", commandContext);
	}

	public void addNewProduct(int newProduct) {
		addRemarks("newProduct", newProduct);
	}

	public void addApiError(Map<String, Object> apiErrorMap) {
		addRemarks("apiErrorMap", apiErrorMap);
	}

	public void addRemarks(String key, Object data) {
		if (StringUtils.isBlank(key)) {
			return;
		}
		if (StringUtils.isBlank(this.remarks)) {
			Map<String, Object> map = new HashMap<>();
			map.put(key, data);
			this.remarks = JsonUtils.toJSONString(map);
		} else {
			try {
				// 反序列化remarks为Map
				Map<String, Object> map = JsonUtils.toReferenceType(this.remarks, new TypeReference<Map<String, Object>>() {
				});
				if (map == null) {
					map = new HashMap<>();
				}
				map.put(key, data);
				this.remarks = JsonUtils.toJSONString(map);
			} catch (Exception e) {
				// 异常
				Map<String, Object> map = new HashMap<>();
				map.put(key, data);
				map.put("exceptionRemarks", this.remarks);
				this.remarks = JsonUtils.toJSONString(map);
				log.error("反序列化remarks为Map异常", e);
			}
		}
	}

	public void setUiid_in(List<Long> ids) {
		this.sqlMap.getWhere().and("id", QueryType.IN, ids);
	}
}