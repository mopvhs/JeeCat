package com.jeesite.modules.cat.service.cg.brand;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.DateTimeUtils;
import com.jeesite.modules.cat.enums.ElasticSearchIndexEnum;
import com.jeesite.modules.cat.enums.task.TaskStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.helper.CatRobotHelper;
import com.jeesite.modules.cat.model.BrandLibTO;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.MaocheBrandLibraryIndex;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.condition.PushTaskIndexCondition;
import com.jeesite.modules.cat.model.ocean.OceanMessageCondition;
import com.jeesite.modules.cat.service.MaochePushTaskRuleService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import com.jeesite.modules.cat.service.cg.ocean.OceanSearchService;
import com.jeesite.modules.cat.service.es.dto.MaocheMessageProductIndex;
import com.jeesite.modules.cat.service.es.dto.PushTaskIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BrandLibConvertService {

    /**
     *
     * @param index
     * @return
     */
    public BrandLibTO convertBrandLibTO(MaocheBrandLibraryIndex index) {

        if (index == null) {
            return null;
        }

        BrandLibTO brandLibTO = new BrandLibTO();
        brandLibTO.setId(index.getId());
        brandLibTO.setBrand(index.getBrand());
        brandLibTO.setProductName(index.getProductName());
        // 字母
        brandLibTO.setBrandInitial(index.getBrandInitial());
        brandLibTO.setEnglishBrand(index.getEnglishBrand());
        brandLibTO.setKeywords(index.getKeyword());
        brandLibTO.setCategoryName(index.getCategoryName());
        brandLibTO.setLevelOneCategoryName(index.getLevelOneCategoryName());
        brandLibTO.setStar(index.getStar());
        brandLibTO.setDescription(index.getDescription());
        brandLibTO.setPolling(index.getPolling());
        // todo
//        private String specifications;		// specifications

        // 群今日新增 - 外部发单数量
        brandLibTO.setGroupDailyInc(index.getGroupDailyInc());
        // 库今日新增 - 此关键词新抓取到的商品
        brandLibTO.setProductDailyInc(index.getProductDailyInc());
        String pushDailyIncDesc = "未发布";
        if (index.getPushDailyInc() != null && index.getPushDailyInc() > 0) {
            pushDailyIncDesc = "已发布（" + index.getProductDailyInc() + "）";
        }
        brandLibTO.setPushDailyIncDesc(pushDailyIncDesc);
        // 历史推送
        brandLibTO.setHistoryPushCnt(Optional.ofNullable(index.getHistoryPushCnt()).orElse(0L));

        Long lastPushTime = index.getLastPushTime();
        Long nextPushTime = index.getNextPushTime();
        // 推送时间查询
        if (lastPushTime != null && lastPushTime > 0) {
            brandLibTO.setLastPushTime(new Date(lastPushTime));
            brandLibTO.setLastPushTimeDesc(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastPushTime) + "天前");
        }

        if (nextPushTime != null && nextPushTime > 0) {
            brandLibTO.setNextPushTime(new Date(nextPushTime));
            brandLibTO.setNextPushTimeDesc(DateTimeUtils.getStringDate(brandLibTO.getNextPushTime()));
        }

        brandLibTO.setTagIds(index.getTags());
        return brandLibTO;
    }
}
