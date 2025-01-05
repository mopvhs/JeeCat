package com.jeesite.modules.cat.model.task.content;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.enums.task.PushTypeEnum;
import com.jeesite.modules.cat.helper.PriceHelper;
import com.jeesite.modules.cat.service.cg.task.dto.NameDetail;
import com.jeesite.modules.cat.service.cg.task.dto.ProductDetail;
import com.jeesite.modules.cat.service.cg.task.dto.PropsInfo;
import com.jeesite.modules.cat.service.cg.task.dto.TaskDetail;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class PushTaskContentDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 3619517226687520758L;

    private String detail;

    private String img;

    public PushTaskContentDetail() {
    }

    public PushTaskContentDetail(String detail, String img) {
        this.detail = detail;
        this.img = img;
    }

    public static PushTaskContentDetail buildContent(MaochePushTaskDO pushTaskDO, TaskDetail detail) {
        PushTaskContentDetail content = new PushTaskContentDetail();
        if (detail == null) {
            return content;
        }


        String pushTypeName = null;
        PushTypeEnum pushTypeEnum = PushTypeEnum.getByName(pushTaskDO.getPushType());
        if (pushTypeEnum != null) {
            pushTypeName = pushTypeEnum.getDesc();
        }
        StringBuilder st = new StringBuilder();
        appendWithLine(st, pushTypeName);

        // 判断是否有活动券
        List<NameDetail> actCoupons = Optional.ofNullable(detail.getActCoupons()).orElse(new ArrayList<>());
        for (NameDetail coupon : actCoupons) {
            if (coupon == null || StringUtils.isBlank(coupon.getContent()) || StringUtils.isBlank(coupon.getName())) {
                continue;
            }
            appendWithLine(st, coupon.getName());
            appendWithLine(st, coupon.getContent());
        }

        List<ProductDetail> products = Optional.ofNullable(detail.getProducts()).orElse(new ArrayList<>());

        String img = "";
        for (ProductDetail product : products) {

            String title = product.getTitle();
            String subTitle = product.getSubTitle();
            if (StringUtils.isNotBlank(subTitle)) {
                title = subTitle + title;
            }
            if (CollectionUtils.isNotEmpty(product.getPropsInfos())) {
                PropsInfo propsInfo = product.getPropsInfos().get(0);
                if (StringUtils.isNotBlank(propsInfo.getContent())) {
                    title += propsInfo.getContent() + propsInfo.getType();
                }
                if (product.getNum() > 1) {
                    title += "*" + product.getNum();
                }
            }

            if (StringUtils.isBlank(img) && CollectionUtils.isNotEmpty(product.getImgs()) && StringUtils.isNotBlank(product.getImgs().get(0))) {
                img = product.getImgs().get(0);
            }

            // 单价 + plus
            String originPriceSt = "到手价💰" + PriceHelper.formatPriceReplaceZero(product.getPayPrice());
//            if (product.getPayPrice() != null) {
//                originPriceSt += "，券后价💰" + PriceHelper.formatPriceReplaceZero(product.getPayPrice());
//            }
            String resourceType = product.getResourceType();
            String vipPriceName = "";
            if ("tb".equals(resourceType)) {
                vipPriceName = "88VIP";
            } else if ("jd".equals(resourceType)) {
                vipPriceName = "Plus";
            }
            if (product.getVipPrice() != null) {
                if (StringUtils.isNotBlank(originPriceSt)) {
                    originPriceSt += "，";
                }
                originPriceSt += vipPriceName + "💰" + PriceHelper.formatPriceReplaceZero(product.getVipPrice());;
            }

            String plusPriceSt = "";
            String itemDesc = null;
            if (product.getNum() > 1) {
                itemDesc = "加购" + product.getNum() + "件";

                if (product.getDiscountPrice() != null && product.getDiscountPrice() > 0) {
                    plusPriceSt += "折💰" + PriceHelper.formatPriceReplaceZero(product.getDiscountPrice()) + "/件";
                }
            }

            appendWithLine(st, title);
            appendWithLine(st, originPriceSt);
            appendWithLine(st, plusPriceSt);
            appendWithLine(st, itemDesc);
            appendWithLine(st, product.getCommand());
        }

        // 凑单
        List<NameDetail> addOnProducts = Optional.ofNullable(detail.getAddOnProducts()).orElse(new ArrayList<>());
        for (NameDetail addOnProduct : addOnProducts) {
            if (addOnProduct == null || StringUtils.isBlank(addOnProduct.getName()) || StringUtils.isBlank(addOnProduct.getContent())) {
                continue;
            }
            appendWithLine(st, addOnProduct.getName());
            appendWithLine(st, addOnProduct.getContent());
        }

        NameDetail miniProgram = detail.getMiniProgram();
        // 小程序
        if (miniProgram != null && StringUtils.isNotBlank(miniProgram.getContent()) && miniProgram.isShow()) {
            appendWithLine(st, "去小程序付");
            appendWithLine(st, miniProgram.getContent());
        }
        // 描述
        appendWithLine(st, detail.getDesc());
        // 标准底部
        appendWithLine(st, "---------------------");
//        appendWithLine(st, "自助查车@猫车选品官 +产品名");
        st.append("自助查车 dwz.cn/qveM26UV");

        content.setDetail(st.toString());
        content.setImg(img);

        return content;
    }

    private static void appendWithLine(StringBuilder st, String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }

        st.append(content).append("\n");
    }
}
