package io.metersphere.utils;

import io.metersphere.dto.FakeErrorDTO;
import io.metersphere.dto.FakeErrorLibraryDTO;
import io.metersphere.dto.MsRegexDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.enums.ApiReportStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 误报解析类
 *
 * @author xiaogang
 */
public class FakeErrorUtils {
    public static FakeErrorLibraryDTO parseAssertions(RequestResult result, Map<String, List<MsRegexDTO>> fakeErrorMap) {
        FakeErrorLibraryDTO fakeError = new FakeErrorLibraryDTO();
        try {
            if (StringUtils.isNotBlank(result.getFakeErrorMessage())) {
                FakeErrorDTO errorReportDTO = JsonUtils.parseObject(result.getFakeErrorMessage(), FakeErrorDTO.class);
                if (MapUtils.isNotEmpty(fakeErrorMap) && errorReportDTO != null) {
                    List<MsRegexDTO> regexList = fakeErrorMap.get(errorReportDTO.getProjectId());
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
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("误报处理错误：", e);
        }
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