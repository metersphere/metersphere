package io.metersphere.commons.utils;

import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseAssertionResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 误报解析类
 */
public class ErrorReportLibraryUtil {

    private static final String ERROR_CODE_START = "Error report:";

    public static ErrorReportLibraryParseDTO parseAssertions(RequestResult result) {
        ErrorReportLibraryParseDTO returnDTO = new ErrorReportLibraryParseDTO();
        if (result != null && result.getResponseResult() != null && CollectionUtils.isNotEmpty(result.getResponseResult().getAssertions())) {
            List<ResponseAssertionResult> errorReportAssertionList = new ArrayList<>();
            for (ResponseAssertionResult assertion : result.getResponseResult().getAssertions()) {
                if (StringUtils.startsWith(assertion.getContent(), ERROR_CODE_START)) {
                    errorReportAssertionList.add(assertion);
                }
            }
            if (CollectionUtils.isNotEmpty(errorReportAssertionList)) {
                List<ResponseAssertionResult> removeList = new ArrayList<>();
                for (ResponseAssertionResult assertion : errorReportAssertionList) {
                    if (assertion.isPass()) {
                        String errorCode = StringUtils.substring(assertion.getContent(), ERROR_CODE_START.length());
                        returnDTO.getErrorCodeList().add(errorCode);
                    } else {
                        removeList.add(assertion);
                    }
                }

                if (CollectionUtils.isNotEmpty(removeList)) {
                    if (result.getError() > 0) {
                        result.setError(result.getError() - 1);
                    }
                    result.setTotalAssertions(result.getTotalAssertions() - removeList.size());
                    result.getResponseResult().getAssertions().removeAll(removeList);
                    //修改由于误报断言导致的执行结果
                    if(result.getError() == 0 && !result.isSuccess()){
                        result.setSuccess(true);
                    }
                }
            }
        }
        return returnDTO;
    }
}

