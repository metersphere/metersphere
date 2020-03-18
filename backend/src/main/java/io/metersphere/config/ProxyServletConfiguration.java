package io.metersphere.config;

import io.metersphere.proxy.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyServletConfiguration {

  @Bean
  public ServletRegistrationBean servletRegistrationBean(){
    //代理到hub节点获取录像
    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new ProxyServlet(), "/proxy/*");
    return servletRegistrationBean;
  }

}