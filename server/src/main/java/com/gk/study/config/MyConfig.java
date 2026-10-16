package com.gk.study.config;

import com.gk.study.interceptor.AccessInterceptor;
import com.gk.study.utils.UploadPathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyConfig implements WebMvcConfigurer {

    @Value("${File.uploadPath:}")
    private String uploadPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")//项目中的所有接口都支持跨域
                .allowedOriginPatterns("*") //所有地址都可以访问，也可以配置具体地址
                .allowCredentials(true)
                .allowedMethods("*");//"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //添加资源映射规则，默认的前缀是：http://localhost:port
        registry.addResourceHandler("/staticfiles/image/**")
                .addResourceLocations(UploadPathUtils.resolveResourceLocation(uploadPath, "image"));
        registry.addResourceHandler("/staticfiles/avatar/**")
                .addResourceLocations(UploadPathUtils.resolveResourceLocation(uploadPath, "avatar"));
        registry.addResourceHandler("/staticfiles/banner/**")
                .addResourceLocations(UploadPathUtils.resolveResourceLocation(uploadPath, "banner"));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 自定义拦截器
        registry.addInterceptor(new AccessInterceptor());
    }
}
