package com.jeesite.modules.cat.service.es;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.entity.MaocheTaskDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.service.MaochePushTaskService;
import com.jeesite.modules.cat.service.MaocheTaskService;
import com.jeesite.modules.cat.service.es.dto.PushTaskIndex;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TaskEsService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaochePushTaskService maochePushTaskService;

    @Resource
    private MaocheTaskService maocheTaskService;

    public boolean indexEs(List<String> pushIds, int corePoolSize) {
        if (CollectionUtils.isEmpty(pushIds)) {
            return false;
        }
        pushIds = pushIds.stream().distinct().collect(Collectors.toList());

        List<MaochePushTaskDO> pushTaskDOs = maochePushTaskService.getByIds(pushIds);
        if (CollectionUtils.isEmpty(pushTaskDOs)) {
            return false;
        }

        List<String> taskIds = pushTaskDOs.stream().map(MaochePushTaskDO::getTaskId).distinct().toList();
        List<MaocheTaskDO> maocheTaskDOs = maocheTaskService.listByIds(taskIds);
        Map<String, MaocheTaskDO> taskDOMap = maocheTaskDOs.stream().collect(Collectors.toMap(MaocheTaskDO::getId, Function.identity(), (k1, k2) -> k1));

        List<Map<String, Object>> messageSyncIndex = build(pushTaskDOs, taskDOMap);

        elasticSearch7Service.index(messageSyncIndex, ElasticSearchIndexEnum.PUSH_TASK_INDEX, "id", corePoolSize);

        return true;
    }

    private List<Map<String, Object>> build(List<MaochePushTaskDO> pushTaskDOs,
                                            Map<String, MaocheTaskDO> taskDOMap) {
        if (CollectionUtils.isEmpty(pushTaskDOs)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        
        for (MaochePushTaskDO push : pushTaskDOs) {
            MaocheTaskDO maocheTaskDO = taskDOMap.get(push.getTaskId());
            PushTaskIndex index = PushTaskIndex.toIndex(push, maocheTaskDO);

            Map<String, Object> map = JsonUtils.toReferenceType(JsonUtils.toJSONString(index), new TypeReference<Map<String, Object>>() {
            });

            list.add(map);
        }

        return list;
    }
}
