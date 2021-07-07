package io.metersphere.api.service;

import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.automation.parse.TcpTreeTableDataParser;
import io.metersphere.api.dto.definition.SaveApiDefinitionRequest;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/16 4:57 下午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TcpApiParamService {

    public SaveApiDefinitionRequest handleTcpRequest(SaveApiDefinitionRequest request) {
        MsTCPSampler tcpSampler = this.handleTcpRequest(request.getRequest());
        if(tcpSampler != null){
            request.setRequest(tcpSampler);
        }
        return request;
    }
    public MsTCPSampler  handleTcpRequest(MsTestElement testElement){
        MsTCPSampler tcpSampler = null;
        try {
            if(testElement instanceof  MsTCPSampler){
                tcpSampler = (MsTCPSampler)testElement;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tcpSampler;
    }

    //通过报文模版中的变量参数，解析报文数据结构，生成对应的xml数据
    private String genValueFromEsbDataStructByParam(List<TcpTreeTableDataStruct> dataStructRequestList) {
        String returnValue = TcpTreeTableDataParser.treeTableData2Xml(dataStructRequestList);
        return returnValue;
    }

    public void checkTestElement(MsTestElement testElement) {
        if(testElement != null){
            if(testElement instanceof MsTCPSampler){
                this.handleTcpRequest(testElement);
            }

            if(testElement.getHashTree() != null){
                for (MsTestElement itemElement : testElement.getHashTree()) {
                    this.checkTestElement(itemElement);
                }
            }
        }
    }
}
