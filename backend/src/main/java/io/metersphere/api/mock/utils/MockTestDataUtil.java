package io.metersphere.api.mock.utils;

import com.mifmif.common.regex.Generex;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.mock.MockTestDataRequest;
import io.metersphere.api.mock.dto.MockParamConditionEnum;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class MockTestDataUtil {
    public List<MockTestDataRequest> parseTestDataByRequest(List<MockTestDataRequest> testDataRequestList) {
        for (MockTestDataRequest request : testDataRequestList) {
            try{
                request.setValue(this.getTestData(request));
            }catch (Exception e){
                LogUtil.error(e);
            }
        }
        return testDataRequestList;
    }
    public String getTestData(MockTestDataRequest request) {
        if (StringUtils.equals(MockApiUtils.parseCondition(request.getCondition()), MockParamConditionEnum.VALUE_EQUALS.name())) {
            return request.getValue();
        } else if (StringUtils.equalsAny(MockApiUtils.parseCondition(request.getCondition()), MockParamConditionEnum.VALUE_CONTAINS.name(), MockParamConditionEnum.VALUE_NOT_EQUALS.name())) {
            return request.getValue() + "A";
        } else if (StringUtils.equals(MockApiUtils.parseCondition(request.getCondition()), MockParamConditionEnum.LENGTH_EQUALS.name())) {
            int length = Integer.parseInt(request.getValue());
            return RandomStringUtils.randomAlphanumeric(length);
        } else if (StringUtils.equalsAny(MockApiUtils.parseCondition(request.getCondition()), MockParamConditionEnum.LENGTH_NOT_EQUALS.name(), MockParamConditionEnum.LENGTH_LARGE_THAN.name())) {
            int length = Integer.parseInt(request.getValue()) + 1;
            return RandomStringUtils.randomAlphanumeric(length);
        } else if (StringUtils.equals(MockApiUtils.parseCondition(request.getCondition()), MockParamConditionEnum.LENGTH_SHOT_THAN.name())) {
            int length = Integer.parseInt(request.getValue());
            if(length > 1){
                return RandomStringUtils.randomAlphanumeric(length-1);
            }else {
                return "";
            }
        } else if (StringUtils.equals(MockApiUtils.parseCondition(request.getCondition()), MockParamConditionEnum.REGULAR_MATCH.name())) {
            if (StringUtils.isNotEmpty(request.getValue())) {
                Generex generex = new Generex(request.getValue());
                String randomStr = generex.random();
                return randomStr;
            }else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String getTestData(TcpTreeTableDataStruct condition) {
        if (StringUtils.equals(MockApiUtils.parseCondition(condition.getCondition()), MockParamConditionEnum.VALUE_EQUALS.name())) {
            return condition.getValue();
        } else if (StringUtils.equalsAny(MockApiUtils.parseCondition(condition.getCondition()), MockParamConditionEnum.VALUE_CONTAINS.name(), MockParamConditionEnum.VALUE_NOT_EQUALS.name())) {
            return condition.getValue() + "A";
        } else if (StringUtils.equals(MockApiUtils.parseCondition(condition.getCondition()), MockParamConditionEnum.LENGTH_EQUALS.name())) {
            int length = Integer.parseInt(condition.getValue());
            return RandomStringUtils.randomAlphanumeric(length);
        } else if (StringUtils.equalsAny(MockApiUtils.parseCondition(condition.getCondition()), MockParamConditionEnum.LENGTH_NOT_EQUALS.name(), MockParamConditionEnum.LENGTH_LARGE_THAN.name())) {
            int length = Integer.parseInt(condition.getValue()) + 1;
            return RandomStringUtils.randomAlphanumeric(length);
        } else if (StringUtils.equals(MockApiUtils.parseCondition(condition.getCondition()), MockParamConditionEnum.LENGTH_SHOT_THAN.name())) {
            int length = Integer.parseInt(condition.getValue());
            if(length > 1){
                return RandomStringUtils.randomAlphanumeric(length);
            }else {
                return "";
            }
        } else if (StringUtils.equals(MockApiUtils.parseCondition(condition.getCondition()), MockParamConditionEnum.REGULAR_MATCH.name())) {
            if (StringUtils.isNotEmpty(condition.getValue())) {
                Generex generex = new Generex(condition.getValue());
                String randomStr = generex.random();
                return randomStr;
            }else {
                return "";
            }
        } else {
            return "";
        }
    }

    public List<TcpTreeTableDataStruct> parseTestDataByTcpTreeTableData(List<TcpTreeTableDataStruct> requestArray) {
        for (TcpTreeTableDataStruct request : requestArray) {
            try{
                request.setValue(this.getTestData(request));
                if(!CollectionUtils.isEmpty(request.getChildren())){
                    this.parseTestDataByTcpTreeTableData(request.getChildren());
                }
            }catch (Exception e){
                LogUtil.error(e);
            }
        }
        return requestArray;
    }
}
