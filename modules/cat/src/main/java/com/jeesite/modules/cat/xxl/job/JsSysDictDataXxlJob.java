package com.jeesite.modules.cat.xxl.job;

import com.jeesite.common.collect.MapUtils;
import com.jeesite.modules.cat.entity.JsDictDataDO;
import com.jeesite.modules.cat.service.JsDictDataService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统字典数据同步全局配置
 */
@Slf4j
@Component
public class JsSysDictDataXxlJob extends IJobHandler {

    // dict_type, <dict_label, dict_value>
    private Map<String, Map<String, String>> dictMap = new HashMap<>();

    @Resource
    private JsDictDataService jsDictDataService;

    @Override
    @XxlJob("jsSysDictDataXxlJob")
    public void execute() throws Exception {
        XxlJobHelper.log("jsSysDictDataXxlJob xxl job start");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取数据
        initDictMap();
        stopWatch.stop();

        XxlJobHelper.log("goodProductAutoAuditXxlJob xxl job end 耗时：" + stopWatch.toString());
    }


    public void initDictMap() {

        try {
            // 获取全部系统有效配置
            JsDictDataDO query = new JsDictDataDO();
            query.setStatus("0");
            query.setIsSys("1");
            List<JsDictDataDO> dicts = jsDictDataService.findList(query);
            if (CollectionUtils.isEmpty(dicts)) {
                return;
            }
            Map<String, Map<String, String>> map = new HashMap<>();
            for (JsDictDataDO dataDO : dicts) {
                String dictType = dataDO.getDictType();
                Map<String, String> dataMap = map.get(dictType);
                if (MapUtils.isEmpty(dataMap)) {
                    dataMap = new HashMap<>();
                }
                dataMap.put(dataDO.getDictLabel(), dataDO.getDictValue());
                map.put(dictType, dataMap);
            }

            dictMap = map;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    public Map<String, String> getDictMapping(String dictType) {
        if (MapUtils.isEmpty(dictMap)) {
            initDictMap();
        }

        if (MapUtils.isEmpty(dictMap)) {
            log.error("dictMap is empty");
            return null;
        }

        return dictMap.get(dictType);
    }
}
