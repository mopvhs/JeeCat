package com.jeesite.modules.cat.xxl;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * 定时任务配置
 *
 * @author jeecg
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(value = XxlJobProperties.class)
public class XxlJobConfiguration {

    @Autowired
    private XxlJobProperties xxlJobProperties;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        //log.info(">>>> ip="+xxlJobProperties.getIp()+"，Port="+xxlJobProperties.getPort()+"，address="+xxlJobProperties.getAdminAddresses());
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobProperties.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(xxlJobProperties.getAppname());
        //update-begin--Author:scott -- Date:20210305 -- for：system服务和demo服务有办法同时使用xxl-job吗 #2313---
        // 获取本机的ip地址
        xxlJobSpringExecutor.setIp(xxlJobProperties.getIp());

        xxlJobSpringExecutor.setPort(xxlJobProperties.getPort());
        //update-end--Author:scott -- Date:20210305 -- for：system服务和demo服务有办法同时使用xxl-job吗 #2313---
        xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlJobProperties.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobProperties.getLogRetentionDays());

        return xxlJobSpringExecutor;
    }


    public static void main(String[] args) {

        try {
            InetAddress addr = InetAddress.getLocalHost();
            System.out.println(addr.getHostAddress());
        } catch (Exception e) {

        }
    }

}
