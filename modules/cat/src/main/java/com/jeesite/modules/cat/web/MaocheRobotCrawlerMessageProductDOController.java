package com.jeesite.modules.cat.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageProductDO;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageProductService;

/**
 * 信息采集商品表Controller
 * @author YHQ
 * @version 2023-11-01
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheRobotCrawlerMessageProductDO")
public class MaocheRobotCrawlerMessageProductDOController extends BaseController {

	@Autowired
	private MaocheRobotCrawlerMessageProductService maocheRobotCrawlerMessageProductDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheRobotCrawlerMessageProductDO get(Long id, boolean isNewRecord) {
//		return maocheRobotCrawlerMessageProductDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageProductDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO, Model model) {
		model.addAttribute("maocheRobotCrawlerMessageProductDO", maocheRobotCrawlerMessageProductDO);
		return "modules/cat/maocheRobotCrawlerMessageProductDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageProductDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheRobotCrawlerMessageProductDO> listData(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO, HttpServletRequest request, HttpServletResponse response) {
		maocheRobotCrawlerMessageProductDO.setPage(new Page<>(request, response));
		Page<MaocheRobotCrawlerMessageProductDO> page = maocheRobotCrawlerMessageProductDOService.findPage(maocheRobotCrawlerMessageProductDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageProductDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO, Model model) {
		model.addAttribute("maocheRobotCrawlerMessageProductDO", maocheRobotCrawlerMessageProductDO);
		return "modules/cat/maocheRobotCrawlerMessageProductDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageProductDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		maocheRobotCrawlerMessageProductDOService.save(maocheRobotCrawlerMessageProductDO);
		return renderResult(Global.TRUE, text("保存信息采集商品表成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageProductDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheRobotCrawlerMessageProductDO maocheRobotCrawlerMessageProductDO) {
		maocheRobotCrawlerMessageProductDOService.delete(maocheRobotCrawlerMessageProductDO);
		return renderResult(Global.TRUE, text("删除信息采集商品表成功！"));
	}
	
}