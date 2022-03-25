package io.metersphere.commons.utils;

import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.dto.ErrorReportAssertionResult;
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

    private static final String NEW_ERROR_CODE_STATE = "Check Error report:";

    private static final String ERROR_REPORT_SUCCESS_MESSAGE_FLAG = "Final result is success";

    public static ErrorReportLibraryParseDTO parseAssertions(RequestResult result) {
        ErrorReportLibraryParseDTO returnDTO = new ErrorReportLibraryParseDTO();
        if (result != null && result.getResponseResult() != null && CollectionUtils.isNotEmpty(result.getResponseResult().getAssertions())) {
            List<ErrorReportAssertionResult> errorReportAssertionList = new ArrayList<>();
            for (ResponseAssertionResult assertion : result.getResponseResult().getAssertions()) {
                if (StringUtils.equals(assertion.getName(), "ErrorReportAssertion") && assertion instanceof  ErrorReportAssertionResult) {
                    errorReportAssertionList.add((ErrorReportAssertionResult)assertion);
                }
            }
            if (CollectionUtils.isNotEmpty(errorReportAssertionList)) {
                List<ResponseAssertionResult> unMatchErrorReportAssertions = new ArrayList<>();
                int machedErrorPortAssertions = 0;
                for (ErrorReportAssertionResult assertion : errorReportAssertionList) {
                    if(StringUtils.endsWith(assertion.getErrorReportMessage(),ERROR_REPORT_SUCCESS_MESSAGE_FLAG)) {
                        machedErrorPortAssertions ++;
                        String errorCode = StringUtils.substring(assertion.getContent(), NEW_ERROR_CODE_STATE.length());
                        returnDTO.getErrorCodeList().add(errorCode);
                    } else {
                        unMatchErrorReportAssertions.add(assertion);
                    }
                }

                if (CollectionUtils.isNotEmpty(unMatchErrorReportAssertions)) {
                    // 未被误报断言匹配到的结果，清除该请求的误报断言记录，并将断言涉及到的统计结果恢复正常
                    result.setTotalAssertions(result.getTotalAssertions() - unMatchErrorReportAssertions.size());
                    int passAssertionCountg = (result.getPassAssertions() - unMatchErrorReportAssertions.size() ) + machedErrorPortAssertions;
                    if(passAssertionCountg< 0){
                        result.setPassAssertions(0);
                    }else {
                        result.setPassAssertions(passAssertionCountg);
                    }

                    result.getResponseResult().getAssertions().removeAll(unMatchErrorReportAssertions);
                }
            }
        }
        returnDTO.setResult(result);
        return returnDTO;
    }
}

