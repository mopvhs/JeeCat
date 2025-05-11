package com.jeesite.modules.cat.service.cg.brand;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.enums.AuditStatusEnum;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.MaocheBrandLibraryIndex;
import com.jeesite.modules.cat.model.brandlib.BrandLibIndex;
import com.jeesite.modules.cat.service.cg.brand.dto.MatchKeywordDTO;
import com.jeesite.modules.cat.service.toolbox.CommandService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Slf4j
@Component
public class BrandLibTaskService {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;

    /**
     * 发单任务匹配品牌库
     * @return
     */
    public MatchKeywordDTO matchBrandLib(String content) {

        if (StringUtils.isBlank(content)) {
            return null;
        }
        log.info("原始：{}", content);
        // todo 处理掉链接和口令，以及emoji等
        content = filterContent(content);
        log.info("过滤：{}", content);
        QueryStringQueryBuilder queryString = QueryBuilders.queryStringQuery(content).field("productName").field("aliasNames", 2.0f);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("keywordCnt");
        rangeQuery.gte(0);

        boolQueryBuilder.must(rangeQuery);
        boolQueryBuilder.must(queryString);

        int size = 50;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0)
                .size(size)
                .query(boolQueryBuilder);

        ElasticSearchData<BrandLibIndex, Object> searchData = elasticSearch7Service.search(searchSourceBuilder,
                ElasticSearchIndexEnum.MAOCHE_BRAND_LIB_INDEX,
                null,
                CatRobotHelper::convertMaocheBrandLib,
                null);

        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            log.info("content ：{} ，未命中品牌", content);
            return null;
        }
        List<String> compareList = new ArrayList<>();
        BrandLibIndex lib = null;
        for(BrandLibIndex index : searchData.getDocuments()) {
            compareList.add(index.getProductName());
            if (content.contains(index.getProductName())) {
                lib = index;
                break;
            }
            if (CollectionUtils.isEmpty(index.getAliasNames())) {
                continue;
            }

            compareList.addAll(index.getAliasNames());
            for (String aliasName : index.getAliasNames()) {
                if (content.contains(aliasName)) {
                    lib = index;
                    break;
                }
            }
        }

        if (lib == null) {
            log.info("content ：{} ，从品牌列表中查询, compareList:{}", content, JsonUtils.toJSONString(compareList));
            return null;
        }

        // 查询关键词
        Long libId = lib.getId();

        List<MaocheBrandLibKeywordDO> keywords = maocheBrandLibKeywordDao.listByLibIds(Collections.singletonList(libId));

        if (CollectionUtils.isEmpty(keywords)) {
            log.info("content ：{} \n 命中品牌:{} \n 但是关键词为空", content, lib.getProductName());
            return null;
        }

        MaocheBrandLibKeywordDO match = null;
        for(MaocheBrandLibKeywordDO item : keywords) {
            String keyword = item.getKeyword();
            // 判断是否在文本内
            if (doMatch(content, keyword)) {
                match = item;
                break;
            }

            List<String> aliasNames = JsonUtils.toReferenceType(item.getAliasNames(), new TypeReference<List<String>>() {
            });
            if (CollectionUtils.isNotEmpty(aliasNames)) {
                for(String aliasName : aliasNames) {
                    if (doMatch(content, aliasName)) {
                        match = item;
                        break;
                    }
                }
            }

            if (match != null) {
                break;
            }
        }

        MatchKeywordDTO result = null;
        if (match != null) {
            log.info("content ：{} \n 命中品牌:{} \n 命中关键词：{}", content, lib.getProductName(), match.getKeyword());
            result  = new MatchKeywordDTO();
            result.setKeywordId(match.getIid());
            result.setKeyword(match.getKeyword());
            result.setBrandName(lib.getProductName());
            result.setLibId(match.getBrandLibId());
        }


        return result;
    }

    private static boolean doMatch(String content, String keyword) {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(keyword)) {
            return false;
        }

        return content.contains(keyword);
    }

    public static void main(String[] args) {


        String content = "自助查车@猫车选品官 +产品名";
        System.out.println(filterContent(content));
    }

    private static String filterContent(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        StringBuilder buf = new StringBuilder();
        // 先吃掉口令和 url
        String[] split = content.split("\n");
        for (String line : split) {
            // 如果这一行存在@,整行过滤
            if (line.contains("@")) {
                continue;
            }
            // 是否是url
            Matcher matcher = CommandService.jd.matcher(line);
            if (matcher.find()) {
                continue;
            }
            // 是否是口令
            matcher = CommandService.tb.matcher(line);
            if (matcher.find()) {
                continue;
            }
            buf.append(line);
        }
        content = buf.toString();

        // 吃掉所有的\n \t /  *
        content = content.replaceAll("\t", "");
        content = content.replaceAll("\n", "");
        content = content.replaceAll("/", "");
        content = content.replaceAll("\\*", "");
        content = content.replaceAll("\\(", "");
        content = content.replaceAll("\\)", "");
        content = content.replaceAll("\\+", "");
        content = content.replaceAll("\\^", "");
        content = content.replaceAll("!", "");
        content = content.replaceAll("\\?", "");
        content = content.replaceAll("-", "");
        content = content.replaceAll("@", "");
        content = content.replaceAll("，", "");
        content = content.replaceAll(",", "");
        content = content.replaceAll("。", "");
        content = content.replaceAll("\\.", "");
        content = content.replaceAll("~", "");
        content = content.replaceAll("\\[", "");
        content = content.replaceAll("\\]", "");

        content = filterEmoji(content);
        return content;
    }


    /**
     * 过滤emoji 或者 其他非文字类型的字符@author madaha
     *     @param source 待过滤字符串@return
     * @param source
     * @return
     */
    public static String filterEmoji(String source) {
        if (StringUtils.isBlank(source)) {
            return source;
        }
        return EmojiParser.removeAllEmojis(source);
    }
}
