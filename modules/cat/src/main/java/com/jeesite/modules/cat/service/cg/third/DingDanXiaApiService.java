package com.jeesite.modules.cat.service.cg.third;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.cg.third.dto.JdUnionIdPromotion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
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
    public Result<JdUnionIdPromotion> jdByUnionidPromotionWithCoupon(String apiKey, String materialId, long unionId, long positionId) {

        Result<String> bestCouponUrl = getBestCouponUrl(apiKey, materialId, positionId);
        if (Result.isOK(bestCouponUrl)) {
            return doGetJdByUnionidPromotion(apiKey, materialId, unionId, positionId, bestCouponUrl.getResult());
        }

        return doGetJdByUnionidPromotion(apiKey, materialId, unionId, positionId, null);
    }

    /**
     * 通过unionId获取京东商品链接/活动链接/店铺链接【转链】【升级版】
     * https://www.dingdanxia.com/doc/97/94
     * @return
     */
    public Result<JdUnionIdPromotion> jdByUnionidPromotion(String apiKey, String materialId, long unionId, long positionId) {

        return doGetJdByUnionidPromotion(apiKey, materialId, unionId, positionId, null);
    }
//
//    // https://www.dingdanxia.com/doc/2/8
//    // http://api.tbk.dingdanxia.com/tbk/id_privilege
//    public Result<?> tbkIdPrivilege(String apiKey, String id) {
//        // 判空
//        if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(id)) {
//            return Result.ERROR(500, "参数错误");
//        }
//
//        String url = "http://api.tbk.dingdanxia.com/tbk/id_privilege";
//        try {
//            Map<String, Object> jsonMap = new HashMap<>();
//            jsonMap.put("apikey", apiKey);
//            jsonMap.put("id", id);
//            String s = flameHttpService.doPost(url, JsonUtils.toJSONString(jsonMap));
//            // {"code":-1,"msg":"数据返回失败【materialId链接无法识别】","data":[]}
//            if (StringUtils.isBlank(s)) {
//                return Result.ERROR(500, "转换失败");
//            }
//            try {
//                JSONObject jsonObject = JSONObject.parseObject(s);
//                if (jsonObject == null) {
//                    return Result.ERROR(500, "转换失败");
//                }
//                int code = Optional.ofNullable(jsonObject.getInteger("code")).orElse(-1);
//                String msg = jsonObject.getString("msg");
//                // {"code":200,"msg":"请求成功【success】","data":{"shortURL":"https://u.jd.com/0Q1Qhqo","note":"","is_coupon":0,"couponInfo":[],"commissionInfo":{"commission":42.54,"commissionShare":11.5,"couponCommission":42.54,"endTime":1699027199000,"isLock":1,"plusCommissionShare":11.5,"startTime":1697731200000},"priceInfo":{"lowestCouponPrice":369.9,"lowestPrice":369.9,"lowestPriceType":4,"price":399.9},"pinGouInfo":[],"shopInfo":{"afsFactorScoreRankGrade":"低","afterServiceScore":"3.90","commentFactorScoreRankGrade":"高","logisticsFactorScoreRankGrade":"高","logisticsLvyueScore":"4.30","scoreRankRate":"84.02","shopId":10542949,"shopLabel":"1","shopLevel":5,"shopName":"阿飞和巴弟旗舰店","userEvaluateScore":"4.70"},"skuName":"阿飞和巴弟【11.11预售狂欢】E76kids幼猫粮套餐 E76kids猫粮  4袋","skuId":10088555250634,"owner":"p","inOrderCount30Days":0,"imageInfo":{"imageList":[{"url":"https://img14.360buyimg.com/pop/jfs/t1/227037/18/496/139561/6531e44dF128c97a1/4ddc3970d9541408.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/205588/6/35245/42491/652f7effF63964776/cc40bae91af0de3b.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/230684/17/177/82463/652f7effF4404833b/5672dd780ab304ac.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/142539/17/40618/40023/652f7efeF5b627fee/21a31b930678b11a.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/187450/14/39423/56075/652f7efeF9dc2cb68/f1a4a5f4b6bfb37a.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/233669/39/126/84547/652f7c67F085f4362/421f5d5ec4484ee7.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/92538/27/29968/63631/652f7c66F70bbc858/9eb2d3fc1c53a101.jpg"}]},"documentInfo":[],"videoInfo":[]}}
//                if (code != 200) {
//                    return Result.ERROR(code, msg);
//                }
//                JdUnionIdPromotion data = jsonObject.getObject("data", JdUnionIdPromotion.class);
//                if (data == null) {
//                    return Result.ERROR(500, "转换失败");
//                }
//
//                return Result.OK(data);
//            } catch (Exception e) {
//                log.error("转换异常! materialId:{}", materialId, e);
//            }
//        } catch (Exception e) {
//            log.error("转换异常!!shiji materialId:{}", materialId, e);
//        }
//
//        return Result.ERROR(500, "转换失败");
//    }

    /**
     * 通过unionId获取京东商品链接/活动链接/店铺链接【转链】【升级版】
     * https://www.dingdanxia.com/doc/97/94
     * @return
     */
    public Result<JdUnionIdPromotion> doGetJdByUnionidPromotion(String apiKey, String materialId, long unionId, long positionId, String couponUrl) {

        String url = "http://api.tbk.dingdanxia.com/jd/by_unionid_promotion?apikey=%s&materialId=%s&unionId=%d&positionId=%d&autoSearch=true";

        try {
            String encode = URLEncoder.encode(materialId);
            url = String.format(url, apiKey, encode, unionId, positionId);
            if (StringUtils.isNotBlank(couponUrl)) {
                // 优惠券领取链接，在使用优惠券、商品二合一功能时入参，且materialId须为商品详情页链接 需要Urlencode
                String cl = URLEncoder.encode(couponUrl);
                url = url + "&couponUrl=" + cl;
            }

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
    }

    /**
     * https://www.dingdanxia.com/doc/100/94
     * 商品ID查询优惠券列表接口,京东/京喜超级搜索接口
     * @param apiKey
     * @param keyword
     * @param positionId
     * @return
     */
    public Result<String> getBestCouponUrl(String apiKey, String keyword, long positionId) {

        String url = "http://api.tbk.dingdanxia.com/jd/query_goods?apikey=%s&keyword=%s&pid=%d";

        try {
            url = String.format(url, apiKey, keyword, positionId);
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
                // {"code":200,"msg":"请求成功【success】","total_results":1,"similarSkuList":[],"hotWords":"","data":[{"brandCode":"7606","brandName":"冠能（PRO PLAN）","categoryInfo":{"cid1":6994,"cid1Name":"宠物生活","cid2":6995,"cid2Name":"猫狗主粮","cid3":7003,"cid3Name":"猫干粮"},"comments":460000,"commissionInfo":{"commission":25.8,"commissionShare":15,"couponCommission":20.1,"endTime":1702051199000,"isLock":1,"plusCommissionShare":15,"startTime":1701619200000},"couponInfo":{"couponList":[{"bindType":1,"couponStyle":0,"discount":38,"getEndTime":1702051199000,"getStartTime":1701619200000,"hotValue":0,"isBest":1,"isInputCoupon":1,"link":"https://coupon.jd.com/ilink/couponActiveFront/linkKey/front_index.action?linkKey=AAROH_xIpeffAs_-naABEFoe28bwBKLkhfD01n0hHk9AgJ6238jwsdz__sXV5tDMpUkdepCcexyBB_FYWcEUxaxjSBeMNA&to=https://mall.jd.com/index-1000100237.html","platformType":0,"quota":170,"useEndTime":1702051199000,"useStartTime":1701619200000}]},"deliveryType":1,"eliteType":[3,1000499054,1000498560,1000498461,20],"forbidTypes":[0],"goodCommentsShare":98,"imageInfo":{"imageList":[{"url":"https://img14.360buyimg.com/pop/jfs/t1/223147/4/35283/103578/65687972F0bd876a2/a3609666540b9c2a.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/234487/33/4906/108663/6565b371F8b383ea2/02fc9977d03b1a86.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/181521/18/44167/70764/6565b2efF86f2c589/099362a6d8eb06bf.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/197846/34/33962/74463/64eefae1Fe5b319d5/8d3b70ada795d1ff.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/89562/15/37324/100132/64df2a88F789217c8/78d22ea443718809.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/80168/11/26200/61875/6526457aFa34d60ea/030ef2eff932b169.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/6420/16/31392/63285/652645b0F058feb1c/11aa582a84d811f5.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/178638/10/40789/42906/652645d8F6afc5c36/0fb9c0e3f14dc753.jpg"},{"url":"https://img14.360buyimg.com/pop/jfs/t1/218475/9/22110/59043/634123aeE2d9d3f59/96f23dae489eeddf.jpg"}],"whiteImage":"https://img14.360buyimg.com/pop/jfs/t1/127328/34/36259/68686/64eeafcdFc67a6297/9f05f39cefcd43fd.png"},"inOrderComm30Days":227931.1,"inOrderCount30Days":10000,"isHot":1,"isJdSale":1,"isOversea":0,"itemId":"MsDxpCw3eBBU3UGhlbKb4c2o_3LZQVebpuKbbMBrvsH","materialUrl":"jingfen.jd.com/detail/MsDxpCw3eBBU3UGhlbKb4c2o_3LZQVebpuKbbMBrvsH.html","owner":"g","pinGouInfo":[],"pingGouInfo":[],"priceInfo":{"lowestCouponPrice":134,"lowestPrice":172,"lowestPriceType":1,"price":172},"shopInfo":{"shopId":1000100237,"shopLabel":"0","shopLevel":4.9,"shopName":"冠能PROPLAN京东自营官方旗舰店"},"skuId":5475198,"skuName":"冠能猫粮成猫鸡肉味2.5kg 挑嘴美毛 新老包装随机发","spuid":5088099,"videoInfo":[]}]}
                if (code != 200) {
                    return Result.ERROR(code, msg);
                }
                JSONArray data = jsonObject.getJSONArray("data");
                if (data == null) {
                    return Result.ERROR(500, "转换失败");
                }
                // 获取第一个sku的商品的券
                JSONObject couponInfo = data.getJSONObject(0).getJSONObject("couponInfo");
                if (couponInfo == null || couponInfo.get("couponList") == null) {
                    return Result.ERROR(404, "无优惠券");
                }

                // 如果没有bestCoupon,则取第一个
                String couponUrl = "";
                JSONArray couponList = couponInfo.getJSONArray("couponList");
                for (int i = 0; i < couponList.size(); i++) {
                    JSONObject coupon = couponList.getJSONObject(i);
                    if (coupon == null) {
                        continue;
                    }
                    Integer isBest = coupon.getInteger("isBest");
                    if (isBest != null && isBest == 1) {
                        couponUrl = coupon.getString("link");
                        break;
                    }
                    if (StringUtils.isBlank(couponUrl)) {
                        couponUrl = coupon.getString("link");
                    }
                }

                if (StringUtils.isBlank(couponUrl)) {
                    return Result.ERROR(404, "无优惠券");
                }

                return Result.OK(couponUrl);
            } catch (Exception e) {
                log.error("转换异常! keyword:{}", keyword, e);
            }
        } catch (Exception e) {
            log.error("转换异常!! keyword:{}", keyword, e);
        }

        return Result.ERROR(500, "转换失败");
    }

    /**
     * 淘宝客高佣转链api（淘口令版）
     * @param apikey 接口秘钥，请登录后台获取
     * @param tkl 淘口令，支持传入包含口令的整个文案，但最好是直接传一个口令
     * @param pid 淘宝联盟推广位pid，必须为在本平台授权淘宝账号下的pid，否则无效。如不传该参数则默认走后台填写的pid
     */
