package io.metersphere.api.dto.definition.request.dns;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.Host;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "DNSCacheManager")
public class MsDNSCacheManager extends MsTestElement {
    private String clazzName = MsDNSCacheManager.class.getCanonicalName();

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        for (MsTestElement el : hashTree) {
            el.toHashTree(tree, el.getHashTree(), config);
        }
    }

    public static void addEnvironmentVariables(HashTree samplerHashTree, String name, EnvironmentConfig config) {
        name += "Environment Variables";
        if (CollectionUtils.isNotEmpty(config.getCommonConfig().getVariables())) {
            samplerHashTree.add(arguments(name, config.getCommonConfig().getVariables()));
        }
    }

    public static void addEnvironmentDNS(HashTree samplerHashTree, String name, EnvironmentConfig config, HttpConfig httpConfig) {
        if (config.getCommonConfig().isEnableHost() && CollectionUtils.isNotEmpty(config.getCommonConfig().getHosts()) && httpConfig != null && httpConfig.getDomain() != null) {
            String domain = httpConfig.getDomain().trim();
            List<Host> hosts = new ArrayList<>();
            config.getCommonConfig().getHosts().forEach(host -> {
                if (StringUtils.isNotBlank(host.getDomain())) {
                    String hostDomain = host.getDomain().trim().replace("http://", "").replace("https://", "");
                    if (StringUtils.equals(hostDomain, domain)) {
                        host.setDomain(hostDomain); // 域名去掉协议
                        hosts.add(host);
                    }
                }
            });
            if (CollectionUtils.isNotEmpty(hosts)) {
                samplerHashTree.add(dnsCacheManager(name + " DNSCacheManager", hosts));
            }
        }
    }

    public static Map<String, String> getEnvironmentDns(EnvironmentConfig config, HttpConfig httpConfig) {
        Map<String, String> dnsMap = new HashMap<>();
        if (config.getCommonConfig().isEnableHost() && CollectionUtils.isNotEmpty(config.getCommonConfig().getHosts()) && httpConfig != null && httpConfig.getDomain() != null) {
            String domain = httpConfig.getDomain().trim();
            config.getCommonConfig().getHosts().forEach(host -> {
                if (StringUtils.isNotBlank(host.getDomain())) {
                    String hostDomain = host.getDomain().trim().replace("http://", "").replace("https://", "");
                    if (StringUtils.equals(hostDomain, domain)) {
                        dnsMap.put(hostDomain, host.getIp());
                    }else if(StringUtils.startsWith(hostDomain,domain+":")){
                        dnsMap.put(domain,StringUtils.replace(hostDomain,domain,host.getIp()));
                    }
                }
            });
        }
        return dnsMap;
    }

    private static Arguments arguments(String name, List<KeyValue> variables) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(name);
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        variables.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
        );
        return arguments;
    }

    private static DNSCacheManager dnsCacheManager(String name, List<Host> hosts) {
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.setEnabled(true);
        dnsCacheManager.setName(name);
        dnsCacheManager.setProperty(TestElement.TEST_CLASS, DNSCacheManager.class.getName());
        dnsCacheManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DNSCachePanel"));
        dnsCacheManager.setCustomResolver(true);
        hosts.forEach(host -> dnsCacheManager.addHost(host.getDomain(), host.getIp()));
        hosts.forEach(host -> dnsCacheManager.addServer(host.getIp()));

        return dnsCacheManager;
    }

}
