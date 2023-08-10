package com.jeesite.modules.cgcat;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.Result;
import com.jeesite.modules.cat.common.MtxHttpClientUtils;
import com.jeesite.modules.cat.service.FlameHttpService;
import com.jeesite.modules.cat.service.cg.image.ImageBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/api")
public class CgImageController {

    @Resource
    private FlameHttpService flameHttpService;

    @Resource
    private ImageBizService imageBizService;

    @RequestMapping(value = "/image/upload")
    @ResponseBody
    public Result<?> imageUpload(@RequestParam("fileName") String fileName, @RequestParam("multipartFile") MultipartFile multipartFile) {
        if (StringUtils.isBlank(fileName) || multipartFile == null) {
            return Result.error("参数错误");
        }

        // 获取token


        return Result.OK("完成");
    }

    @RequestMapping(value = "/image/token")
    @ResponseBody
    public Result<?> imageToken() {

        String daddy = imageBizService.getToken("cgwcge@qq.com", "aD43t3.xXftxWxBc");

        return Result.OK(daddy);
    }

}
