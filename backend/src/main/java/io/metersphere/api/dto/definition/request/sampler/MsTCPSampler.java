package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.automation.EsbDataStruct;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.DelimiterConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "TCPSampler")
public class MsTCPSampler extends MsTestElement {
    @JSONField(ordinal = 20)
    private String type = "TCPSampler";
    @JSONField(ordinal = 21)
    private String classname = "";
    @JSONField(ordinal = 22)
    private String server = "";
    @JSONField(ordinal = 23)
    private String port = "";
    @JSONField(ordinal = 24)
    private String ctimeout = "";
    @JSONField(ordinal = 25)
    private String timeout = "";
    @JSONField(ordinal = 26)
    private boolean reUseConnection = true;
    @JSONField(ordinal = 27)
    private boolean nodelay;
    @JSONField(ordinal = 28)
    private boolean closeConnection;
    @JSONField(ordinal = 29)
    private String soLinger = "";
    @JSONField(ordinal = 30)
    private String eolByte = "";
    @JSONField(ordinal = 31)
    private String username = "";
    @JSONField(ordinal = 32)
    private String password = "";
    @JSONField(ordinal = 33)
    private String request;
    @JSONField(ordinal = 35)
    private List<KeyValue> parameters;
    @JSONField(ordinal = 36)
    private String useEnvironment;
    @JSONField(ordinal = 37)
    private MsJSR223PreProcessor tcpPreProcessor;
    @JSONField(ordinal = 38)
    private String protocol = "TCP";
    @JSONField(ordinal = 39)
    private String projectId;
    @JSONField(ordinal = 40)
    private String connectEncoding;
    @JSONField(ordinal = 41)
    private String reportType;
    @JSONField(ordinal = 42)
    private List<TcpTreeTableDataStruct> xmlDataStruct;
    @JSONField(ordinal = 43)
    private String jsonDataStruct;
    @JSONField(ordinal = 44)
    private String rawDataStruct;


