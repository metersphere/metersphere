package io.metersphere.commons.utils;

import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.dto.RequestResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求返回解析工具
 */
public class ResponseUtil {

    public static RequestResultExpandDTO parseByRequestResult(RequestResult baseResult) {
        //解析是否含有误报库信息
        ErrorReportLibraryParseDTO errorCodeDTO = ErrorReportLibraryUtil.parseAssertions(baseResult);
        RequestResult requestResult = errorCodeDTO.getResult();
        RequestResultExpandDTO expandDTO = new RequestResultExpandDTO();
        BeanUtils.copyBean(expandDTO, requestResult);

        if (CollectionUtils.isNotEmpty(errorCodeDTO.getErrorCodeList())) {
            Map<String, String> expandMap = new HashMap<>();
            expandMap.put(ExecuteResult.ERROR_REPORT_RESULT.toString(), errorCodeDTO.getErrorCodeStr());
            if (StringUtils.equalsIgnoreCase(errorCodeDTO.getRequestStatus(), ExecuteResult.ERROR_REPORT_RESULT.toString())) {
                expandMap.put("status", ExecuteResult.ERROR_REPORT_RESULT.toString());
            }
            expandDTO.setAttachInfoMap(expandMap);
        }
        if (StringUtils.equalsIgnoreCase(errorCodeDTO.getRequestStatus(), ExecuteResult.ERROR_REPORT_RESULT.toString())) {
            expandDTO.setStatus(errorCodeDTO.getRequestStatus());
        }
        return expandDTO;
    }
}
