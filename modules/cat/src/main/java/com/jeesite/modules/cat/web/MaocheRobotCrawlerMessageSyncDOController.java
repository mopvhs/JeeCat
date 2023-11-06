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
import com.jeesite.modules.cat.entity.MaocheRobotCrawlerMessageSyncDO;
import com.jeesite.modules.cat.service.MaocheRobotCrawlerMessageSyncService;

/**
 * 信息采集表Controller
 * @author YHQ
 * @version 2023-11-01
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheRobotCrawlerMessageSyncDO")
public class MaocheRobotCrawlerMessageSyncDOController extends BaseController {

	@Autowired
	private MaocheRobotCrawlerMessageSyncService maocheRobotCrawlerMessageSyncDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheRobotCrawlerMessageSyncDO get(Long id, boolean isNewRecord) {
//		return maocheRobotCrawlerMessageSyncDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageSyncDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO, Model model) {
		model.addAttribute("maocheRobotCrawlerMessageSyncDO", maocheRobotCrawlerMessageSyncDO);
		return "modules/cat/maocheRobotCrawlerMessageSyncDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageSyncDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheRobotCrawlerMessageSyncDO> listData(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO, HttpServletRequest request, HttpServletResponse response) {
		maocheRobotCrawlerMessageSyncDO.setPage(new Page<>(request, response));
		Page<MaocheRobotCrawlerMessageSyncDO> page = maocheRobotCrawlerMessageSyncDOService.findPage(maocheRobotCrawlerMessageSyncDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageSyncDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO, Model model) {
		model.addAttribute("maocheRobotCrawlerMessageSyncDO", maocheRobotCrawlerMessageSyncDO);
		return "modules/cat/maocheRobotCrawlerMessageSyncDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageSyncDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		maocheRobotCrawlerMessageSyncDOService.save(maocheRobotCrawlerMessageSyncDO);
		return renderResult(Global.TRUE, text("保存信息采集表成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheRobotCrawlerMessageSyncDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheRobotCrawlerMessageSyncDO maocheRobotCrawlerMessageSyncDO) {
		maocheRobotCrawlerMessageSyncDOService.delete(maocheRobotCrawlerMessageSyncDO);
		return renderResult(Global.TRUE, text("删除信息采集表成功！"));
	}
	
}