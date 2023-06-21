package com.jeesite.modules.cat.service.cg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.model.dataoke.DaTaoKeResponse;
import com.jeesite.modules.cat.service.FlameHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DaTaoKeApiService {

    @Resource
    private FlameHttpService flameHttpService;

    // https://openapi.dataoke.com/api/goods/get-history-low-price-list

    /**
     * https://openapi.dataoke.com/api/goods/get-history-low-price-list
     */
    public DaTaoKeResponse<Object> getHistoryLowPriceList(String appKey, String version, int pageId, int pageSize, String cids, String sort) {

        String url = "https://openapi.dataoke.com/api/goods/get-history-low-price-list" +
                "?appKey=%s" +
                "&version=%s" +
                "&cids=%s" +
                "&pageSize=%d&pageId=%d&sort=%s";

//        String apiUrl = cgUnionProductService.getEApiUrl("V73687541H40026415", unionProductDO.getItemId(), "mm_30153430_909250463_109464700418");
        String apiUrl = String.format(url, appKey, version, cids, pageSize, pageId, sort);

        try {

            String response = flameHttpService.doGet(apiUrl);

            log.info("getHistoryLowPriceList apiUrl:{}, response:{}", apiUrl, response);

            return JsonUtils.toReferenceType(response, new TypeReference<DaTaoKeResponse<Object>>() {
            });
        } catch (Exception e ) {
            log.info("getHistoryLowPriceList apiUrl:{}", apiUrl, e);
        }

        return null;
    }

}
