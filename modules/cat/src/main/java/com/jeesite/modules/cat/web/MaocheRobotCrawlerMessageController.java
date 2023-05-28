package com.jeesite.modules.cat.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageDO;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageService;

/**
 * 信息采集表Controller
 * @author YHQ
 * @version 2023-04-30
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheRobotCrawlerMessageDO")
public class MaocheRobotCrawlerMessageController extends BaseController {

	@Autowired
	private MaocheRobotCrawlerMessageService maocheRobotCrawlerMessageDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheRobotCrawlerMessageDO get(Long id, boolean isNewRecord) {
//		return maocheRobotCrawlerMessageDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO, Model model) {
		model.addAttribute("maocheRobotCrawlerMessageDO", maocheRobotCrawlerMessageDO);
		return "modules/cat/maocheRobotCrawlerMessageDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheRobotCrawlerMessageDO> listData(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO, HttpServletRequest request, HttpServletResponse response) {
		maocheRobotCrawlerMessageDO.setPage(new Page<>(request, response));
		Page<MaocheRobotCrawlerMessageDO> page = maocheRobotCrawlerMessageDOService.findPage(maocheRobotCrawlerMessageDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO, Model model) {
		model.addAttribute("maocheRobotCrawlerMessageDO", maocheRobotCrawlerMessageDO);
		return "modules/cat/maocheRobotCrawlerMessageDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		maocheRobotCrawlerMessageDOService.save(maocheRobotCrawlerMessageDO);
		return renderResult(Global.TRUE, text("保存信息采集表成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheRobotCrawlerMessageDO maocheRobotCrawlerMessageDO) {
		maocheRobotCrawlerMessageDOService.delete(maocheRobotCrawlerMessageDO);
		return renderResult(Global.TRUE, text("删除信息采集表成功！"));
	}
	
}