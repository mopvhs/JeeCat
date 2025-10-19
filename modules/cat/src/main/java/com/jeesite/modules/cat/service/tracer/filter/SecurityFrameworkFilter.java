package com.jeesite.modules.cat.service.tracer.filter;

import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.model.MemberUserRespVO;
import com.jeesite.modules.cat.service.cg.third.RpaUserAdapter;
import com.jeesite.modules.cat.service.tracer.TraceInfo;
import com.jeesite.modules.cat.service.tracer.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
@WebFilter(urlPatterns = "/*", filterName = "securityFrameworkFilter")
public class SecurityFrameworkFilter implements Filter {

    @Resource
    private RpaUserAdapter rpaUserAdapter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("securityFrameworkFilter init 完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            if (!(request instanceof HttpServletRequest)) {
                return;
            }
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String token = rpaUserAdapter.getToken(httpRequest);
            if (StringUtils.isBlank(token)) {
                return;
            }
            MemberUserRespVO user = rpaUserAdapter.getUser(token);
            TraceInfo traceInfo = Tracer.getCurrentTraceInfo();
            traceInfo.setUser(user);

        } catch (Exception e) {
            log.error("SecurityFrameworkFilter exception ", e);
        } finally {
            chain.doFilter(request, response);
        }
    }




}
