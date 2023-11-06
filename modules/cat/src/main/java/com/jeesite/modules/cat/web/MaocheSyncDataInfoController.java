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
import com.jeesite.modules.cat.entity.MaocheSyncDataInfoDO;
import com.jeesite.modules.cat.service.MaocheSyncDataInfoService;

/**
 * 数据同步位点表Controller
 * @author YHQ
 * @version 2023-10-31
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheSyncDataInfoDO")
public class MaocheSyncDataInfoController extends BaseController {

	@Autowired
	private MaocheSyncDataInfoService maocheSyncDataInfoDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheSyncDataInfoDO get(Long id, boolean isNewRecord) {
//		return maocheSyncDataInfoDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheSyncDataInfoDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheSyncDataInfoDO maocheSyncDataInfoDO, Model model) {
		model.addAttribute("maocheSyncDataInfoDO", maocheSyncDataInfoDO);
		return "modules/cat/maocheSyncDataInfoDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheSyncDataInfoDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheSyncDataInfoDO> listData(MaocheSyncDataInfoDO maocheSyncDataInfoDO, HttpServletRequest request, HttpServletResponse response) {
		maocheSyncDataInfoDO.setPage(new Page<>(request, response));
		Page<MaocheSyncDataInfoDO> page = maocheSyncDataInfoDOService.findPage(maocheSyncDataInfoDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheSyncDataInfoDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheSyncDataInfoDO maocheSyncDataInfoDO, Model model) {
		model.addAttribute("maocheSyncDataInfoDO", maocheSyncDataInfoDO);
		return "modules/cat/maocheSyncDataInfoDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheSyncDataInfoDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		maocheSyncDataInfoDOService.save(maocheSyncDataInfoDO);
		return renderResult(Global.TRUE, text("保存数据同步位点表成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheSyncDataInfoDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheSyncDataInfoDO maocheSyncDataInfoDO) {
		maocheSyncDataInfoDOService.delete(maocheSyncDataInfoDO);
		return renderResult(Global.TRUE, text("删除数据同步位点表成功！"));
	}
	
}