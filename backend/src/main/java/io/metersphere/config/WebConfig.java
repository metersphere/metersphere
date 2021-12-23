package io.metersphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return getTimeOutTemplate(10 * 1000, 10 * 1000, 30 * 1000);
    }

    @Bean
    public RestTemplate restTemplateWithTimeOut() {
        return getTimeOutTemplate(4000, 4000, 10 * 1000);
    }

    private RestTemplate getTimeOutTemplate(int requestTimeout, int connectTimeout, int readTimeout) {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(requestTimeout);
        httpRequestFactory.setConnectTimeout(connectTimeout);
        httpRequestFactory.setReadTimeout(readTimeout);
        restTemplate.setRequestFactory(httpRequestFactory);
        return restTemplate;
    }
}
