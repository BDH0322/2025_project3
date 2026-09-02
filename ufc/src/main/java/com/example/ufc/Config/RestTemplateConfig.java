package com.example.ufc.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    /**
     * RestTemplate Bean을 정의하여 Spring Container에 등록합니다.
     * 이제 다른 Service나 Controller에서 @Autowired를 통해 주입받을 수 있습니다.
     */
    @Bean
    public RestTemplate restTemplate(){

        return new RestTemplate();
    }
}
