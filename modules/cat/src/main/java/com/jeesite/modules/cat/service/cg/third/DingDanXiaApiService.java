package com.jeesite.modules.cat.service.cg.third;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.Optional;

@Slf4j
@Component
public class DingDanXiaApiService {

    @Resource
    private FlameHttpService flameHttpService;

    /**
     * 通过unionId获取京东商品链接/活动链接/店铺链接【转链】【升级版】
     * https://www.dingdanxia.com/doc/97/94
     * @return
     */
    public Result<JdUnionIdPromotion> jdByUnionidPromotion(String apiKey, String materialId, long unionId, long positionId) {

        String url = "http://api.tbk.dingdanxia.com/jd/by_unionid_promotion?apikey=%s&materialId=%s&unionId=%d&positionId=%d";

        try {
            String encode = URLEncoder.encode(materialId);
            url = String.format(url, apiKey, encode, unionId, positionId);
            String s = flameHttpService.doGet(url);
            // {"code":-1,"msg":"数据返回失败【materialId链接无法识别】","data":[]}
            if (StringUtils.isBlank(s)) {
                return Result.ERROR(500, "转换失败");
            }
            try {
                JSONObject jsonObject = JSONObject.parseObject(s);
                if (jsonObject == null) {
                    return Result.ERROR(500, "转换失败");
                }
                int code = Optional.ofNullable(jsonObject.getInteger("code")).orElse(-1);
                String msg = jsonObject.getString("msg");
                // {"code":200,"msg":"请求成功【success】","data":{"shortURL":"https://u.jd.com/0Q1Qhqo","note":"","is_coupon":0,"couponInfo":[],"commissionInfo":{"commission":42.54,"commissionShare":11.5,"couponCommission":42.54,"endTime":1699027199000,"isLock":1,"plusCommissionShare":11.5,"startTime":1697731200000},"priceInfo":{"lowestCouponPrice":369.9,"lowestPrice":369.9,"lowestPriceType":4,"price":399.9},"pinGouInfo":[],"shopInfo":{"afsFactorScoreRankGrade":"低","afterServiceScore":"3.90","commentFactorScoreRankGrade":"高","logisticsFactorScoreRankGrade":"高","logisticsLvyueScore":"4.30","scoreRankRate":"84.02","shopId":10542949,"shopLabel":"1","shopLevel":5,"shopName":"阿飞和巴弟旗舰店","userEvaluateScore":"4.70"},"skuName":"阿飞和巴弟【11.11预售狂欢】E76kids幼猫粮套餐 E76kids猫粮  4袋","skuId":10088555250634,"owner":"p","inOrderCount30Days":0,"imageInfo":{"imageList":[{"url":"https://img14.360buyimg.com/pop/jfs/t1/227037/18/496/139561/6531e44dF128c97a1/4ddc3970d9541408.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/205588/6/35245/42491/652f7effF63964776/cc40bae91af0de3b.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/230684/17/177/82463/652f7effF4404833b/5672dd780ab304ac.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/142539/17/40618/40023/652f7efeF5b627fee/21a31b930678b11a.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/187450/14/39423/56075/652f7efeF9dc2cb68/f1a4a5f4b6bfb37a.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/233669/39/126/84547/652f7c67F085f4362/421f5d5ec4484ee7.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/92538/27/29968/63631/652f7c66F70bbc858/9eb2d3fc1c53a101.jpg"}]},"documentInfo":[],"videoInfo":[]}}
                if (code != 200) {
                    return Result.ERROR(code, msg);
                }
                JdUnionIdPromotion data = jsonObject.getObject("data", JdUnionIdPromotion.class);
                if (data == null) {
                    return Result.ERROR(500, "转换失败");
                }

                return Result.OK(data);
            } catch (Exception e) {
                log.error("转换异常! materialId:{}", materialId, e);
            }
        } catch (Exception e) {
            log.error("转换异常!!shiji materialId:{}", materialId, e);
        }

        return Result.ERROR(500, "转换失败");

        /**
         * if (strpos($message->content, 'http') !== FALSE) {
         *                     $text = $message->content;
         *                     $re = '/^(http|https):\/\/[a-zA-Z0-9-\.]+\.[a-z]{2,}(\/\S*)/m';
         *                     preg_match_all($re, $text, $matches, PREG_SET_ORDER, 0);
         *
         *                  ✨有好价✨
         * K9海外旗舰店，领200-37
         * https://jd.cn.hn/aUGu
         * plus领200-10
         * https://u.jd.com/ysMPUng
         * k9鸡肉猫主食罐头170g
         * https://u.jd.com/PzN7wAE
         * k9牛肉鳕鱼猫主食罐头170g
         * https://u.jd.com/P8N7Lw1
         * k9羊心帝王鲑猫主食罐头170g
         * https://u.jd.com/PqN7bbI
         * 任意加车6件 💰114.2
         * plus💰104.2，折💰17.3/罐
         * ---------------------
         * 自助查车@猫车选品官 +产品名
         */


    }
}
