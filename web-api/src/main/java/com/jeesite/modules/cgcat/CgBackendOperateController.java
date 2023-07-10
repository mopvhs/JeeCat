package com.jeesite.modules.cgcat;

import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionProductDao;
import com.jeesite.modules.cat.dao.MaocheAlimamaUnionTitleKeywordDao;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDetailDO;
import com.jeesite.modules.cat.enums.SaleStatusEnum;
import com.jeesite.modules.cat.es.config.es7.ElasticSearch7Service;
import com.jeesite.modules.cat.model.ProductAuditRequest;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductDetailService;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionProductService;
import com.jeesite.modules.cat.service.MaocheCategoryMappingService;
import com.jeesite.modules.cat.service.MaocheCategoryProductRelService;
import com.jeesite.modules.cat.service.MaocheCategoryService;
import com.jeesite.modules.cat.service.cg.CgUnionProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/backend/api/operate/")
public class CgBackendOperateController {

    @Resource
    private ElasticSearch7Service elasticSearch7Service;

    @Resource
    private CgUnionProductService cgUnionProductService;

    @Resource
    private MaocheAlimamaUnionProductDao maocheAlimamaUnionProductDao;

    @Resource
    private MaocheAlimamaUnionProductService maocheAlimamaUnionProductService;

    @Resource
    private MaocheAlimamaUnionTitleKeywordDao maocheAlimamaUnionTitleKeywordDao;

    @Resource
    private MaocheCategoryService maocheCategoryService;

    @Resource
    private MaocheCategoryProductRelService maocheCategoryProductRelService;

    @Resource
    private MaocheAlimamaUnionProductDetailService maocheAlimamaUnionProductDetailService;

    @Resource
    private MaocheCategoryMappingService maocheCategoryMappingService;

    // 审核状态变更
    @RequestMapping(value = "/product/quality/status/change")
    @ResponseBody
    public Result<Object> changeQualityStatus(@RequestBody ProductAuditRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getIds()) || request.getQualityStatus() == null) {
            return Result.ERROR(500, "参数错误");
        }

        // 批量更新
        List<Long> ids = request.getIds().stream().distinct().collect(Collectors.toList());

        // 更新索引
        List<MaocheAlimamaUnionProductDO> productDOs = maocheAlimamaUnionProductService.listByIds(ids);
        if (CollectionUtils.isEmpty(productDOs)) {
            return Result.ERROR(404, "资源不存在");
        }

        int row = maocheAlimamaUnionProductDao.updateQualityStatus(ids, request.getQualityStatus());
        if (row <= 0) {
            return Result.ERROR(500, "更新失败");
        }
        // 重新查一次数据库
        productDOs = maocheAlimamaUnionProductService.listByIds(ids);
        cgUnionProductService.indexEs(productDOs, 10);

        return Result.OK("处理完成");
    }
}
