package com.jeesite.modules.cgcat;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.cache.CacheService;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.model.TagTree;
import com.jeesite.modules.cat.service.MaocheTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CgMaocheTagController {

    @Resource
    private CacheService cacheService;

    @Resource
    private MaocheTagService maocheTagService;

    // 获取标签列表
    @RequestMapping(value = "/maoche/tag/list")
    @ResponseBody
    public Result<List<TagTree>> listTags() {

        List<TagTree> tagTrees = maocheTagService.getCategoryTreeFromCache();

        return Result.OK(tagTrees);
    }

    // 新增标签
    // 获取标签列表
    @RequestMapping(value = "/maoche/tag/add")
    @ResponseBody
    public Result<String> addTag(@RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId,
                                 @RequestParam(value = "name") String name) {

        // 判空
        if (parentId == null || StringUtils.isBlank(name)) {
            return Result.ERROR(400, "参数错误");
        }

        // 查询是否重复添加
        MaocheTagDO query = new MaocheTagDO();
        query.setTagName(name);
        query.setStatus("NORMAL");

        List<MaocheTagDO> tags = maocheTagService.findList(query);
        if (CollectionUtils.isNotEmpty(tags)) {
            return Result.ERROR(400, "标签已存在");
        }
        long level = 1;
        if (parentId > 0) {
            level = 2;
        }

        // 添加标签
        MaocheTagDO tag = new MaocheTagDO();
        tag.setLevel(level);
        tag.setParentId(parentId);
        tag.setTagName(name);
        tag.setStatus("NORMAL");
        tag.setRemarks("");
        tag.setUpdateBy("admin");
        tag.setCreateBy("admin");
        tag.setUpdateDate(new Date());
        tag.setCreateDate(new Date());

        maocheTagService.save(tag);

        if (StringUtils.isNotBlank(tag.getId())) {

            // 删除缓存
            maocheTagService.deleteCache();

            return Result.OK("添加完成");
        }

        return Result.ERROR(500, "添加失败");
    }
}
