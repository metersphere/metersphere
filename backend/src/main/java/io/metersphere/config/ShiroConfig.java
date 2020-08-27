package io.metersphere.config;

import io.metersphere.commons.utils.ShiroUtils;
import io.metersphere.security.ApiKeyFilter;
import io.metersphere.security.LoginFilter;
import io.metersphere.security.ShiroDBRealm;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

@Configuration
@ConditionalOnProperty(prefix="sso",name = "mode", havingValue = "local", matchIfMissing = true)
public class ShiroConfig implements EnvironmentAware {
    private Environment env;

    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager sessionManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.getFilters().put("authc", new LoginFilter());
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSecurityManager(sessionManager);
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setSuccessUrl("/");

        shiroFilterFactoryBean.getFilters().put("apikey", new ApiKeyFilter());
        Map<String, String> filterChainDefinitionMap = shiroFilterFactoryBean.getFilterChainDefinitionMap();
        ShiroUtils.loadBaseFilterChain(filterChainDefinitionMap);
        filterChainDefinitionMap.put("/**", "apikey, authc");
        return shiroFilterFactoryBean;
    }

    @Bean(name = "shiroFilter")
    public FilterRegistrationBean<Filter> shiroFilter(ShiroFilterFactoryBean shiroFilterFactoryBean) throws Exception {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter((Filter) Objects.requireNonNull(shiroFilterFactoryBean.getObject()));
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Bean
    public MemoryConstrainedCacheManager memoryConstrainedCacheManager() {
        return new MemoryConstrainedCacheManager();
    }

    /**
     * securityManager 不用直接注入shiroDBRealm，可能会导致事务失效
     * 解决方法见 handleContextRefresh
     * http://www.debugrun.com/a/NKS9EJQ.html
     */
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(SessionManager sessionManager, MemoryConstrainedCacheManager memoryConstrainedCacheManager) {
        DefaultWebSecurityManager dwsm = new DefaultWebSecurityManager();
        dwsm.setSessionManager(sessionManager);
        dwsm.setCacheManager(memoryConstrainedCacheManager);
        return dwsm;
    }

    @Bean(name = "shiroDBRealm")
    @DependsOn("lifecycleBeanPostProcessor")
    public ShiroDBRealm getShiroDBRealm() {
        return new ShiroDBRealm();
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
        daap.setProxyTargetClass(true);
        return daap;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(DefaultWebSecurityManager sessionManager) {
        AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
        aasa.setSecurityManager(sessionManager);
        return new AuthorizationAttributeSourceAdvisor();
    }

    @Bean
    public SessionManager sessionManager(MemoryConstrainedCacheManager memoryConstrainedCacheManager) {
        Long sessionTimeout = env.getProperty("session.timeout", Long.class, 1800L); // 默认1800s, 半个小时
        return ShiroUtils.getSessionManager(sessionTimeout, memoryConstrainedCacheManager);
    }

    /**
     * 等到ApplicationContext 加载完成之后 装配shiroRealm
     */
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        ShiroDBRealm shiroDBRealm = (ShiroDBRealm) context.getBean("shiroDBRealm");
        ((DefaultWebSecurityManager) context.getBean("securityManager")).setRealm(shiroDBRealm);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