//    public void tklPrivilege(String apikey, String tkl, String pid) {
//        String url = "http://api.tbk.dingdanxia.com/tbk/tkl_privilege";
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("apikey", apikey);
//        params.put("tkl", tkl);
//        params.put("pid", pid);
//        params.put("tpwd", true);
//
//        flameHttpService.doPost(url, JsonUtils.toJSONString(params));
//    }

    /**
     * 淘宝客高佣转链api（商品id版）
     * @param apikey 接口秘钥，请登录后台获取
     * @param id 商品id
     * @param pid 淘宝联盟推广位pid，必须为在本平台授权淘宝账号下的pid，否则无效。如不传该参数则默认走后台填写的pid
     */
    public Result<JSONObject> idPrivilege(String apikey, String id, String pid) {
        String url = "http://api.tbk.dingdanxia.com/tbk/id_privilege";

        Map<String, Object> params = new HashMap<>();
        params.put("apikey", apikey);
        params.put("id", id);
        params.put("pid", pid);
        params.put("tpwd", true);

        String s = flameHttpService.doPost(url, JsonUtils.toJSONString(params));
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

            return Result.OK(jsonObject);
        } catch (Exception e) {
            log.error("转换异常! id:{}", id, e);
        }

        return Result.ERROR(500, "转换失败");
    }
}
