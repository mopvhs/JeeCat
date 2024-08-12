package com.jeesite.modules.cat.service.cg;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.stage.cg.ocean.AbstraOceanStage;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanContext;
import com.jeesite.modules.cat.service.stage.cg.ocean.OceanStage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OceanSyncService {

    @Resource
    private MaocheSyncDataInfoService maocheSyncDataInfoService;

    @Resource
    private MaocheRobotCrawlerMessageService maocheRobotCrawlerMessageService;

    @Resource
    private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncService;

    @Resource
    private OceanStage tbOceanStage;

    @Resource
    private OceanStage jdOceanStage;

    public Result<String> sync() {
        MaocheSyncDataInfoDO dataInfo = maocheSyncDataInfoService.getLatestSyncDataInfo("maoche_robot_crawler_message");
        long syncDataId = 0;
        long maxId = 0;
        int step = 50;
        if (dataInfo != null) {
            syncDataId = dataInfo.getIid();
            maxId = NumberUtils.toLong(dataInfo.getSyncMaxId());
            step = Optional.ofNullable(dataInfo.getStep()).orElse(step);
            if (maxId <= 0) {
                return Result.ERROR(400, "同步数据异常，最大同步id为0");
            }
        }

        step = 50;

        List<String> affTypes = new ArrayList<>();
        affTypes.add("tb");
        affTypes.add("jd");
        // 查询数据
        List<MaocheRobotCrawlerMessageDO> messages = maocheRobotCrawlerMessageService.startById(maxId, step, affTypes);
        if (CollectionUtils.isEmpty(messages)) {
            return Result.OK("暂无数据");
        }

        String offset = String.valueOf(messages.get(messages.size() - 1).getId());
        // 一个一个的解析
        for (MaocheRobotCrawlerMessageDO message : messages) {
            offset = String.valueOf(message.getId());
            // afftype干预订正
            String affType = message.getAffType();
            String msg = message.getMsg();
            affType = AbstraOceanStage.fixAffType(msg, affType);
            message.setAffType(affType);
            try {
                OceanContext context = new OceanContext(message);
                if (affType.equals("tb")) {
                    tbOceanStage.process(context);
                } else if (affType.equals("jd")) {
                    jdOceanStage.process(context);
                }
            } catch (Exception e) {
                break;
            }
        }

        // 更新位点
        maocheSyncDataInfoService.addOrUpdateOffset(syncDataId, "maoche_robot_crawler_message", offset);

        return Result.OK("操作完成");
    }


}
