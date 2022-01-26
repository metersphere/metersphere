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
            boolean hasOtherErrorAssertion = false;
            for (ResponseAssertionResult assertion : result.getResponseResult().getAssertions()) {
                if (StringUtils.startsWith(assertion.getContent(), ERROR_CODE_START)) {
                    errorReportAssertionList.add(assertion);
                }else {
                    if(!assertion.isPass()){
                        hasOtherErrorAssertion = true;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(errorReportAssertionList)) {
                List<ResponseAssertionResult> unMatchErrorReportAssertions = new ArrayList<>();
                for (ResponseAssertionResult assertion : errorReportAssertionList) {
                    if (assertion.isPass()) {
                        String errorCode = StringUtils.substring(assertion.getContent(), ERROR_CODE_START.length());
                        returnDTO.getErrorCodeList().add(errorCode);
                    } else {
                        unMatchErrorReportAssertions.add(assertion);
                    }
                }

                if (CollectionUtils.isNotEmpty(unMatchErrorReportAssertions)) {
                    // 未被误报断言匹配到的结果，清除该请求的误报断言记录，并将断言涉及到的统计结果恢复正常
                    if (result.getResponseResult() != null
                            && StringUtils.equalsIgnoreCase(result.getResponseResult().getResponseCode(), "200")
                            && result.getError() > 0) {
                        if(!hasOtherErrorAssertion){
                            result.setError(result.getError() - 1);
                        }
                    }
                    result.setTotalAssertions(result.getTotalAssertions() - unMatchErrorReportAssertions.size());
                    result.getResponseResult().getAssertions().removeAll(unMatchErrorReportAssertions);
                    if (result.getError() == 0 && !result.isSuccess()) {
                        result.setSuccess(true);
                    }
                }
            }
        }
        return returnDTO;
    }
}

