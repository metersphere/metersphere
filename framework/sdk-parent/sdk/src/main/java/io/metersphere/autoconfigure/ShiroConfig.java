package io.metersphere.autoconfigure;


import io.metersphere.commons.utils.ShiroUtils;
import io.metersphere.security.ApiKeyFilter;
import io.metersphere.security.CsrfFilter;
import io.metersphere.security.MsPermissionAnnotationMethodInterceptor;
import io.metersphere.security.realm.LocalRealm;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.aop.*;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.aop.SpringAnnotationResolver;
import org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.*;

public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager sessionManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setLoginUrl("/");
        shiroFilterFactoryBean.setSecurityManager(sessionManager);
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setSuccessUrl("/");

        shiroFilterFactoryBean.getFilters().put("apikey", new ApiKeyFilter());
        shiroFilterFactoryBean.getFilters().put("csrf", new CsrfFilter());
        Map<String, String> filterChainDefinitionMap = shiroFilterFactoryBean.getFilterChainDefinitionMap();

        ShiroUtils.loadBaseFilterChain(filterChainDefinitionMap);

        ShiroUtils.ignoreCsrfFilter(filterChainDefinitionMap);

        filterChainDefinitionMap.put("/**", "apikey, csrf, authc");
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

    @Bean
    public SessionManager sessionManager() {
        return new ServletContainerSessionManager();
    }

    /**
     * securityManager 不用直接注入 Realm，可能会导致事务失效
     * 解决方法见 handleContextRefresh
     */
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(SessionManager sessionManager, CacheManager cacheManager, Realm localRealm) {
        DefaultWebSecurityManager dwsm = new DefaultWebSecurityManager();
        dwsm.setSessionManager(sessionManager);
        dwsm.setCacheManager(cacheManager);
        dwsm.setRealm(localRealm);
        return dwsm;
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public LocalRealm localRealm() {
        return new LocalRealm();
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn({"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
        daap.setProxyTargetClass(true);
        return daap;
    }


    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(DefaultWebSecurityManager sessionManager) {
        AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
        aasa.setSecurityManager(sessionManager);
        AopAllianceAnnotationsAuthorizingMethodInterceptor advice = new AopAllianceAnnotationsAuthorizingMethodInterceptor();
        List<AuthorizingAnnotationMethodInterceptor> interceptors = new ArrayList<>(5);

        AnnotationResolver resolver = new SpringAnnotationResolver();
        interceptors.add(new RoleAnnotationMethodInterceptor(resolver));
        interceptors.add(new MsPermissionAnnotationMethodInterceptor(resolver));
        interceptors.add(new AuthenticatedAnnotationMethodInterceptor(resolver));
        interceptors.add(new UserAnnotationMethodInterceptor(resolver));
        interceptors.add(new GuestAnnotationMethodInterceptor(resolver));
        advice.setMethodInterceptors(interceptors);
        aasa.setAdvice(advice);
        return aasa;
    }

}
