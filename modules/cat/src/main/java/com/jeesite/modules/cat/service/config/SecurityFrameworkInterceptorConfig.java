package com.jeesite.modules.cat.service.config;

import com.jeesite.modules.cat.service.tracer.interceptor.SecurityFrameworkInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 后台管理日志记录拦截器
 * @author ThinkGem
 * @version 2018年1月10日
 */
@Configuration(proxyBeanMethods = false)
//@ConditionalOnProperty(name="web.interceptor.log.enabled", havingValue="true", matchIfMissing=true)
@EnableWebMvc
public class SecurityFrameworkInterceptorConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		InterceptorRegistration registration = registry.addInterceptor(new SecurityFrameworkInterceptor());
//		String apps = Global.getProperty("web.interceptor.log.addPathPatterns");
//		String epps = Global.getProperty("web.interceptor.log.excludePathPatterns");
//		for (String uri : StringUtils.split(apps, ",")){
//			if (StringUtils.isNotBlank(uri)){
//				registration.addPathPatterns(StringUtils.trim(uri));
//			}
//		}
		registration.addPathPatterns("/**");
//		for (String uri : StringUtils.split(epps, ",")){
//			if (StringUtils.isNotBlank(uri)){
//				registration.excludePathPatterns(StringUtils.trim(uri));
//			}
//		}
	}

}