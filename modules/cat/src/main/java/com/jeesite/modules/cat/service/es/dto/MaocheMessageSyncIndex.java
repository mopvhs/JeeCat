package com.jeesite.modules.cat.service.es.dto;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MaocheMessageSyncIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = -5134709930129841343L;

    private Long id;		// id

    private Long robotMsgId;		// 机器人抓取消息id

    // 使用ik_max_word分词
    private String msg;		// 消息内容 口令信息采集 不会很长
    // 使用ngram分词
    private String msgNgram;		// 消息内容 口令信息采集 不会很长

    private Long wxTime;		// 微信time

    private String affType;		// jd / tb

    private List<String> resourceIds;		// 资源id

    private String status;

    private String uniqueHash;		// hash

    private Long createDate;        // 创建时间

    private List<String> categoryNames;

    private Long newProduct;        // 是否新品


    public static MaocheMessageSyncIndex toIndex(MaocheRobotCrawlerMessageSyncDO syncDO) {
        if (syncDO == null || syncDO.getUiid() == null || syncDO.getUiid() <= 0) {
            return null;
        }

        MaocheMessageSyncIndex index = new MaocheMessageSyncIndex();
        index.setId(syncDO.getUiid());
        index.setRobotMsgId(syncDO.getRobotMsgId());
        index.setMsg(syncDO.getMsg());
        index.setMsgNgram(syncDO.getMsg());
        index.setWxTime(syncDO.getWxTime().getTime());
        index.setAffType(syncDO.getAffType());
        index.setResourceIds(StringUtils.isNotBlank(syncDO.getResourceIds()) ? List.of(syncDO.getResourceIds().split(",")) : null);
        index.setUniqueHash(syncDO.getUniqueHash());
        index.setCreateDate(syncDO.getCreateDate().getTime());
        index.setStatus(syncDO.getStatus());
        String remarks = syncDO.getRemarks();
        JSONObject jsonObject = JsonUtils.toJsonObject(remarks);
        if (jsonObject != null && jsonObject.getInteger("newProduct") != null) {
            Integer newProductValue = jsonObject.getInteger("newProduct");
            index.setNewProduct(NumberUtils.toLong(String.valueOf(newProductValue)));
        }

        return index;
    }
}
