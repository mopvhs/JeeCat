package com.jeesite.modules.cat.service.es.dto;

import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MaocheMessageProductIndex implements Serializable {

    @Serial
    private static final long serialVersionUID = 396490561080255399L;

    private Long id;        // id

    private Long robotMsgId;        // 机器人抓取消息id

    private Long msgId;        // 机器人抓取消息的sync表的id

    private String affType;        // jd / tb

    private String resourceId;        // 资源id

    private String innerId;        // 内部资源id

//    private String apiContent;		// 消息内容 口令信息采集 不会很长

    private String category;        // 类目

    private String pictUrl;        // 图片地址

    private String title;        // 标题

    private String shortTitle;        // 短标题

    private String shopDsr;        // 店铺分

    private Long commissionRate;    // 佣金比例

    private String shopName;        // 店铺名称

    private String sellerId;        // 商家id

    private Long price;        // 价格

    private Long volume;        // 销量

    private Long createDate;        // 创建时间

    private String status;          // 状态

    private String itemId;          // itemId
    private String itemUrl;          // itemId


    public static MaocheMessageProductIndex toIndex(MaocheRobotCrawlerMessageProductDO item) {
        if (item == null || item.getUiid() == null || item.getUiid() <= 0) {
            return null;
        }

        MaocheMessageProductIndex index = new MaocheMessageProductIndex();

        index.setId(item.getUiid());
        index.setRobotMsgId(item.getRobotMsgId());
        index.setMsgId(item.getMsgId());
        index.setAffType(item.getAffType());
        index.setResourceId(item.getResourceId());
        index.setInnerId(item.getInnerId());
        index.setPictUrl(item.getPictUrl());
        index.setCategory(item.getCategory());
        index.setItemId(item.getItemId());
        index.setItemUrl(item.getItemUrl());
        index.setCommissionRate(item.getCommissionRate());
        index.setTitle(item.getTitle());
        index.setShortTitle(item.getShortTitle());
        index.setShopDsr(item.getShopDsr());
        index.setShopName(item.getShopName());
        index.setSellerId(item.getSellerId());
        index.setPrice(item.getPrice());
        index.setVolume(item.getVolume());
        index.setCreateDate(item.getCreateDate().getTime());
        index.setStatus(item.getStatus());

        return  index;
    }

}
