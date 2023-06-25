package io.metersphere.api.dto.definition.request.sampler;

import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.GlobalScriptFilterRequest;
import io.metersphere.api.parse.api.JMeterScriptUtil;
import io.metersphere.api.parse.scenario.TcpTreeTableDataParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.HashTreeUtil;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.tcp.sampler.MsTCPClientImpl;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
@EqualsAndHashCode(callSuper = true)
public class MsTCPSampler extends MsTestElement {
    private String type = ElementConstants.TCP_SAMPLER;
    private String clazzName = MsTCPSampler.class.getCanonicalName();
    private String classname = "";
    private String server = "";
    private String port = "";
    private String ctimeout = "";
    private String timeout = "";
    private boolean reUseConnection = true;
    private boolean nodelay;
    private boolean closeConnection;
    private String soLinger = "";
    private String eolByte = "";
    private String username = "";
    private String password = "";
    private String request;
    private List<KeyValue> parameters;
    private String useEnvironment;
    private MsJSR223PreProcessor tcpPreProcessor;
    private String protocol = "TCP";
    private String projectId;
    private String connectEncoding;
    private String reportType;
    private List<TcpTreeTableDataStruct> xmlDataStruct;
    private String jsonDataStruct;
    private String rawDataStruct;
    private boolean customizeReq;
    private Boolean isRefEnvironment;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        } else if (config.isOperating() && StringUtils.isNotEmpty(config.getOperatingSampleTestName())) {
            this.setName(config.getOperatingSampleTestName());
        }
        if (!ElementUtil.isEnable(this, config)) {
            return;
        }
        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            boolean ref = this.setRefElement();
            if (!ref) {
                LoggerUtil.debug("引用对象已经被删除：" + this.getId());
                return;
            }
            hashTree = this.getHashTree();
        }
        //检查request
        if (StringUtils.isNotEmpty(reportType)) {
            switch (reportType) {
                case "json":
                    if (StringUtils.isNotEmpty(this.jsonDataStruct)) {
                        request = this.jsonDataStruct;
                    }
                    break;
                case "xml":
                    if (CollectionUtils.isNotEmpty(this.xmlDataStruct)) {
                        request = TcpTreeTableDataParser.treeTableData2Xml(this.xmlDataStruct, this.connectEncoding);
                    }
                    break;
                case "raw":
                    if (StringUtils.isNotEmpty(this.rawDataStruct)) {
                        request = this.rawDataStruct;
                    }
                    break;
                default:
                    break;
            }
        }
        if (config.getConfig() == null) {
            // 单独接口执行
            if (StringUtils.isNotEmpty(config.getProjectId())) {
                this.setProjectId(config.getProjectId());
            }
            config.setConfig(ElementUtil.getEnvironmentConfig(StringUtils.isNotEmpty(this.getEnvironmentId()) ? this.getEnvironmentId() : useEnvironment, this.getProjectId()));
        }
        EnvironmentConfig envConfig = null;
        if (config.getConfig() != null) {
            envConfig = config.getConfig().get(this.projectId);
            parseEnvironment(envConfig);
        }
        // 添加环境中的公共变量
        Arguments arguments = ElementUtil.getConfigArguments(config, this.getName(), this.getProjectId(), null);
        if (arguments != null) {
            tree.add(arguments);
        }
        //添加csv
        ElementUtil.addApiVariables(config, tree, this.getProjectId());
        final HashTree samplerHashTree = new ListedHashTree();
        samplerHashTree.add(tcpConfig());
        TCPSampler tcpSampler = tcpSampler(config);
        // 失败重试
        if (config.getRetryNum() > 0 && !ElementUtil.isLoop(this.getParent())) {
            final HashTree loopTree = ElementUtil.retryHashTree(this.getName(), config.getRetryNum(), tree);
            loopTree.set(tcpSampler, samplerHashTree);
        } else {
            tree.set(tcpSampler, samplerHashTree);
        }

        setUserParameters(samplerHashTree);
        if (tcpPreProcessor != null && StringUtils.isNotBlank(tcpPreProcessor.getScript())) {
            samplerHashTree.add(tcpPreProcessor.getShellProcessor());
        }
        //增加误报、全局断言
        HashTreeUtil.addPositive(envConfig, samplerHashTree, config, this.getProjectId(), tcpSampler);
        //处理全局前后置脚本(步骤内)
        String environmentId = this.getEnvironmentId();
        if (environmentId == null) {
            environmentId = this.useEnvironment;
        }
        //根据配置将脚本放置在私有脚本之前
        JMeterScriptUtil.setScriptByEnvironmentConfig(envConfig, samplerHashTree, GlobalScriptFilterRequest.TCP.name(), environmentId, config, false);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree = ElementUtil.order(hashTree);
            hashTree.forEach(el -> {
                if (el instanceof MsJDBCPreProcessor || el instanceof MsJDBCPostProcessor) {
                    el.setParent(this);
                }
                el.toHashTree(samplerHashTree, el.getHashTree(), config);
            });
        }
        //根据配置将脚本放置在私有脚本之后
        JMeterScriptUtil.setScriptByEnvironmentConfig(envConfig, samplerHashTree, GlobalScriptFilterRequest.TCP.name(), environmentId, config, true);
    }

    private boolean setRefElement() {
        try {
            MsTCPSampler proxy = null;
            if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
                if (bloBs != null) {
                    this.setName(bloBs.getName());
                    this.setProjectId(bloBs.getProjectId());
                    JSONObject element = JSONUtil.parseObject(bloBs.getRequest());
                    ElementUtil.dataFormatting(element);
                    proxy = JSONUtil.parseObject(element.toString(), MsTCPSampler.class);
                }
            } else {
                ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
                if (apiDefinition != null) {
                    this.setName(apiDefinition.getName());
                    this.setProjectId(apiDefinition.getProjectId());
                    proxy = JSONUtil.parseObject(apiDefinition.getRequest(), MsTCPSampler.class);
                }
            }
            if (proxy != null) {
                if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
                    ElementUtil.mergeHashTree(this, proxy.getHashTree());
                } else {
                    this.setHashTree(proxy.getHashTree());
                }
                this.setClassname(proxy.getClassname());
                this.setServer(proxy.getServer());
                this.setPort(proxy.getPort());
                this.setRequest(proxy.getRequest());
                return true;
            }
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return false;
    }

    private void parseEnvironment(EnvironmentConfig config) {
        if (!isCustomizeReq() && config != null && config.getTcpConfig() != null) {
            if (!isCustomizeReq() && config != null) {
                this.server = config.getTcpConfig().getServer();
                this.port = config.getTcpConfig().getPort() + StringUtils.EMPTY;
                if (StringUtils.equals(this.eolByte, StringUtils.SPACE)) {
                    this.eolByte = "";
                } else {
                    if (StringUtils.isEmpty(this.eolByte)) {
                        this.eolByte = config.getTcpConfig().getEolByte();
                    }
                }
                if ((StringUtils.isEmpty(this.timeout) || StringUtils.equals(this.timeout, "0")) && StringUtils.isNotEmpty(config.getTcpConfig().getTimeout())) {
                    this.timeout = config.getTcpConfig().getTimeout();
                }
                if (StringUtils.isEmpty(this.ctimeout) || StringUtils.equals(this.ctimeout, "0") && StringUtils.isNotEmpty(config.getTcpConfig().getCtimeout())) {
                    this.ctimeout = config.getTcpConfig().getCtimeout();
                }
            }
        }
    }

    private TCPSampler tcpSampler(ParameterConfig config) {
        TCPSampler tcpSampler = new TCPSampler();
        tcpSampler.setEnabled(this.isEnable());
        tcpSampler.setName(this.getName());
        if (StringUtils.isEmpty(this.getName())) {
            tcpSampler.setName("TcpSamplerProxy");
        }
        if (config.isOperating()) {
            String[] testNameArr = tcpSampler.getName().split("<->");
            if (testNameArr.length > 0) {
                String testName = testNameArr[0];
                tcpSampler.setName(testName);
            }
        }
        ElementUtil.setBaseParams(tcpSampler, this.getParent(), config, this.getId(), this.getIndex());
        tcpSampler.setProperty(TestElement.TEST_CLASS, TCPSampler.class.getName());
        tcpSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TCPSamplerGui"));
        if (StringUtils.isEmpty(this.getClassname())) {
            tcpSampler.setClassname("TCPClientImpl");
        } else {
            tcpSampler.setClassname(this.getClassname());
        }
        if (StringUtils.equals("TCPClientImpl", this.getClassname())) {
            tcpSampler.setClassname(MsTCPClientImpl.class.getCanonicalName());
        }
        tcpSampler.setCharset(this.getConnectEncoding());
        tcpSampler.setServer(this.getServer());
        tcpSampler.setPort(this.getPort());
        tcpSampler.setConnectTimeout(this.getCtimeout());
        tcpSampler.setProperty(TCPSampler.RE_USE_CONNECTION, this.isReUseConnection());
        tcpSampler.setProperty(TCPSampler.NODELAY, this.isNodelay());
        tcpSampler.setCloseConnection(String.valueOf(this.isCloseConnection()));
        tcpSampler.setSoLinger(this.getSoLinger());
        if (StringUtils.equalsIgnoreCase("LengthPrefixedBinaryTCPClientImpl", this.classname)) {
            //LengthPrefixedBinaryTCPClientImpl取样器不可以设置eolByte
            this.eolByte = null;
        } else {
            tcpSampler.setEolByte(this.getEolByte());
        }
        if (StringUtils.isNotEmpty(this.timeout)) {
            tcpSampler.setTimeout(this.timeout);
        }
        if (StringUtils.isNotEmpty(this.ctimeout)) {
            tcpSampler.setConnectTimeout(this.ctimeout);
        }
        String value = this.getRequest();
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
                if (StringUtils.isNotEmpty(value)) {
                    value = this.formatMockValue(value);
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
        String pattern = ">@[^>@]+</?";
        Pattern r = Pattern.compile(pattern);
        try {
            Matcher m = r.matcher(value);
            while (m.find()) {
                String findStr = m.group();
                if (findStr.length() > 3) {
                    findStr = findStr.substring(1, findStr.length() - 2);
                    String replaceStr = ScriptEngineUtils.buildFunctionCallString(findStr);
                    if (StringUtils.equals(findStr, replaceStr)) {
                        replaceStr = "";
                    }
                    value = value.replace(">" + findStr + "</", ">" + replaceStr + "</");
                    m = r.matcher(value);
                }
            }
        } catch (Exception e) {
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
        if (!StringUtils.equalsIgnoreCase("LengthPrefixedBinaryTCPClientImpl", this.classname)) {
            configTestElement.setProperty(TCPSampler.EOL_BYTE, this.getEolByte());
        }
        configTestElement.setProperty(TCPSampler.SO_LINGER, this.getSoLinger());
        configTestElement.setProperty(ConfigTestElement.USERNAME, this.getUsername());
        configTestElement.setProperty(ConfigTestElement.PASSWORD, this.getPassword());
        return configTestElement;
    }
}
