package com.jeesite.modules.cgcat.brand.brandlib;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.service.cg.brandlib.BrandLibBizService;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibCreateRequest;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibKeywordCreateRequest;
import com.jeesite.modules.cat.xxl.job.brand.BrandLibSyncXxlJob;
import com.jeesite.modules.cat.xxl.job.brand.BrandSyncXxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * 品牌库
 */
@Slf4j
@Controller
@RequestMapping(value = "${adminPath}")
public class BrandLibController {

    @Resource
    private BrandLibBizService brandLibBizService;

    @Resource
    private BrandSyncXxlJob brandSyncXxlJob;

    @Resource
    private BrandLibSyncXxlJob brandLibSyncXxlJob;

    /**
     * 创建品牌库
     * @param query
     * @return
     */
    @RequestMapping(value = "/api/brand/lib/create")
    public Result<String> createBrandLib(@RequestBody BrandLibCreateRequest query) {
        if (query == null) {
            return Result.ERROR(500, "参数错误");
        }
        if (StringUtils.isBlank(query.getImg())) {
            return Result.ERROR(500, "图片不能为空");
        }
        if (query.getBrandId() == null || query.getBrandId() <= 0) {
            return Result.ERROR(500, "品牌不存在");
        }

        Result<MaocheBrandLibDO> brandLib = brandLibBizService.createBrandLib(query);

        if (Result.isOK(brandLib)) {
            return Result.OK("完成");
        }

        return Result.ERROR(500, "新增失败");
    }

    /**
     * 创建品牌库关键词
     * @param query
     * @return
     */
    @RequestMapping(value = "/api/brand/lib/keyword/create")
    public Result<String> createBrandLibKeyword(@RequestBody BrandLibKeywordCreateRequest query) {
        if (query == null) {
            return Result.ERROR(500, "参数错误");
        }
        if (query.getBrandLibId() == null || query.getBrandLibId() <= 0) {
            return Result.ERROR(500, "品牌库不存在");
        }
        if (StringUtils.isBlank(query.getKeyword())) {
            return Result.ERROR(500, "关键词不能为空");
        }

        Result<MaocheBrandLibKeywordDO> brandLib = brandLibBizService.createBrandLibKeyword(query);

        if (Result.isOK(brandLib)) {
            return Result.OK("完成");
        }

        return Result.ERROR(500, "新增失败");
    }

    /**
     * 同步品牌信息
     * @return
     */
    @RequestMapping(value = "/api/brand/sync/job")
    public Result<String> syncBrand() {
        try {
            brandSyncXxlJob.execute();
        } catch (Exception e) {
            log.error("品牌同步异常");
            return Result.ERROR(500, "同步异常" + e.getMessage());
        }

        return Result.OK("同步完成");
    }

    /**
     * 同步品牌库信息
     * @return
     */
    @RequestMapping(value = "/api/brand/lib/sync/job")
    public Result<String> syncBrandLib() {
        try {
            brandLibSyncXxlJob.execute();
        } catch (Exception e) {
            log.error("品牌库同步异常");
            return Result.ERROR(500, "同步异常" + e.getMessage());
        }

        return Result.OK("同步完成");
    }
}
