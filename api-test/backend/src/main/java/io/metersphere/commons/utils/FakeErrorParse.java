package io.metersphere.commons.utils;

import io.metersphere.api.dto.FakeErrorLibraryDTO;
import io.metersphere.api.dto.definition.FakeError;
import io.metersphere.api.dto.definition.MsRegexDTO;
import io.metersphere.base.domain.ErrorReportLibraryExample;
import io.metersphere.base.domain.ErrorReportLibraryWithBLOBs;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.dto.RequestResult;
import io.metersphere.utils.JsonUtils;
import io.metersphere.xpack.fake.error.ErrorReportLibraryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 误报解析类
 *
 * @author xiaogang
 */
public class FakeErrorParse {

    public static FakeErrorLibraryDTO parseAssertions(RequestResult result) {
        FakeErrorLibraryDTO fakeError = new FakeErrorLibraryDTO();
        if (StringUtils.isNotBlank(result.getFakeErrorMessage())) {
            FakeError errorReportDTO = JsonUtils.parseObject(result.getFakeErrorMessage(), FakeError.class);
            ErrorReportLibraryService service = CommonBeanFactory.getBean(ErrorReportLibraryService.class);
            ErrorReportLibraryExample example = new ErrorReportLibraryExample();
            example.createCriteria().andProjectIdEqualTo(errorReportDTO.getProjectId()).andStatusEqualTo(true);
            List<ErrorReportLibraryWithBLOBs> bloBs = service.selectByExampleWithBLOBs(example);
            List<MsRegexDTO> regexList = new ArrayList<>();
            bloBs.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getContent())) {
                    try {
                        Map<String, Object> assertionMap = JSON.parseObject(item.getContent(), Map.class);
                        if (assertionMap != null) {
                            MsRegexDTO regexConfig = JSON.parseObject(JSONUtil.toJSONString(assertionMap.get("regexConfig")), MsRegexDTO.class);
                            regexConfig.setErrorCode(item.getErrorCode());
                            regexList.add(regexConfig);
                        }
                    } catch (Exception e) {
                        LogUtil.error(e);
                    }
                }
            });
            //根据配置来筛选断言、获取误报编码、获取接口状态是否是误报
            List<String> errorCodeList = new ArrayList<>();
            regexList.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getSubject())) {
                    switch (item.getSubject()) {
                        case "Response Code" ->
                                item.setPass(parseResponseCode(result.getResponseResult().getResponseCode(), item.getValue(), item.getCondition()));

                        case "Response Headers" ->
                                item.setPass(parseResponseCode(result.getResponseResult().getHeaders(), item.getValue(), item.getCondition()));

                        case "Response Data" ->
                                item.setPass(parseResponseCode(result.getResponseResult().getBody(), item.getValue(), item.getCondition()));
                        default -> item.setPass(false);
                    }
                }
                if (item.isPass()) {
                    errorCodeList.add(item.getErrorCode());
                }
            });
            boolean higherThanError = errorReportDTO.isHigherThanError();
            boolean higherThanSuccess = errorReportDTO.isHigherThanSuccess();
            if (CollectionUtils.isNotEmpty(errorCodeList)) {
                if ((higherThanError && !result.isSuccess()) || (higherThanSuccess && result.isSuccess())) {
                    fakeError.setRequestStatus(ApiReportStatus.FAKE_ERROR.name());
                }
                fakeError.setErrorCodeList(errorCodeList);
            }
            LogUtil.info(" FAKE_ERROR result:  config-higherThanError:" + higherThanError
                    + ", config-higherThanSuccess:" + higherThanSuccess
                    + ", resultIsSuccess: " + result.isSuccess()
                    + ", isFakeError: " + ((higherThanError && !result.isSuccess()) || (higherThanSuccess && result.isSuccess()))
                    + "; status:" + fakeError.getRequestStatus());
        }
        fakeError.setResult(result);
        return fakeError;
    }

    private static boolean parseResponseCode(String result, String regexDTO, String condition) {
        return switch (condition.toUpperCase()) {
            case "CONTAINS" -> result.contains(regexDTO);

            case "NOT_CONTAINS" -> notContains(result, regexDTO);

            case "EQUALS" -> StringUtils.equals(result, regexDTO);

            case "START_WITH" -> result.startsWith(regexDTO);

            case "END_WITH" -> result.endsWith(regexDTO);

            default -> false;
        };
    }

    private static boolean notContains(String result, String regexDTO) {
        return !result.contains(regexDTO);
    }
}