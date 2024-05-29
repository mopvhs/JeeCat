package com.jeesite.modules.cgcat;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.xxl.job.CgProductSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.task.SyncOceanSimilarXxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class TestController {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private SyncOceanSimilarXxlJob syncOceanSimilarXxlJob;

    @Resource
    private CgProductSyncXxlJob cgProductSyncXxlJob;

    @RequestMapping(value = "/test/es/maoche_message_sync_index/similar/update")
    public String update(@Param("iids") String iids) {

        if (iids.equals("0")) {
            try {
                syncOceanSimilarXxlJob.execute();
            } catch (Exception e) {

            }

            return "最近n条更新完成";
        }

        List<Map<String, Object>> data = new ArrayList<>();
        String[] split = iids.split(",");
        for (String p : split) {
            Map<String, Object> messageSyncIndex = new HashMap<>();
            messageSyncIndex.put("id", NumberUtils.toLong(p));
            messageSyncIndex.put("status", "SIMILAR");
            data.add(messageSyncIndex);
        }

        elasticSearch7Service.update(data, ElasticSearchIndexEnum.MAOCHE_OCEAN_MESSAGE_SYNC_INDEX, "id", 10);

        return "success";
    }

    @RequestMapping(value = "/test/es/product/all/index")
    public String productIndex() throws Exception {


        cgProductSyncXxlJob.execute();
        return "success";
    }
}
