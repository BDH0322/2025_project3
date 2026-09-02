package com.example.ufc.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /community/image/** 로 접근하면
        // 실제 C:/project/Data/community/image/ 폴더의 파일을 연결
        registry.addResourceHandler("/communityimages/**")
                .addResourceLocations("file:/C:/project/Data/community/image/");
    }

}
