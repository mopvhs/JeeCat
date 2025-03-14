package com.jeesite.modules.cgcat.miniprogram;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.dao.MaocheBrandLibKeywordDao;
import com.jeesite.modules.cat.dao.MaocheSubscribeDao;
import com.jeesite.modules.cat.entity.MaocheBrandDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibDO;
import com.jeesite.modules.cat.entity.MaocheBrandLibKeywordDO;
import com.jeesite.modules.cat.entity.MaocheSubscribeDO;
import com.jeesite.modules.cat.enums.subscribe.SubscribeOpenSwitchEnum;
import com.jeesite.modules.cat.enums.subscribe.SubscribeStatusEnum;
import com.jeesite.modules.cat.enums.subscribe.SubscribeTypeEnum;
import com.jeesite.modules.cat.service.MaocheBrandLibMapper;
import com.jeesite.modules.cat.service.MaocheBrandMapper;
import com.jeesite.modules.cat.service.MaocheSubscribeService;
import com.jeesite.modules.cat.service.cg.third.RpaUserAdapter;
import com.jeesite.modules.cgcat.dto.subscribe.SubscribeDetailVO;
import com.jeesite.modules.cgcat.dto.subscribe.SubscribeHomeDetailVO;
import com.jeesite.modules.cgcat.dto.subscribe.SubscribeHomeRequest;
import com.jeesite.modules.cgcat.dto.subscribe.SubscribeHomeVO;
import com.jeesite.modules.cgcat.dto.subscribe.SubscribeRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}")
public class ApiSubscribeController {

    @Resource
    private MaocheBrandLibMapper maocheBrandLibMapper;

    @Resource
    private MaocheBrandLibKeywordDao maocheBrandLibKeywordDao;

    @Resource
    private MaocheSubscribeService maocheSubscribeService;

    @Resource
    private MaocheSubscribeDao maocheSubscribeDao;

    @Resource
    private MaocheBrandMapper maocheBrandMapper;

    @Resource
    private RpaUserAdapter rpaUserAdapter;

