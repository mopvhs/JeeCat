package com.jeesite.modules.cat.common;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SpringContextUtil implements ApplicationContextAware, BeanPostProcessor {

    private static ApplicationContext applicationContext;

    /**
     * 获取bean
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 获取bean
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 获取bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取bean
     * @param type
     * @param <T>
     * @return
     */
    public static <T> List<T> getBeansOfType(Class<T> type) {
        Collection<T> beans = applicationContext.getBeansOfType(type).values();
        if (CollectionUtils.isEmpty(beans)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(beans);
    }

    /**
     * 获取bean
     * @param type
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeanMapOfType(Class<T> type) {
        return applicationContext.getBeansOfType(type);
    }

    /**
     * 获取 applicationContext
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }
}
