package com.jeesite.modules.cgcat;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cgcat.dto.topic.TopicSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class TopicController {

    @Resource
    private MaochePushTaskRuleService maochePushTaskRuleService;


    @RequestMapping(value = "/topic/detail/get")
    public Result<?> getTopicDetail(@RequestBody TopicSearchRequest request) {

        if (request == null || StringUtils.isBlank(request.getId())) {
            return Result.error("参数错误");
        }

        MaochePushTaskRuleDO query = new MaochePushTaskRuleDO();
        query.setId(request.getId());
        MaochePushTaskRuleDO ruleDO = maochePushTaskRuleService.get(query);
        if (ruleDO == null) {
            return Result.error("未找到该话题");
        }

        return Result.OK();
    }

    @RequestMapping(value = "/topic/edit")
    public void editTopic(@RequestBody TopicSearchRequest request) {
        log.info("getTaskInfo");


    }

    @RequestMapping(value = "/topic/search")
    public void searchTopics(@RequestBody TopicSearchRequest request) {
        log.info("getTaskInfo");


    }
}
