package io.metersphere.api.service;

import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.automation.parse.TcpTreeTableDataParser;
import io.metersphere.api.dto.definition.SaveApiDefinitionRequest;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/16 4:57 下午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TcpApiParamService {
    @Resource
    private EsbApiParamService esbApiParamService;

    public SaveApiDefinitionRequest handleTcpRequest(SaveApiDefinitionRequest request) {
        MsTCPSampler tcpSampler = this.handleTcpRequest(request.getRequest());
        if (tcpSampler != null) {
            request.setRequest(tcpSampler);
        }
        return request;
    }

    public MsTestElement parseMsTestElement(MsTestElement testElement) {
        if (testElement == null) {
            return null;
        }
        if (testElement instanceof MsTCPSampler) {
            MsTCPSampler tcpSampler = this.handleTcpRequest(testElement);
            if (tcpSampler != null) {
                return tcpSampler;
            } else {
                return testElement;
            }
        } else {
            return testElement;
        }
    }

    public MsTCPSampler handleTcpRequest(MsTestElement testElement) {
        MsTCPSampler tcpSampler = null;
        try {
            if (testElement instanceof MsTCPSampler) {
                tcpSampler = (MsTCPSampler) testElement;
                String protocol = tcpSampler.getProtocol();
                if(StringUtils.equalsIgnoreCase(protocol,"esb")){
                    if(CollectionUtils.isNotEmpty(tcpSampler.getEsbDataStruct())){
                        List<KeyValue> keyValueList = esbApiParamService.genKeyValueListByDataStruct(tcpSampler, tcpSampler.getEsbDataStruct());
                        tcpSampler.setParameters(keyValueList);
                    }
                }else {
                    String reportType = tcpSampler.getReportType();
                    if (StringUtils.isNotEmpty(reportType)) {
                        switch (reportType) {
                            case "raw":
                                tcpSampler.setRequest(tcpSampler.getRawDataStruct());
                                break;
                            case "xml":
                                String xmlDataStruct = this.genValueFromEsbDataStructByParam(tcpSampler.getXmlDataStruct());
                                tcpSampler.setRequest(xmlDataStruct);
                                break;
                            case "json":
                                tcpSampler.setRequest(tcpSampler.getJsonDataStruct());
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return tcpSampler;
    }

    //通过报文模版中的变量参数，解析报文数据结构，生成对应的xml数据
    private String genValueFromEsbDataStructByParam(List<TcpTreeTableDataStruct> dataStructRequestList) {
        String returnValue = TcpTreeTableDataParser.treeTableData2Xml(dataStructRequestList);
        return returnValue;
    }

    public void checkTestElement(MsTestElement testElement) {
        try {
            if (testElement != null) {
                if (testElement instanceof MsTCPSampler) {
                    LoggerUtil.info("处理TCP请求【 " + testElement.getId() + " 】开始");
                    this.handleTcpRequest(testElement);
                    LoggerUtil.info("处理TCP请求【 " + testElement.getId() + " 】完成");
                }
                if (testElement.getHashTree() != null) {
                    for (MsTestElement itemElement : testElement.getHashTree()) {
                        this.checkTestElement(itemElement);
                    }
                }
            }

        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
