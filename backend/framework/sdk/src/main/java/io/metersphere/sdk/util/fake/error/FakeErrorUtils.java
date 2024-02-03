package io.metersphere.sdk.util.fake.error;

import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.dto.api.result.MsRegexDTO;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 误报解析类
 */
public class FakeErrorUtils {
    public static void compute(RequestResult result, List<MsRegexDTO> regexDTOS) {
        try {
            if (result != null && StringUtils.isNotBlank(result.getProjectId()) &&
                    CollectionUtils.isNotEmpty(regexDTOS)) {

                Map<String, List<MsRegexDTO>> fakeErrorMap = regexDTOS.stream()
                        .collect(Collectors.groupingBy(MsRegexDTO::getProjectId));

                List<MsRegexDTO> regexList = fakeErrorMap.get(result.getProjectId());
                //根据配置来筛选断言、获取误报编码、获取接口状态是否是误报
                List<String> errorCodeList = new ArrayList<>();
                regexList.forEach(item -> {
                    if (StringUtils.isNotEmpty(item.getType())) {
                        switch (item.getRespType()) {
                            case "Response Code" ->
                                    item.setPass(parseResponseCode(result.getResponseResult().getResponseCode(), item.getExpression(), item.getRelation()));

                            case "Response Headers" ->
                                    item.setPass(parseResponseCode(result.getResponseResult().getHeaders(), item.getExpression(), item.getRelation()));

                            case "Response Data" ->
                                    item.setPass(parseResponseCode(result.getResponseResult().getBody(), item.getExpression(), item.getRelation()));
                            default -> item.setPass(false);
                        }
                    }
                    if (BooleanUtils.isTrue(item.getPass())) {
                        errorCodeList.add(item.getName());
                    }
                });
                if (CollectionUtils.isNotEmpty(errorCodeList)) {
                    result.setStatus(ApiReportStatus.FAKE_ERROR.name());
                    result.setFakeErrorCode(getErrorCodeStr(errorCodeList));
                }
            }
        } catch (Exception e) {
            LogUtils.error("误报处理错误：", e);
        }
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

    private static String getErrorCodeStr(List<String> errorCodeList) {
        if (CollectionUtils.isNotEmpty(errorCodeList)) {
            String errorCodeStr = StringUtils.join(errorCodeList, ";");
            return errorCodeStr;
        } else {
            return StringUtils.EMPTY;
        }
    }

    private static boolean notContains(String result, String regexDTO) {
        return !result.contains(regexDTO);
    }
}