package io.metersphere.eureka.config;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SyncEurekaServer implements ApplicationRunner {
    private Logger logger = LoggerFactory.getLogger(SyncEurekaServer.class);

    private static final String KUBERNETES_ENVIRONMENT = "KUBERNETES_PORT";

    private static final String DEFAULT_ZONE_PROPERTIES = "eureka.client.service-url.defaultZone";

    private static final String EUREKA_SERVERS = "EUREKA_SERVERS";

    private static String _localIp = StringUtils.EMPTY;

    private static boolean IS_KUBERNETES = false;

    @Resource
    private Environment environment;

    @Resource
    private EurekaClientConfigBean eurekaClientConfigBean;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        init();
    }

    @Scheduled(fixedDelay = 15000)
    public void syncEurekaServer() {
        if (!IS_KUBERNETES) {
            return;
        }
        String localIp = registerSelf();
        List<String> members = getMembers();

        if (StringUtils.isNotBlank(localIp)) {
            members.remove(localIp);
        }
        List<String> serverUrls = convert2ARecordUrl(members);
        if (logger.isTraceEnabled()) {
            logger.trace("serverUrls: " + serverUrls);
        }
        System.out.println(serverUrls);
        eurekaClientConfigBean.getServiceUrl().put(EurekaClientConfigBean.DEFAULT_ZONE, StringUtils.join(serverUrls, ","));
        if (logger.isTraceEnabled()) {
            logger.trace("getServiceUrl: " + eurekaClientConfigBean.getServiceUrl());
        }
    }

    public void init() {
        if (!isKubernetes()) {
            logger.info("Not Kubernetes Deployment.");
            return;
        }
        logger.info("syncEurekaServer start.");
        String localIp = registerSelf();
        logger.info("Self registered: " + localIp);
    }

    private String registerSelf() {
        String localIp = getLocalIp();
        if (StringUtils.isBlank(localIp)) {
            return localIp;
        }
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(EUREKA_SERVERS, localIp);
        if (BooleanUtils.isTrue(isMember)) {
            return localIp;
        }
        stringRedisTemplate.opsForSet().add(EUREKA_SERVERS, localIp);
        return localIp;
    }

    private List<String> convert2ARecordUrl(List<String> members) {
        String defaultUrl = environment.getProperty(DEFAULT_ZONE_PROPERTIES);
        List<String> result = new ArrayList<>();
        try {
            String host = new URL(defaultUrl).getHost();
            members.forEach(ip -> result.add(RegExUtils.replaceFirst(defaultUrl, host, ip)));
        } catch (Exception e) {
            // do nothing
        }
        if (CollectionUtils.isEmpty(result)) {
            result.add(defaultUrl);
        }
        return result;
    }

    private List<String> getMembers() {
        List<String> result = new ArrayList<>();
        try {
            Set<String> members = stringRedisTemplate.opsForSet().members(EUREKA_SERVERS);
            if (CollectionUtils.isEmpty(members)) {
                return result;
            }
            members.forEach(memberIp -> {
                try {
                    if (InetAddress.getByName(memberIp).isReachable(3000)) {
                        result.add(memberIp);
                    } else {
                        stringRedisTemplate.opsForSet().remove(EUREKA_SERVERS, memberIp);
                    }
                } catch (Exception ignore) {
                }
            });
        } catch (Exception ignore) {
        }
        return result;
    }

    private boolean isKubernetes() {
        Map<String, String> map = System.getenv();
        logger.debug("环境变量: " + map);
        IS_KUBERNETES = map.keySet().stream().anyMatch(key -> StringUtils.equals(key, KUBERNETES_ENVIRONMENT));
        return IS_KUBERNETES;
    }


    private String getLocalIp() {
        if (StringUtils.isNotBlank(_localIp)) {
            return _localIp;
        }
        try {
            InetAddress address = InetAddress.getLocalHost();
            _localIp = address.getHostAddress();
        } catch (Exception e) {
            logger.error("failed to getLocalIp", e);
        }
        return _localIp;
    }


}