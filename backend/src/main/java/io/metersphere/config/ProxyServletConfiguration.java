package io.metersphere.config;

import io.metersphere.proxy.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SolrProxyServletConfiguration {

  @Bean
  public ServletRegistrationBean servletRegistrationBean(){
    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new ProxyServlet(), "/solr/*");
//    servletRegistrationBean.addInitParameter("targetUri", "http://localhost:4444");
//    servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, "false");
    return servletRegistrationBean;
  }

}