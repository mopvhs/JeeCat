package com.jeesite.modules.cat.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dtk.fetch.response.DtkGoodsListResponse;
import com.dtk.fetch.response.DtkGoodsUpdateResponse;
import com.jeesite.common.utils.JsonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.cat.entity.MaocheDataokeProductDO;
import com.jeesite.modules.cat.dao.MaocheDataokeProductDao;

/**
 * maoche_dataoke_productService
 * @author YHQ
 * @version 2023-06-04
 */
@Service
public class MaocheDataokeProductService extends CrudService<MaocheDataokeProductDao, MaocheDataokeProductDO> {

	/**
	 * 获取单条数据
	 * @param maocheDataokeProductDO
	 * @return
	 */
	@Override
	public MaocheDataokeProductDO get(MaocheDataokeProductDO maocheDataokeProductDO) {
		return super.get(maocheDataokeProductDO);
	}

	/**
	 * 查询分页数据
	 * @param maocheDataokeProductDO 查询条件
	 * @param maocheDataokeProductDO page 分页对象
	 * @return
	 */
	@Override
	public Page<MaocheDataokeProductDO> findPage(MaocheDataokeProductDO maocheDataokeProductDO) {
		return super.findPage(maocheDataokeProductDO);
	}

	/**
	 * 查询列表数据
	 * @param maocheDataokeProductDO
	 * @return
	 */
	@Override
	public List<MaocheDataokeProductDO> findList(MaocheDataokeProductDO maocheDataokeProductDO) {
		return super.findList(maocheDataokeProductDO);
	}

	/**
	 * 保存数据（插入或更新）
	 * @param maocheDataokeProductDO
	 */
	@Override
	@Transactional
	public void save(MaocheDataokeProductDO maocheDataokeProductDO) {
		super.save(maocheDataokeProductDO);
	}

	/**
	 * 更新状态
	 * @param maocheDataokeProductDO
	 */
	@Override
	@Transactional
	public void updateStatus(MaocheDataokeProductDO maocheDataokeProductDO) {
		super.updateStatus(maocheDataokeProductDO);
	}

	/**
	 * 删除数据
	 * @param maocheDataokeProductDO
	 */
	@Override
	@Transactional
	public void delete(MaocheDataokeProductDO maocheDataokeProductDO) {
		super.delete(maocheDataokeProductDO);
	}

	public List<MaocheDataokeProductDO> listByDtkIds(List<Long> dtkIds) {
		if (CollectionUtils.isEmpty(dtkIds)) {
			return new ArrayList<>();
		}

		MaocheDataokeProductDO query = new MaocheDataokeProductDO();

		query.setDtkId_in(dtkIds.toArray(new Long[0]));

		return dao.findList(query);
	}


	public List<MaocheDataokeProductDO> listByIds(List<String> dtkIds) {
		if (CollectionUtils.isEmpty(dtkIds)) {
			return new ArrayList<>();
		}

		MaocheDataokeProductDO query = new MaocheDataokeProductDO();

		query.setId_in(dtkIds.toArray(new String[0]));

		return dao.findList(query);
	}
	public boolean insert(DtkGoodsListResponse.ItemInfo itemInfo) {
		if (itemInfo == null) {
			return false;
		}

		long originalPrice = factorValue(itemInfo.getOriginalPrice(), 100);
		long actualPrice = factorValue(itemInfo.getActualPrice(), 100);
		long couponPrice = factorValue(itemInfo.getCouponPrice(), 100);
		long commissionRate = factorValue(itemInfo.getCommissionRate(), 100);
		long monthSales = factorValue(itemInfo.getMonthSales(), 1);
		long hotPush = factorValue(itemInfo.getHotPush(), 1);
		long couponReceiveNum = (long) Optional.ofNullable(itemInfo.getCouponReceiveNum()).orElse(0);
		long couponRemainCount = (long) Optional.ofNullable(itemInfo.getCouponTotalNum()).orElse(0);

		MaocheDataokeProductDO product = new MaocheDataokeProductDO();
		product.setDtkId(itemInfo.getId().longValue());
		product.setGoodsId(itemInfo.getGoodsId());
		product.setOrigContent(JsonUtils.toJSONString(itemInfo));

		product.setOriginalPrice(originalPrice);
		product.setActualPrice(actualPrice);
		product.setCouponPrice(couponPrice);
		product.setCommissionRate(commissionRate);
		product.setMonthSales(monthSales);
		product.setHotPush(hotPush);
		product.setCouponReceiveNum(couponReceiveNum);
		product.setCouponRemainCount(couponRemainCount);

		product.setCreateBy("consumer");
		product.setUpdateBy("consumer");
		product.setCreateDate(new Date());
		product.setUpdateDate(new Date());
		product.setRemarks("");
		product.setStatus("normal");

		return dao.insert(product) > 0;
	}

