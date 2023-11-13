package com.jeesite.modules.cat.service.cg.third;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 维易接口
 */
@Slf4j
@Component
public class VeApiService {

    @Resource
    private FlameHttpService flameHttpService;


    // 超级搜索
    public Result<JSONArray> tbSearch(String veKey, String id, String pid) {
        // 判空
        if (StringUtils.isBlank(veKey) || StringUtils.isBlank(id) || StringUtils.isBlank(pid)) {
            return Result.ERROR(400, "veKey不能为空");
        }

        // 700021007361678794ad9e04977e5d18515c3b2b1284eed41f1ad2ec0a1b63004744dcc53145618
        String url = "http://api.veapi.cn/tbk/tb_search?sessionkey=700021007361678794ad9e04977e5d18515c3b2b1284eed41f1ad2ec0a1b63004744dcc53145618&tklink=1&item_id=%s&vekey=%s&pid=%s";
        url = String.format(url, id, veKey, pid);
//        Map<String, Object> objectMap = new HashMap<>();
//        objectMap.put("item_id", id);
//        objectMap.put("vekey", veKey);
//        objectMap.put("pid", pid);

        try {
            String response = flameHttpService.doGet(url);
            if (StringUtils.isBlank(response)) {
                return Result.ERROR(500, "请求失败，数据为空");
            }
            // {"error":"0","msg":"查询成功！","search_type":10,"is_similar":"0","is_splitWord":0,"force_index":"","total_results":1,"result_list":[{"category_id":50023217,"category_name":"猫砂","commission_rate":"150","coupon_info":"","coupon_remain_count":0,"coupon_total_count":0,"include_dxjh":"true","info_dxjh":"{\\\"1001575805\\\":\\\"601\\\",\\\"1001651112\\\":\\\"150\\\",\\\"1001557372\\\":\\\"150\\\",\\\"1001630216\\\":\\\"601\\\"}","item_description":"遇水释香 清新白茶 长久祛味","item_id":"Z4Gw55XC6tPzq7bP28TJKvfpU6-k53G2xS8bn8qdjVh52","item_url":"https://uland.taobao.com/item/edetail?id=Z4Gw55XC6tPzq7bP28TJKvfpU6-k53G2xS8bn8qdjVh52","level_one_category_id":29,"level_one_category_name":"宠物/宠物食品及用品","lock_rate":"0","nick":"福丸旗舰店","num_iid":"Z4Gw55XC6tPzq7bP28TJKvfpU6-k53G2xS8bn8qdjVh52","pict_url":"https://img.alicdn.com/bao/uploaded/i1/4052404661/O1CN017U1DVk1kImxSFm4UJ_!!0-item_pic.jpg","presale_deposit":"","provcity":"浙江 杭州","real_post_fee":"0.00","reserve_price":"39.90","seller_id":262546345292736640,"shop_dsr":49167,"shop_title":"福丸旗舰店","short_title":"","small_images":["https://img.alicdn.com/i4/4052404661/O1CN01yRWvM71kImxFrVF7p_!!0-item_pic.jpg","https://img.alicdn.com/i1/4052404661/O1CN01G0EjUx1kImxGYSiJF_!!4052404661.png","https://img.alicdn.com/i1/4052404661/O1CN01gkGKuv1kImwrFo55T_!!4052404661.jpg","https://img.alicdn.com/i2/4052404661/O1CN01PKBy6L1kImwipT2UR_!!4052404661.jpg"],"superior_brand":"0","title":"【20点抢】百亿补贴福丸爆款白茶混合豆腐猫砂2kg","tk_total_commi":"","tk_total_sales":"","url":"https://s.click.taobao.com/t?e=m%3D2%26s%3DKEFh5Ujcm5gcQipKwQzePOeEDrYVVa64r4ll3HtqqoxyINtkUhsv0GMeqLljBpQlAMYTuK9O3W1RPAWXiEIX1XKGrHNQ4%2FdmtG2MJ%2BKRa4%2FdSMASiQPvQy6EJTdg%2FQ6fSBaygToy7XlbvY7ttc82R0sy7BKU7b5q2pePJy6EQg8ckRg%2BYjOckp3d7CqX3ZqBDBbY3acjQHdCYccaaem8RwgERt3LvFaxUR0dcAWqbpw2h1fwfJ4LNOei79hRDyfknBf80C6qOF8%3D&scm=1007.30148.280456.0&pvid=f8998af5-89e7-4c8a-9c2b-d111e2cc112d&app_pvid=59590_33.62.115.168_12353_1699344400108&ptl=floorId:2836;originalFloorId:2836;pvid:f8998af5-89e7-4c8a-9c2b-d111e2cc112d;app_pvid:59590_33.62.115.168_12353_1699344400108&xId=2goLC6JiR51gJiPdOXIYfD6jmDfjp9B2z7A3ODJYVAuDoLUJizgajtrkFU0AcQ9qanAq3cT6gW9QrWx0bGbgaoINlHTyEa6L0zRlx0vz1Qr7&union_lens=lensId%3AMAPI%401699344400%40213e73a8_0f93_18ba8d1bf0b_da86%4001%40eyJmbG9vcklkIjoyODM2fQieie","user_type":1,"volume":50000,"white_image":"","zk_final_price":"29.9","coupon_id":""}],"request_id":"6QEkbfP"}
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (jsonObject == null) {
                return Result.ERROR(500, "请求解析失败，数据为空");
            }
            // 搜索类型：0表示未启动搜索，10表示搜索淘口令或链接中的产品，21和22表示搜索非淘客商品找相似结果，30表示搜索关键字，60其它
            Integer searchType = jsonObject.getInteger("search_type");
            if (searchType == null || searchType != 10) {
                return Result.ERROR(1500, "search_type异常，请求解析失败，数据为空");
            }
            String code = jsonObject.getString("error");
            String msg = jsonObject.getString("msg");
            if (!"0".equals(code)) {
                int errorCode = NumberUtils.toInt(code) + 10000;
                return Result.ERROR(errorCode, msg);
            }

            return Result.OK(jsonObject.getJSONArray("result_list"));
        } catch (Exception e) {
            log.error("请求淘宝客接口异常", e);
        }

        return Result.OK(null);
    }
}
