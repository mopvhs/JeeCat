package com.jeesite.modules.cat.service.cg.task;

import com.jeesite.modules.cat.entity.MaochePushTaskDO;
import com.jeesite.modules.cat.enums.task.TaskResourceTypeEnum;
import com.jeesite.modules.cat.es.config.model.ElasticSearchData;
import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;
import com.jeesite.modules.cat.model.CatProductBucketTO;
import com.jeesite.modules.cat.model.condition.CatUnionProductCondition;
import com.jeesite.modules.cat.model.UnionProductTO;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TaskSearchBizService {

    @Resource
    private CgUnionProductService cgUnionProductService;

    public List<UnionProductTO> getPushTaskProducts(List<MaochePushTaskDO> pushTasks) {
        if (CollectionUtils.isEmpty(pushTasks)) {
            return null;
        }

        MaochePushTaskDO pushTaskDO = pushTasks.get(0);
        if (!TaskResourceTypeEnum.PRODUCT.name().equals(pushTaskDO.getResourceType())) {
            return null;
        }

        List<Long> ids = pushTasks.stream().map(MaochePushTaskDO::getResourceId).map(Long::valueOf).collect(Collectors.toList());

        CatUnionProductCondition condition = new CatUnionProductCondition();
        condition.setIds(ids);
        // 1. 索引数据
        ElasticSearchData<CarAlimamaUnionProductIndex, CatProductBucketTO> searchData = cgUnionProductService.searchProduct(condition, null, 0, ids.size());
        if (searchData == null || CollectionUtils.isEmpty(searchData.getDocuments())) {
            return null;
        }
        List<UnionProductTO> productTOs = cgUnionProductService.listProductInfo(searchData);
        if (CollectionUtils.isEmpty(productTOs)) {
            return null;
        }

        return productTOs;
    }
}
