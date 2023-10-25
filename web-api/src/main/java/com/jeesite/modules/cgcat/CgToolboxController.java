package com.jeesite.modules.cgcat;


import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.jeesite.modules.cat.service.toolbox.dto.CommandDTO;
import com.jeesite.modules.cat.xxl.job.CgProductDeleteSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.CgProductSyncXxlJob;
import com.jeesite.modules.cgcat.dto.CommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CgToolboxController {

    @Resource
    private CommandService commandService;

    @Resource
    private CgProductDeleteSyncXxlJob cgProductDeleteSyncXxlJob;

    @Resource
    private CgProductSyncXxlJob cgProductSyncXxlJob;

    /**
     * 口令信息替换
     * @param command
     * @return
     */
    @RequestMapping(value = "toolbox/command/exchange")
    public Result<CommandDTO> exchangeCommand(@RequestBody CommandRequest command) {

        if (command == null || StringUtils.isBlank(command.getContent()) || StringUtils.isBlank(command.getType())) {
            return Result.ERROR(500, "参数不能为空");
        }

        Result<CommandDTO> result = commandService.exchangeCommand(command.getContent(), command.getType());

        return result;
    }

    /**
     * 口令信息解析
     * @param command
     * @return
     */
     @RequestMapping(value = "toolbox/command/analysis")
    public Result<?> analysisCommand(@RequestBody CommandRequest command) {

        if (command == null || StringUtils.isBlank(command.getContent()) || StringUtils.isBlank(command.getType())) {
            return Result.ERROR(500, "参数不能为空");
        }

        Result<?> result = commandService.analysisCommand(command.getContent(), command.getType());

        return result;
    }



    @RequestMapping(value = "toolbox/product/del/sync")
    public Result<?> syncDelProduct() {

        try {
            cgProductDeleteSyncXxlJob.execute();
        } catch (Exception e) {
            log.error("同步删除商品失败", e);
            return Result.ERROR(500, "同步删除商品失败");
        }
        return Result.OK("OK");
    }

    @RequestMapping(value = "toolbox/product/online/sync")
    public Result<?> syncProduct() {

        try {
            cgProductSyncXxlJob.execute();
        } catch (Exception e) {
            log.error("同步商品失败", e);
            return Result.ERROR(500, "同步商品失败");
        }
        return Result.OK("OK");
    }
}
