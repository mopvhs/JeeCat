package com.jeesite.modules.cgcat;

import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaochePushTaskRuleDO;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.CatUnionProductCondition;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cgcat.dto.ProductSpecificationTemplateVO;
import com.jeesite.modules.cgcat.dto.PushTaskRuleRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.annotations.Param;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api/")
public class CgPushTaskRuleController {

    @Resource
    private MaochePushTaskRuleService maochePushTaskRuleService;

    @Resource
    private CgUnionProductService cgUnionProductService;


    // 规则列表获取
    @RequestMapping(value = "/product/push/task/rule/list")
    @ResponseBody
    public Result<Object> listRules(@RequestBody PushTaskRuleRequest request) {
        // yhq tody
        return Result.OK(null);
    }

    // 新增或者修改规则
    @RequestMapping(value = "/product/push/task/rule/addOrUpdate")
    @ResponseBody
    public Result<String> addOrUpdatePushTaskRule(@RequestBody PushTaskRuleRequest request) {
        if (request == null) {
            return Result.ERROR(400, "参数不能为空");
        }

        MaochePushTaskRuleDO ruleDO = new MaochePushTaskRuleDO();
        ruleDO.setBrand(request.getBrand());
        ruleDO.setEnglishBrand(request.getEnglishBrand());
        ruleDO.setProductName(request.getProductName());
        ruleDO.setKeyword(JsonUtils.toJSONString(request.getKeywords()));

        ruleDO.setCategoryId(request.getCategoryId());
        ruleDO.setCategoryName(request.getCategory());
        ruleDO.setLevelOneCategoryId(request.getLevelOneCategoryId());
        ruleDO.setLevelOneCategoryName(request.getLevelOneCategoryName());

        ruleDO.setStar(request.getStar());
        ruleDO.setPolling(request.getPolling());

        ruleDO.setUpdateDate(new Date());
        ruleDO.setStatus("NORMAL");

        // 更新
        if (request.getId() != null && request.getId() > 0) {
            ruleDO.setId(String.valueOf(request.getId()));
            ruleDO.setUiid(request.getId());
            // 更新
            maochePushTaskRuleService.update(ruleDO);
            return Result.OK("更新完成");
        }

        ruleDO.setRemarks("");
        ruleDO.setUpdateBy("admin");
        ruleDO.setCreateBy("admin");
        ruleDO.setCreateDate(new Date());

        // 新增
        maochePushTaskRuleService.save(ruleDO);

        return Result.OK("新增完成");
    }

    // 获取关键词最大的类目
    @RequestMapping(value = "/product/push/task/rule/keyword/category/get")
    @ResponseBody
    public Result<Map<String, Object>> getKeywordCategory(@Param("keyword") String keyword) {

        if (StringUtils.isBlank(keyword)) {
            return Result.ERROR(400, "关键词不能为空");
        }

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setTitle(keyword);

        SearchSourceBuilder source = cgUnionProductService.searchSource(condition,
                this::buildCategoryTermsAgg, null, null, 0, 1);

        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> search = cgUnionProductService.search(source);
        if (search == null) {
            return Result.ERROR(400, "查询失败");
        }

        if (MapUtils.isEmpty(search.getBucketMap())) {
            return Result.ERROR(404, "暂无资源");
        }
        List<CatProductBucketTO> categories = search.getBucketMap().get("category");

        CatProductBucketTO catProductBucketTO = categories.get(0);

        Map<String, Object> data = new HashMap<>();
        data.put("categoryName", catProductBucketTO.getName());
        data.put("levelOneCategoryName", "宠物/宠物食品及用品");

        return Result.OK(data);
    }

    /**
     * 大类目下的小类目数据
     * @param condition
     * @return
     */
    private List<AggregationBuilder> buildCategoryTermsAgg(CatUnionProductCondition condition) {

        List<AggregationBuilder> builders = new ArrayList<>();

        TermsAggregationBuilder builder = AggregationBuilders
                .terms("category")
                .field("categoryName")
                .size(10);

        builders.add(builder);

        return builders;
    }

    // 检查关键词是否重复
    @RequestMapping(value = "/product/push/task/rule/check/keyword")
    @ResponseBody
    public Result<Boolean> checkKeyword(@Param("keyword") String keyword) {

        if (StringUtils.isBlank(keyword)) {
            return Result.ERROR(400, "关键词不能为空");
        }

        boolean checkKeyword = maochePushTaskRuleService.checkKeyword(keyword);
        if (!checkKeyword) {
            // 关键词不可用
            return Result.ERROR(400, "关键词已存在");
        }

        return Result.OK(checkKeyword);
    }

    // 获取规则模板 - specification
    @RequestMapping(value = "/product/push/task/rule/specification/template/get")
    @ResponseBody
    public Result<ProductSpecificationTemplateVO> addPushTaskRule() {

        // todo yhq

        return null;
    }
}
