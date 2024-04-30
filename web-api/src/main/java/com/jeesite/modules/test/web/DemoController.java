/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.test.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.codec.EncodeUtils;
import com.jeesite.common.lang.NumberUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.utils.JsonUtils;
import com.jeesite.common.web.BaseController;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.jeesite.common.io.FileUtils.readFileToString;

/**
 * 演示实例Controller
 * @author ThinkGem
 * @version 2018-03-24
 */
@Controller
@RequestMapping(value = "${adminPath}/demo")
public class DemoController extends BaseController {


	public static void main(String[] args) {

		Date date = new Date();
		String strDateFormat = "dd";
		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

		String format = sdf.format(date);

		System.out.println(format);
	}

	public static long earliestTimeToday(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		now.set(11, 0);
		now.set(12, 0);
		now.set(13, 0);
		now.set(14, 0);
		return now.getTimeInMillis();
	}

}