    /**
     * 订阅&取消订阅
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/maoche/lib/subscribe")
    public Result<String> subscribe(@RequestBody SubscribeRequest request, HttpServletRequest httpRequest) {

        if (request == null || request.getSubId() == null || request.getSubId() <= 0) {
            return Result.ERROR(500, "参数错误");
        }

        // todo 0. 查询用户信息是否正确
        String token = rpaUserAdapter.getToken(httpRequest);
        JSONObject user = rpaUserAdapter.getUser(token);
        if (user == null) {
            return Result.ERROR(300, "用户未登录");
        }

        // 1. 查询品牌库是否存在
        MaocheBrandLibKeywordDO keywordDO = maocheBrandLibKeywordDao.getById(request.getSubId());
        if (keywordDO == null || !keywordDO.getStatus().equalsIgnoreCase("NORMAL")) {
            return Result.ERROR(404, "品牌库不存在");
        }
        Long userId = user.getLong("id");
        boolean isSubscribe = request.isSubscribe();
        SubscribeStatusEnum statusEnum = SubscribeStatusEnum.isSubscribe(isSubscribe);
        // 2. 查询是否已经有订阅记录
        MaocheSubscribeDO subscribe = maocheSubscribeDao.getUserSubscribe(String.valueOf(userId), String.valueOf(request.getSubId()), request.getSubType());
        if (subscribe != null) {
            // 判断是否已经订阅过
            if (isSubscribe) {
                if (SubscribeStatusEnum.SUBSCRIBE.name().equalsIgnoreCase(subscribe.getStatus())) {
                    return Result.OK("订阅成功");
                }
            } else {
                if (SubscribeStatusEnum.UNSUBSCRIBE.name().equalsIgnoreCase(subscribe.getStatus())) {
                    return Result.OK("取消订阅成功");
                }
            }

            // 更新
            int row = maocheSubscribeDao.updateById(subscribe.getId(), statusEnum.name(), null);
            if (row > 0) {
                // 同步修改计数
                if (isSubscribe) {
                    maocheBrandLibKeywordDao.incrSubscribeCount(request.getSubId(), 1);
                } else {
                    maocheBrandLibKeywordDao.decrSubscribeCount(request.getSubId(), -1);
                }
                return Result.OK("操作成功");
            } else {
                return Result.ERROR(500, "操作失败，请重试");
            }
        }

        // 3. 未订阅过的订阅操作
        if (!isSubscribe) {
            return Result.ERROR(500, "取消订阅操作错误，请重试");
        }
        MaocheSubscribeDO subscribeDO = new MaocheSubscribeDO();
        subscribeDO.setSubscribeId(String.valueOf(request.getSubId()));
        subscribeDO.setSubscribeType(request.getSubType());
        subscribeDO.setUserId(String.valueOf(userId));
        subscribeDO.setCid1(0L);
        subscribeDO.setCid2(0L);
        subscribeDO.setCategoryName(keywordDO.getCategoryName());
        subscribeDO.setLevelOneCategoryName(keywordDO.getLevelOneCategoryName());
        subscribeDO.setStatus(statusEnum.name());
        subscribeDO.setOpenSwitch(SubscribeOpenSwitchEnum.CLOSE.name());
        subscribeDO.setCreateBy(String.valueOf(userId));
        subscribeDO.setUpdateBy(String.valueOf(userId));
        subscribeDO.setCreateDate(new Date());
        subscribeDO.setUpdateDate(new Date());
        subscribeDO.setRemarks("");
        maocheSubscribeService.save(subscribeDO);

        if (StringUtils.isBlank(subscribeDO.getId())) {
            return Result.ERROR(500, "订阅失败");
        }

        // todo 新增订阅数量
        maocheBrandLibKeywordDao.incrSubscribeCount(request.getSubId(), 1);
        return Result.OK("订阅成功");
    }

    // 猫粮 猫砂 猫用品 驱虫保健
    // 我的订阅列表
    @RequestMapping(value = "/api/maoche/lib/subscribe/home")
    public Result<SubscribeHomeVO> subscribeHome(@RequestBody SubscribeHomeRequest request, HttpServletRequest httpRequest) {
        // 猫粮 猫砂 猫用品 驱虫保健
        // 获取我得全部订阅列表
        if (request == null) {
            return Result.ERROR(500, "参数错误");
        }
        SubscribeHomeVO homeVO = new SubscribeHomeVO();
        // 推荐订阅
        // todo 0. 查询用户信息是否正确
        String token = rpaUserAdapter.getToken(httpRequest);
        JSONObject user = rpaUserAdapter.getUser(token);
        if (user == null) {
            return Result.ERROR(300, "用户未登录");
        }
        String userId = String.valueOf(user.getLong("id"));

        List<MaocheSubscribeDO> list = maocheSubscribeDao.listUserSubscribes(userId, SubscribeTypeEnum.BRAND_LIB_KEYWORD.getType());
        if (CollectionUtils.isEmpty(list)) {
            // todo 获取推荐订阅
            return Result.OK(homeVO);
        }
        // 关键词id
        List<Long> libKeywordIds = new ArrayList<>();
        Map<String, List<MaocheSubscribeDO>> map = new HashMap<>();
        for(MaocheSubscribeDO item : list) {
            List<MaocheSubscribeDO> resources = map.get(item.getLevelOneCategoryName());
            if (CollectionUtils.isEmpty(resources)) {
                resources = new ArrayList<>();
            }

            libKeywordIds.add(NumberUtils.toLong(item.getSubscribeId()));

            resources.add(item);
            map.put(item.getLevelOneCategoryName(), resources);
        }

        Map<Long, MaocheBrandDO> brandMap = new HashMap<>();
        Map<Long, MaocheBrandLibDO> libMap = new HashMap<>();
        Map<Long, MaocheBrandLibKeywordDO> keywordMap = new HashMap<>();

        // 获取到关联的品牌库id
        List<MaocheBrandLibKeywordDO> keywordDOs = maocheBrandLibKeywordDao.listByIds(libKeywordIds);
        if (CollectionUtils.isEmpty(keywordDOs)) {
            return Result.OK(homeVO);
        }
        List<Long> libIds = keywordDOs.stream().map(MaocheBrandLibKeywordDO::getBrandLibId).toList();
        keywordMap = keywordDOs.stream().collect(Collectors.toMap(MaocheBrandLibKeywordDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<MaocheBrandLibDO> libDOs = maocheBrandLibMapper.listByIds(libIds);
        if (CollectionUtils.isEmpty(libDOs)) {
            return Result.OK(homeVO);
        }
        libMap = libDOs.stream().collect(Collectors.toMap(MaocheBrandLibDO::getIid, Function.identity(), (o1, o2) -> o1));

        List<Long> brandIds = libDOs.stream().map(MaocheBrandLibDO::getBrandId).toList();
        List<MaocheBrandDO> brandDOs = maocheBrandMapper.listByIds(brandIds);
        if (CollectionUtils.isEmpty(brandDOs)) {
            return Result.OK(homeVO);
        }
        brandMap = brandDOs.stream().collect(Collectors.toMap(MaocheBrandDO::getIid, Function.identity(), (o1, o2) -> o1));


        // 猫粮 猫砂 猫用品 驱虫保健顺序展示
        List<String> sort = new ArrayList<>();
        sort.add("猫粮");
        sort.add("猫砂");
        sort.add("猫咪用品");
        sort.add("驱虫保健");
        List<SubscribeHomeDetailVO> blocks = new ArrayList<>();
        for (String category : sort) {
            List<MaocheSubscribeDO> dos = map.get(category);
            if (CollectionUtils.isEmpty(dos)) {
                continue;
            }

            List<SubscribeDetailVO> details = new ArrayList<>();
            for (MaocheSubscribeDO subscribe : dos) {
                long keywordId = NumberUtils.toLong(subscribe.getSubscribeId());
                MaocheBrandLibKeywordDO keywordDO = keywordMap.get(keywordId);

                MaocheBrandLibDO libDO = libMap.get(keywordDO.getBrandLibId());
                if (libDO == null) {
                    continue;
                }

                MaocheBrandDO brandDO = brandMap.get(libDO.getBrandId());
                if (brandDO == null) {
                    continue;
                }
                // todo 这个地方 改成es
                SubscribeDetailVO detail = toSubscribeDetailVO(subscribe, keywordDO, brandDO);

                details.add(detail);
            }
            SubscribeHomeDetailVO detailVO = new SubscribeHomeDetailVO();
            detailVO.setTitle(category);
            detailVO.setDetails(details);

            blocks.add(detailVO);
        }
        homeVO.setBlocks(blocks);

        return Result.OK(homeVO);
    }

    public static SubscribeDetailVO toSubscribeDetailVO(MaocheSubscribeDO subscribe, MaocheBrandLibKeywordDO keywordDO, MaocheBrandDO brandDO) {
        if (subscribe == null) {
            return null;
        }
        SubscribeDetailVO vo = new SubscribeDetailVO();
        vo.setKeywordId(keywordDO.getIid());
        vo.setTitle(keywordDO.getKeyword());
        vo.setLogo(brandDO.getIcon());
        vo.setSubTitles(JsonUtils.toReferenceType(keywordDO.getAliasNames(), new TypeReference<List<String>>() {
        }));
        // todo 如果别名不存在，展示标签
        if (CollectionUtils.isEmpty(vo.getSubTitles())) {

        }
        List<Long> specialTags = JsonUtils.toReferenceType(keywordDO.getSpecialTags(), new TypeReference<List<Long>>() {
        });
        if (CollectionUtils.isNotEmpty(specialTags) && specialTags.contains(1)) {
            vo.setIcon("todo 金标");
        }
        vo.setOpenSwitch(subscribe.getOpenSwitch());

        return vo;
    }
}
