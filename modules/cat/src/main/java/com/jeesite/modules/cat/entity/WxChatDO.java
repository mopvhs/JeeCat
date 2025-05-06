package com.jeesite.modules.cat.entity;


import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;

@Data
@Table(name="tbl_chat", alias="a", label="企微配置详情数据信息", columns={
        @Column(name="id", attrName="iid", label="id", isPK=true),
        @Column(name="chat_wx_id", attrName="chatWxId", label="exe唯一标识售卖订单号"),
        @Column(name="name", attrName="name", label="绑定机器号 一个exe绑定一个机器"),
        @Column(name="avatar", attrName="avatar", label="客户端配置"),
        @Column(name="owner_wx_id", attrName="ownerWxId", label="客户端登录账号信息存储"),
        @Column(name="owner_wx_name", attrName="ownerWxName", label="备注"),
        @Column(name="total", attrName="total", label="备注"),
        @Column(name="chat_create_timestamp", attrName="chatCreateTimestamp", label="备注"),
}, orderBy="a.id DESC"
)
public class WxChatDO {

    private static final long serialVersionUID = 1L;

    private Long iid;

    private String chatWxId;

    private String name;

    private String avatar;

    private String ownerWxId;

    private String ownerWxName;

    private Integer total;

    private Integer status;

    private Long chatCreateTimestamp;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private String uniqueId;

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
    public String getId() {
        return ObjectUtils.toString(getIid());
    }

    /**
     * 重载默认方法，主键类型互转，方便操作
     */
    public void setId(String id) {
        setIid(StringUtils.isNotBlank(id) ? NumberUtils.toLong(id) : null);
    }

    public String getChatWxId() {
        return chatWxId;
    }

    public void setChatWxId(String chatWxId) {
        this.chatWxId = chatWxId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOwnerWxId() {
        return ownerWxId;
    }

    public void setOwnerWxId(String ownerWxId) {
        this.ownerWxId = ownerWxId;
    }

    public String getOwnerWxName() {
        return ownerWxName;
    }

    public void setOwnerWxName(String ownerWxName) {
        this.ownerWxName = ownerWxName;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getChatCreateTimestamp() {
        return chatCreateTimestamp;
    }

    public void setChatCreateTimestamp(Long chatCreateTimestamp) {
        this.chatCreateTimestamp = chatCreateTimestamp;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
