package com.jeesite.modules.cat.service.cg;

import com.dtk.fetch.config.DtkFetchRequestParameters;
import com.dtk.fetch.consumer.IDtkConsumer;
import com.dtk.fetch.response.DtkGoodsInvalidResponse;
import com.dtk.fetch.response.DtkGoodsListResponse;
import com.dtk.fetch.response.DtkGoodsUpdateResponse;
import com.dtk.fetch.response.DtkPageResponse;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.enums.ProductDataSource;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheDataokeProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DtkConsumer implements IDtkConsumer {

    @Resource
    private MaocheDataokeProductService maocheDataokeProductService;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private CgUnionProductService cgUnionProductService;

    public static Map<Integer, Integer> updateMap;

    static {
        updateMap = new HashedMap();
    }

    public static Map<Integer, Integer> getUpdateMap() {
        return updateMap;
    }

    @Override
    public String initTime() {
        log.info("DtkConsumer initTime");
        return null;
    }

    @Override
    public DtkFetchRequestParameters buildRequestParameters() {
        log.info("DtkConsumer buildRequestParameters");
        return null;
    }

    @Override
    public void initConsume(DtkGoodsListResponse dtkGoodsListResponse) {
//        log.info("DtkConsumer initConsume");
        // 获取到商品数据
        DtkPageResponse<DtkGoodsListResponse.ItemInfo> data = dtkGoodsListResponse.getData();
        if (CollectionUtils.isEmpty(data.getList())) {
            return;
        }
        List<Long> dtkIds = data.getList().stream().map(i -> i.getId().longValue()).toList();

        List<MaocheDataokeProductDO> productDOs = Optional.ofNullable(maocheDataokeProductService.listByDtkIds(dtkIds)).orElse(new ArrayList<>());
        Map<Long, MaocheDataokeProductDO> productDOMap = productDOs.stream().collect(Collectors.toMap(MaocheDataokeProductDO::getDtkId, Function.identity(), (o1, o2) -> o1));


        // 存在的忽略，不存在的新增
        for (DtkGoodsListResponse.ItemInfo dtk : data.getList()) {
            long id = dtk.getId().longValue();
            MaocheDataokeProductDO product = productDOMap.get(id);
            if (product != null) {
                // 更新
//                maocheDataokeProductService.update(product, dtk);
//                log.info("更新商品信息: id:{}", product.getId());
                continue;
            }

            maocheDataokeProductService.insert(dtk);
        }

    }

    @Override
    public void updateConsume(DtkGoodsUpdateResponse dtkGoodsUpdateResponse) {
        if (dtkGoodsUpdateResponse == null || dtkGoodsUpdateResponse.getData() == null) {
            return;
        }
//        log.info("DtkConsumer updateConsume");
        DtkPageResponse<DtkGoodsUpdateResponse.ItemInfo> data = dtkGoodsUpdateResponse.getData();
        List<DtkGoodsUpdateResponse.ItemInfo> products = data.getList();
        if (CollectionUtils.isEmpty(products)) {
            return;
        }

        for (DtkGoodsUpdateResponse.ItemInfo item : products) {
            Integer integer = updateMap.get(item.getId());
            if (integer == null) {
                integer = 0;
            }
            updateMap.put(item.getId(), integer + 1);
        }

        List<String> ids = maocheDataokeProductService.updateProductInfo(products);
        if (CollectionUtils.isNotEmpty(ids)) {
            // 批量更新索引
            // 查询Product表
            List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByMaocheInnerIds(ids, ProductDataSource.DATAOKE);
            cgUnionProductService.indexEs(productDOs, 20);
        }
    }

    @Override
    public void invalidConsume(DtkGoodsInvalidResponse dtkGoodsInvalidResponse) {
        log.info("DtkConsumer invalidConsume");
        if (dtkGoodsInvalidResponse == null || dtkGoodsInvalidResponse.getData() == null) {
            return;
        }
        DtkPageResponse<DtkGoodsInvalidResponse.ItemInfo> response = dtkGoodsInvalidResponse.getData();
        if (CollectionUtils.isEmpty(response.getList())) {
            return;
        }
        List<DtkGoodsInvalidResponse.ItemInfo> itemInfos = response.getList();
        List<Long> dtkIds = itemInfos.stream().map(i -> i.getId().longValue()).toList();
        maocheDataokeProductService.delProduct(dtkIds);

        List<MaocheDataokeProductDO> products = maocheDataokeProductService.listByDtkIds(dtkIds);
        log.info("invalidConsume dtkIds : {}, size:{}", JsonUtils.toJSONString(dtkIds), products.size());
        if (CollectionUtils.isNotEmpty(products)) {
            List<MaocheAlimamaUnionProductDO> unionProductDOs = maocheAlimamaUnionProductService.listByMaocheInnerIds(products.stream().map(MaocheDataokeProductDO::getId).toList(), ProductDataSource.DATAOKE);
            if (CollectionUtils.isNotEmpty(unionProductDOs)) {
                List<Long> unionProductIds = unionProductDOs.stream().map(MaocheAlimamaUnionProductDO::getUiid).toList();
                boolean delete = maocheAlimamaUnionProductService.updateProductStatus(unionProductIds, "DELETE");
                log.info("invalidConsume delete ids:{}, delete:{}", JsonUtils.toJSONString(unionProductIds), delete);
                for (MaocheAlimamaUnionProductDO unionProductDO : unionProductDOs) {
                    log.info("invalidConsume del dtk id:{}", unionProductDO.getMaocheInnerId());
                    unionProductDO.setStatus("DELETE");
                }
                cgUnionProductService.indexEs(unionProductDOs, 20);
            }
        }

    }
}