	public boolean update(MaocheDataokeProductDO product, DtkGoodsListResponse.ItemInfo itemInfo) {
		if (itemInfo == null || product == null) {
			return false;
		}

		long originalPrice = factorValue(itemInfo.getOriginalPrice(), 100);
		long actualPrice = factorValue(itemInfo.getActualPrice(), 100);
		long couponPrice = factorValue(itemInfo.getCouponPrice(), 100);
		long commissionRate = factorValue(itemInfo.getCommissionRate(), 100);
		long monthSales = factorValue(itemInfo.getMonthSales(), 1);
		long hotPush = factorValue(itemInfo.getHotPush(), 1);
		long couponReceiveNum = (long) Optional.ofNullable(itemInfo.getCouponReceiveNum()).orElse(0);
		long couponRemainCount = (long) Optional.ofNullable(itemInfo.getCouponTotalNum()).orElse(0);

		product.setDtkId(itemInfo.getId().longValue());
		product.setGoodsId(itemInfo.getGoodsId());
		product.setOrigContent(JsonUtils.toJSONString(itemInfo));

		product.setOriginalPrice(originalPrice);
		product.setActualPrice(actualPrice);
		product.setCouponPrice(couponPrice);
		product.setCommissionRate(commissionRate);
		product.setMonthSales(monthSales);
		product.setHotPush(hotPush);
		product.setCouponReceiveNum(couponReceiveNum);
		product.setCouponRemainCount(couponRemainCount);

		product.setUpdateBy("consumer");
		product.setRemarks("");
		product.setStatus("normal");

		return dao.update(product) > 0;
	}
	public List<String> updateProductInfo(List<DtkGoodsUpdateResponse.ItemInfo> items) {
		if (CollectionUtils.isEmpty(items)) {
			return new ArrayList<>();
		}

		List<Long> dtkIds = items.stream().map(i -> i.getId().longValue()).toList();
		Map<Integer, DtkGoodsUpdateResponse.ItemInfo> itemInfoMap = items.stream().collect(Collectors.toMap(DtkGoodsUpdateResponse.ItemInfo::getId, Function.identity(), (o1, o2) -> o1));

		List<MaocheDataokeProductDO> products = listByDtkIds(dtkIds);
		if (CollectionUtils.isEmpty(products)) {
			return new ArrayList<>();
		}

		List<String> updateIds = new ArrayList<>();
		for (MaocheDataokeProductDO product : products) {
			DtkGoodsUpdateResponse.ItemInfo itemInfo = itemInfoMap.get(product.getDtkId().intValue());
			if (itemInfo == null) {
				continue;
			}
			long originalPrice = factorValue(itemInfo.getOriginalPrice(), 100);
			long actualPrice = factorValue(itemInfo.getActualPrice(), 100);
			long couponPrice = factorValue(itemInfo.getCouponPrice(), 100);
			long commissionRate = factorValue(itemInfo.getCommissionRate(), 100);
			long monthSales = factorValue(itemInfo.getMonthSales(), 1);
			long hotPush = factorValue(itemInfo.getHotPush(), 1);
			long couponReceiveNum = (long) Optional.ofNullable(itemInfo.getCouponReceiveNum()).orElse(0);
			long couponRemainCount = (long) Optional.ofNullable(itemInfo.getCouponRemainCount()).orElse(0);
			List<String> specialText = itemInfo.getSpecialText();

			boolean same = true;
			if (!theLongSame(product.getOriginalPrice(), originalPrice)) {
				same = false;
			} else if (!theLongSame(product.getActualPrice(), actualPrice)) {
				same = false;
			} else if (!theLongSame(product.getCouponPrice(), couponPrice)) {
				same = false;
			} else if (!theLongSame(product.getCommissionRate(), commissionRate)) {
				same = false;
			} else if (!theLongSame(product.getMonthSales(), monthSales)) {
				same = false;
			} else if (!theLongSame(product.getCouponReceiveNum(), couponReceiveNum)) {
				same = false;
			} else if (!theLongSame(product.getCouponRemainCount(), couponRemainCount)) {
				same = false;
			}

			if (same) {
				continue;
			}

			dao.updateProduct(product.getId(),
					originalPrice,
					actualPrice,
					couponPrice,
					commissionRate,
					monthSales,
					JsonUtils.toJSONString(specialText),
					couponRemainCount,
					couponReceiveNum,
					"consumer"
					);

			updateIds.add(product.getId());
		}

		return updateIds;
	}

	public void delProduct(List<Long> dtkIds) {
		if (CollectionUtils.isEmpty(dtkIds)) {
			return;
		}

		dao.updateStatus(dtkIds, "DELETE");
	}

	private static long factorValue(BigDecimal num, int factor) {
		if (num == null) {
			return 0;
		}

		return num.multiply(new BigDecimal(factor)).longValue();
	}

	private static boolean theLongSame(Long source, Long target) {
		if (source == null && target == null) {
			return true;
		}
		if (source == null || target == null) {
			return false;
		}

		return source.equals(target);
	}
}