    /**
     * 新加两个参数，场景保存/修改时需要的参数。不会传递JMeter，只是用于最后的保留。
     */
    private List<EsbDataStruct> esbDataStruct;
    private List<EsbDataStruct> backEsbDataStruct;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            this.setRefElement();
        }
        if (config.getConfig() == null) {
            // 单独接口执行
            this.setProjectId(config.getProjectId());
            config.setConfig(getEnvironmentConfig(useEnvironment));
        }
        if (config.getConfig() != null) {
            parseEnvironment(config.getConfig().get(this.projectId));
        }

        // 添加环境中的公共变量
        Arguments arguments = this.addArguments(config);
        if (arguments != null) {
            tree.add(arguments);
        }

        final HashTree samplerHashTree = new ListedHashTree();
        samplerHashTree.add(tcpConfig());
        tree.set(tcpSampler(config), samplerHashTree);
        setUserParameters(samplerHashTree);
        if (tcpPreProcessor != null && StringUtils.isNotBlank(tcpPreProcessor.getScript())) {
            samplerHashTree.add(tcpPreProcessor.getJSR223PreProcessor());
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(samplerHashTree, el.getHashTree(), config);
            });
        }
    }

    private void setRefElement() {
        try {
            ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MsTCPSampler proxy = null;
            if (StringUtils.equals(this.getRefType(), "CASE")) {
                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
                if (bloBs != null) {
                    this.setName(bloBs.getName());
                    this.setProjectId(bloBs.getProjectId());
                    proxy = mapper.readValue(bloBs.getRequest(), new TypeReference<MsTCPSampler>() {
                    });
                }
            } else {
                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
                if (apiDefinition != null) {
                    this.setName(apiDefinition.getName());
                    this.setProjectId(apiDefinition.getProjectId());
                    proxy = mapper.readValue(apiDefinition.getRequest(), new TypeReference<MsTCPSampler>() {
                    });
                }
            }
            if (proxy != null) {
                this.setHashTree(proxy.getHashTree());
                this.setClassname(proxy.getClassname());
                this.setServer(proxy.getServer());
                this.setPort(proxy.getPort());
                this.setRequest(proxy.getRequest());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.error(ex.getMessage());
        }
    }

    private void parseEnvironment(EnvironmentConfig config) {
        if (!isCustomizeReq() && config != null && config.getTcpConfig() != null) {
            this.server = config.getTcpConfig().getServer();
            this.port = config.getTcpConfig().getPort();
            if(StringUtils.equals(this.eolByte," ")){
                this.eolByte = "";
            }else{
                if(StringUtils.isEmpty(this.eolByte)){
                    this.eolByte = config.getTcpConfig().getEolByte();
                }
            }

        }
    }

    private TCPSampler tcpSampler(ParameterConfig config) {
        TCPSampler tcpSampler = new TCPSampler();
        tcpSampler.setEnabled(this.isEnable());
        tcpSampler.setName(this.getName());
        String name = this.getParentName(this.getParent());
        if (StringUtils.isNotEmpty(name) && !config.isOperating()) {
            tcpSampler.setName(this.getName() + DelimiterConstants.SEPARATOR.toString() + name);
        }
        tcpSampler.setProperty("MS-ID", this.getId());
        tcpSampler.setProperty("MS-RESOURCE-ID", this.getResourceId());
        List<String> id_names = new LinkedList<>();
        this.getScenarioSet(this, id_names);
        tcpSampler.setProperty("MS-SCENARIO", JSON.toJSONString(id_names));

        tcpSampler.setProperty(TestElement.TEST_CLASS, TCPSampler.class.getName());
        tcpSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TCPSamplerGui"));
        tcpSampler.setClassname(this.getClassname());
        tcpSampler.setServer(this.getServer());
        tcpSampler.setPort(this.getPort());
        tcpSampler.setConnectTimeout(this.getCtimeout());
        tcpSampler.setProperty(TCPSampler.RE_USE_CONNECTION, this.isReUseConnection());
        tcpSampler.setProperty(TCPSampler.NODELAY, this.isNodelay());
        tcpSampler.setCloseConnection(String.valueOf(this.isCloseConnection()));
        tcpSampler.setSoLinger(this.getSoLinger());
        tcpSampler.setEolByte(this.getEolByte());

        String value = this.getRequest();
        if(StringUtils.isNotEmpty(this.getConnectEncoding())){
            if(StringUtils.equalsIgnoreCase("utf-8",this.getConnectEncoding())){
                try {
                    value = new String(value.getBytes(),StandardCharsets.UTF_8);
                }catch (Exception e){
                }
            }else if(StringUtils.equalsIgnoreCase("gbk",this.getConnectEncoding())){
                try {
                    value = new String(value.getBytes(),"GBK");
                }catch (Exception e){
                }

            }
        }
        tcpSampler.setRequestData(value);

        tcpSampler.setProperty(ConfigTestElement.USERNAME, this.getUsername());
        tcpSampler.setProperty(ConfigTestElement.PASSWORD, this.getPassword());
        return tcpSampler;
    }

    private void setUserParameters(HashTree tree) {
        UserParameters userParameters = new UserParameters();
        userParameters.setEnabled(true);
        userParameters.setName(this.getName() + "UserParameters");
        userParameters.setPerIteration(false);
        userParameters.setProperty(TestElement.TEST_CLASS, UserParameters.class.getName());
        userParameters.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("UserParametersGui"));
        List<StringProperty> names = new ArrayList<>();
        List<StringProperty> threadValues = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(this.parameters)) {
            this.parameters.forEach(item -> {
                names.add(new StringProperty(new Integer(new Random().nextInt(1000000)).toString(), item.getName()));
                String value = item.getValue();
                if(StringUtils.isNotEmpty(value)){
                    value = this.formatMockValue(value);
                    if(StringUtils.isNotEmpty(this.getConnectEncoding())){
                        if(StringUtils.equalsIgnoreCase("utf-8",this.getConnectEncoding())){
                            try {
                                value = new String(value.getBytes(),StandardCharsets.UTF_8);
                            }catch (Exception e){
                            }
                        }else if(StringUtils.equalsIgnoreCase("gbk",this.getConnectEncoding())){
                            try {
                                value = new String(value.getBytes(),"GBK");
                            }catch (Exception e){
                            }
                        }
                    }
                    threadValues.add(new StringProperty(new Integer(new Random().nextInt(1000000)).toString(), value));
                }
            });
        }
        userParameters.setNames(new CollectionProperty(UserParameters.NAMES, names));
        List<CollectionProperty> collectionPropertyList = new ArrayList<>();
        collectionPropertyList.add(new CollectionProperty(new Integer(new Random().nextInt(1000000)).toString(), threadValues));
        userParameters.setThreadLists(new CollectionProperty(UserParameters.THREAD_VALUES, collectionPropertyList));
        tree.add(userParameters);
    }
    private String formatMockValue(String value) {

        String patten = ">@[^>@]+</?";
        Pattern r = Pattern.compile(patten);
        try{
            Matcher m = r.matcher(value);
            while (m.find()){
                String findStr = m.group();
                if(findStr.length() > 3){
                    findStr = findStr.substring(1,findStr.length()-2);
                    String replaceStr = ScriptEngineUtils.buildFunctionCallString(findStr);
                    if(StringUtils.equals(findStr,replaceStr)){
                        replaceStr = "";
                    }
                    value = value.replace(">"+findStr+"</",">"+replaceStr+"</");
                    m  = r.matcher(value);
                }
            }
        }catch (Exception e){

        }
        return value;
    }
    private ConfigTestElement tcpConfig() {
        ConfigTestElement configTestElement = new ConfigTestElement();
        configTestElement.setEnabled(true);
        configTestElement.setName(this.getName());
        configTestElement.setProperty(TestElement.TEST_CLASS, ConfigTestElement.class.getName());
        configTestElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TCPConfigGui"));
        configTestElement.setProperty(TCPSampler.CLASSNAME, this.getClassname());
        configTestElement.setProperty(TCPSampler.SERVER, this.getServer());
        configTestElement.setProperty(TCPSampler.PORT, this.getPort());
        configTestElement.setProperty(TCPSampler.TIMEOUT_CONNECT, this.getCtimeout());
        configTestElement.setProperty(TCPSampler.RE_USE_CONNECTION, this.isReUseConnection());
        configTestElement.setProperty(TCPSampler.NODELAY, this.isNodelay());
        configTestElement.setProperty(TCPSampler.CLOSE_CONNECTION, this.isCloseConnection());
        configTestElement.setProperty(TCPSampler.SO_LINGER, this.getSoLinger());
        configTestElement.setProperty(TCPSampler.EOL_BYTE, this.getEolByte());
        configTestElement.setProperty(TCPSampler.SO_LINGER, this.getSoLinger());
        configTestElement.setProperty(ConfigTestElement.USERNAME, this.getUsername());
        configTestElement.setProperty(ConfigTestElement.PASSWORD, this.getPassword());
        return configTestElement;
    }

}
