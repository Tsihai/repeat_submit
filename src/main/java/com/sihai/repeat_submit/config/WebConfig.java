package com.sihai.repeat_submit.config;

import com.sihai.repeat_submit.filter.RepeatableRequestFilter;
import com.sihai.repeat_submit.interceptor.RepeatSubmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    RepeatSubmitInterceptor repeatSubmitInterceptor;

    /**
     * 注册拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对repeatSubmitInterceptor添加拦截器
        // 拦截所有请求
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    /**
     * 处理请求可重复提交的过滤器
     * @return
     */
    @Bean
    FilterRegistrationBean<RepeatableRequestFilter> repeatableRequestFilterFilterRegistrationBean() {
        FilterRegistrationBean<RepeatableRequestFilter> bean = new FilterRegistrationBean();
        bean.setFilter(new RepeatableRequestFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }
}
