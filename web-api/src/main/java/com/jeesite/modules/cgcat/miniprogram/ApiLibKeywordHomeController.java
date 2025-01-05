package com.jeesite.modules.cgcat.miniprogram;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheSubscribeDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheSubscribeDO;
import com.jeesite.modules.cat.entity.MaocheTagDO;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.subscribe.SubscribeStatusEnum;
import com.jeesite.modules.cat.enums.subscribe.SubscribeTypeEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.BrandLibKeywordIndex;
import com.jeesite.modules.cat.model.condition.BrandLibKeywordCondition;
import com.jeesite.modules.cat.service.MaocheBrandMapper;
import com.jeesite.modules.cat.service.MaocheTagService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.cg.third.RpaUserAdapter;
import com.jeesite.modules.cgcat.dto.subscribe.LibKeywordHomeRequest;
import com.jeesite.modules.cgcat.dto.subscribe.LibKeywordHomeVO;
import com.jeesite.modules.cgcat.dto.subscribe.LibKeywordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}")
public class ApiLibKeywordHomeController {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheBrandMapper maocheBrandMapper;

    @Resource
    private MaocheTagService maocheTagService;

    @Resource
    private MaocheSubscribeDao maocheSubscribeDao;

    @Resource
    private RpaUserAdapter rpaUserAdapter;

    @RequestMapping(value = "/api/maoche/mini/program/lib/keyword/detail/get")
    @ResponseBody
    public Result<LibKeywordHomeVO> detail(@RequestBody LibKeywordHomeRequest request, HttpServletRequest httpRequest) {

        if (request == null || request.getKeywordId() == null || request.getKeywordId() <= 0) {
            return Result.ERROR(500, "参数错误");
        }

        // todo 0. 查询用户信息是否正确
        String token = rpaUserAdapter.getToken(httpRequest);
        JSONObject user = rpaUserAdapter.getUser(token);
        if (user == null) {
            return Result.ERROR(300, "用户未登录");
        }
        String userId = String.valueOf(user.getLong("id"));
        int from = 0;
        int size = 1;
        BrandLibKeywordCondition condition = new BrandLibKeywordCondition();
        condition.setId(request.getKeywordId());
        BoolQueryBuilder boolBuilder = CatRobotHelper.buildQuery(condition, BrandLibKeywordCondition.class);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ES搜索条件
        searchSourceBuilder.query(boolBuilder);

        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        ElasticSearchData<BrandLibKeywordIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_KEYWORD_INDEX,
                null,
                OceanSearchService::convertBrandLibKeywordIndex,
                null);

        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return Result.ERROR(404, "资源不存在");
        }
        List<Long> tagIds = new ArrayList<>();
        List<Long> brandIds = new ArrayList<>();
        for (BrandLibKeywordIndex index : searchData.getDocuments()) {
            brandIds.add(index.getBrandId());
            if (CollectionUtils.isEmpty(index.getAliasNames()) && CollectionUtils.isNotEmpty(index.getTags())) {
                tagIds.addAll(index.getTags());
            }
        }
        brandIds = brandIds.stream().distinct().collect(Collectors.toList());
        tagIds = tagIds.stream().distinct().collect(Collectors.toList());
        // 查询品牌信息
        List<MaocheBrandDO> brands = maocheBrandMapper.listByIds(brandIds);
        Map<Long, MaocheBrandDO> brandMap = brands.stream().collect(Collectors.toMap(MaocheBrandDO::getIid, Function.identity(), (o1, o2) -> o1));
        // 获取标签
        List<MaocheTagDO> tags = maocheTagService.listByIds(tagIds);
        Map<Long, MaocheTagDO> tagMap = tags.stream().collect(Collectors.toMap(MaocheTagDO::getIid, Function.identity(), (o1, o2) -> o1));
        List<LibKeywordVO> keywords = LibKeywordVO.toVO(searchData.getDocuments(), brandMap, tagMap);

        if (CollectionUtils.isEmpty(keywords)) {
            return Result.ERROR(404, "资源不存在");
        }

        LibKeywordVO keywordVO = keywords.get(0);
        boolean subscribed = false;
        // 判断是否已经订阅
        if (StringUtils.isNotBlank(userId)) {
            MaocheSubscribeDO subscribe = maocheSubscribeDao.getUserSubscribe(userId, String.valueOf(request.getKeywordId()), SubscribeTypeEnum.BRAND_LIB_KEYWORD.getType());
            if (subscribe != null && subscribe.getStatus().equalsIgnoreCase(SubscribeStatusEnum.SUBSCRIBE.name())) {
                subscribed = true;
            }
        }
        keywordVO.setSubscribed(subscribed);
        LibKeywordHomeVO vo = new LibKeywordHomeVO();
        vo.setKeyword(keywordVO);
        vo.setDescTitle("猫车AI说");
        vo.setDesc("todo");
        return Result.OK(vo);
    }

    @RequestMapping(value = "/api/maoche/mini/program/lib/keyword/history/get")
    @ResponseBody
    public Result<Object> getHistory(LibKeywordHomeRequest request) {

        if (request == null || request.getKeywordId() == null || request.getKeywordId() <= 0) {
            return Result.ERROR(500, "参数错误");
        }

        return Result.OK(null);
    }
}
