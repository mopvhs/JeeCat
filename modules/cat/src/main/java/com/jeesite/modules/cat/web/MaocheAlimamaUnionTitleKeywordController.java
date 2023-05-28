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
import com.jeesite.modules.cat.entity.MaocheAlimamaUnionTitleKeywordDO;
import com.jeesite.modules.cat.service.MaocheAlimamaUnionTitleKeywordService;

/**
 * maoche_alimama_union_title_keywordController
 * @author YHQ
 * @version 2023-05-14
 */
@Controller
@RequestMapping(value = "${adminPath}/cat/maocheAlimamaUnionTitleKeywordDO")
public class MaocheAlimamaUnionTitleKeywordController extends BaseController {

	@Autowired
	private MaocheAlimamaUnionTitleKeywordService maocheAlimamaUnionTitleKeywordDOService;
	
	/**
	 * 获取数据
	 */
//	@ModelAttribute
//	public MaocheAlimamaUnionTitleKeywordDO get(Long id, boolean isNewRecord) {
//		return maocheAlimamaUnionTitleKeywordDOService.get(id, isNewRecord);
//	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionTitleKeywordDO:view")
	@RequestMapping(value = {"list", ""})
	public String list(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO, Model model) {
		model.addAttribute("maocheAlimamaUnionTitleKeywordDO", maocheAlimamaUnionTitleKeywordDO);
		return "modules/cat/maocheAlimamaUnionTitleKeywordDOList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionTitleKeywordDO:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<MaocheAlimamaUnionTitleKeywordDO> listData(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO, HttpServletRequest request, HttpServletResponse response) {
		maocheAlimamaUnionTitleKeywordDO.setPage(new Page<>(request, response));
		Page<MaocheAlimamaUnionTitleKeywordDO> page = maocheAlimamaUnionTitleKeywordDOService.findPage(maocheAlimamaUnionTitleKeywordDO);
		return page;
	}

	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionTitleKeywordDO:view")
	@RequestMapping(value = "form")
	public String form(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO, Model model) {
		model.addAttribute("maocheAlimamaUnionTitleKeywordDO", maocheAlimamaUnionTitleKeywordDO);
		return "modules/cat/maocheAlimamaUnionTitleKeywordDOForm";
	}

	/**
	 * 保存数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionTitleKeywordDO:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		maocheAlimamaUnionTitleKeywordDOService.save(maocheAlimamaUnionTitleKeywordDO);
		return renderResult(Global.TRUE, text("保存maoche_alimama_union_title_keyword成功！"));
	}
	
	/**
	 * 删除数据
	 */
	@RequiresPermissions("cat:maocheAlimamaUnionTitleKeywordDO:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(MaocheAlimamaUnionTitleKeywordDO maocheAlimamaUnionTitleKeywordDO) {
		maocheAlimamaUnionTitleKeywordDOService.delete(maocheAlimamaUnionTitleKeywordDO);
		return renderResult(Global.TRUE, text("删除maoche_alimama_union_title_keyword成功！"));
	}
	
}