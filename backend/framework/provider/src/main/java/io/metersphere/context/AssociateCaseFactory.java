package io.metersphere.context;

import io.metersphere.provider.BaseAssociateCaseProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 关联用例接口Bean实例上下文
 */
@Component
public class AssociateCaseFactory implements ApplicationContextAware {

	public static final Map<String, BaseAssociateCaseProvider> PROVIDER_MAP = new HashMap<>();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Map<String, BaseAssociateCaseProvider> beanMap = applicationContext.getBeansOfType(BaseAssociateCaseProvider.class);
		PROVIDER_MAP.putAll(beanMap);
	}

	public static BaseAssociateCaseProvider getInstance(String serviceType) {
		return PROVIDER_MAP.get(serviceType);
	}
}
