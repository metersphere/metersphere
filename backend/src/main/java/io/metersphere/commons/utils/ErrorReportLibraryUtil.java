package io.metersphere.commons.utils;

import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.dto.ErrorReportAssertionResult;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseAssertionResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 误报解析类
 */
public class ErrorReportLibraryUtil {

    private static final String ERROR_REPORT_NAME_START = "ErrorReportAssertion:";
    private static final String ERROR_REPORT_SUCCESS_MESSAGE_FLAG = " Final result is success";
    public static final String ASSERTION_CONTENT_REGEX_DELIMITER = "--REGEX-->";

    public static ErrorReportLibraryParseDTO parseAssertions(RequestResult result) {
        ErrorReportLibraryParseDTO returnDTO = new ErrorReportLibraryParseDTO();
        if (result != null && result.getResponseResult() != null && CollectionUtils.isNotEmpty(result.getResponseResult().getAssertions())) {
            AssertionParserResult assertionParserResult = parserAndFormatAssertion(result.getResponseResult().getAssertions());
            List<ResponseAssertionResult> errorReportAssertionList = assertionParserResult.errorReportAssertionList;
            Map<String, List<ResponseAssertionResult>> successAssertionMessageMap = assertionParserResult.successAssertionMessageMap;
            Map<String, List<ResponseAssertionResult>> errorAssertionMessageMap = assertionParserResult.errorAssertionMessageMap;
            boolean higherThanSuccess = assertionParserResult.higherThanSuccess;
            boolean higherThanError = assertionParserResult.higherThanError;
            /*
                默认优先级(higherThanSuccess=false,higherThanError=false):
                1.如果一个请求同时有 成功、误报、失败 三个断言（断言规则不一样）时，那么请求最终结果处理成失败，展示匹配到的误报断言；
                2.如果一个请求同时有 失败、误报（断言规则不一样）那么处理成失败，展示匹配到的误报断言；
                3.如果一个请求同时有 成功、误报（断言规则不一样）那么处理成误报，展示匹配到的误报断言；
                4.如果一个请求同时有 成功、误报（断言规则一样）那么处理成成功，不展示匹配到的误报断言。
                能和误报断言匹配上的成功断言，都转为成功断言；
                如果含有失败断言，则结果处理成失败，展示剩下的误报断言
                如果还有误报断言，那么结果处理成误报，展示剩下的误报断言
                最后就是成功

                如果配置的误报与失败的优先级 / 误报与成功的优先级 与上述出现偏差，则对应的结果也会改变
             */
            if (CollectionUtils.isNotEmpty(errorReportAssertionList)) {
                List<ResponseAssertionResult> unMatchErrorReportAssertions = new ArrayList<>();
                Map<String, List<ResponseAssertionResult>> passedErrorReportAssertionMap = new HashMap<>();

                //过滤出没有命中的误报断言，并整理出命中误报断言的数据
                for (ResponseAssertionResult assertion : errorReportAssertionList) {
                    if (StringUtils.endsWith(assertion.getMessage(), ERROR_REPORT_SUCCESS_MESSAGE_FLAG)) {
                        String regexString = assertion.getContent();
                        if (passedErrorReportAssertionMap.containsKey(regexString))
                            passedErrorReportAssertionMap.get(regexString).add(assertion);
                        else {
                            List<ResponseAssertionResult> list = new ArrayList<>();
                            list.add(assertion);
                            passedErrorReportAssertionMap.put(regexString, list);
                        }
                    } else {
                        unMatchErrorReportAssertions.add(assertion);
                    }
                }

                //根据配置来筛选断言、获取误报编码、获取接口状态是否是误报
                AssertionFilterResult filterResult = filterAssertions(passedErrorReportAssertionMap, successAssertionMessageMap, errorAssertionMessageMap, result.isSuccess(), higherThanSuccess, higherThanError);
                int filteredSuccessAssertionCount = unMatchErrorReportAssertions.size() + filterResult.filteredSuccessAssertionList.size();
                unMatchErrorReportAssertions.addAll(filterResult.filteredSuccessAssertionList);
                unMatchErrorReportAssertions.addAll(filterResult.filteredErrorAssertionList);
                returnDTO.setRequestStatus(filterResult.requestStatus);
                returnDTO.setErrorCodeList(filterResult.errorCodeList);

                if (CollectionUtils.isNotEmpty(unMatchErrorReportAssertions)) {
                    // 未被误报断言匹配到的结果，清除该请求的误报断言记录，并将断言涉及到的统计结果恢复正常
                    result.setTotalAssertions(result.getTotalAssertions() - unMatchErrorReportAssertions.size());
                    int passAssertionCount = (result.getPassAssertions() - filteredSuccessAssertionCount);
                    if (passAssertionCount < 0) {
                        result.setPassAssertions(0);
                    } else {
                        result.setPassAssertions(passAssertionCount);
                    }
                    List<ResponseAssertionResult> allAssertions = result.getResponseResult().getAssertions();
                    List<ResponseAssertionResult> newAssertions = new ArrayList<>();

                    allAssertionForeach:
                    for (ResponseAssertionResult assertion : allAssertions) {
                        for (ResponseAssertionResult unMatchAssertion : unMatchErrorReportAssertions) {
                            if (StringUtils.equals(unMatchAssertion.getContent(), assertion.getContent())) {
                                continue allAssertionForeach;
                            }
                        }
                        newAssertions.add(assertion);
                    }
                    result.getResponseResult().getAssertions().clear();
                    result.getResponseResult().getAssertions().addAll(newAssertions);
                }
            }
        }
        returnDTO.setResult(result);
        return returnDTO;
    }

    /**
     * 过滤断言并统计最后结果
     * 1.higherThanError=true：含有误报断言就为误报，否则就为失败
     * 2.higherThanSuccess=true：含有误报断言就为误报，否则就为成功
     *
     * @param errorReportAssertionMap 匹配到的误报断言
     * @param successAssertionMap     匹配到的成功断言
     * @param errorAssertionMap       匹配到的失败断言
     * @param higherThanSuccess       误报断言优先级小于成功断言优先级
     * @param higherThanError         误报断言优先级大于成功断言优先级
     */
    private static AssertionFilterResult filterAssertions(Map<String, List<ResponseAssertionResult>> errorReportAssertionMap,
                                                          Map<String, List<ResponseAssertionResult>> successAssertionMap,
                                                          Map<String, List<ResponseAssertionResult>> errorAssertionMap,
                                                          boolean resultIsSuccess, boolean higherThanSuccess, boolean higherThanError) {
        AssertionFilterResult result = new AssertionFilterResult();
        if (MapUtils.isNotEmpty(errorReportAssertionMap)) {
            List<ResponseAssertionResult> removedSuccessList = removeAssertions(errorReportAssertionMap, successAssertionMap, higherThanSuccess);
            if (CollectionUtils.isNotEmpty(removedSuccessList)) {
                result.filteredSuccessAssertionList.addAll(removedSuccessList);
            }
            List<ResponseAssertionResult> removedErrorList = removeAssertions(errorReportAssertionMap, errorAssertionMap, higherThanError);
            if (CollectionUtils.isNotEmpty(removedErrorList)) {
                if (higherThanError) {
                    result.filteredErrorAssertionList.addAll(removedErrorList);
                } else {
                    result.filteredSuccessAssertionList.addAll(removedErrorList);
                }

            }
            for (List<ResponseAssertionResult> list : errorReportAssertionMap.values()) {
                list.forEach(item -> {
                    String assertionName = item.getName();
                    if (StringUtils.startsWith(assertionName, ERROR_REPORT_NAME_START)) {
                        String errorCode = StringUtils.substring(assertionName, ERROR_REPORT_NAME_START.length());
                        result.errorCodeList.add(errorCode);
                    }
                });
            }

            if (MapUtils.isNotEmpty(errorReportAssertionMap)) {
                if ((higherThanError && !resultIsSuccess) || (higherThanSuccess && resultIsSuccess)) {
                    result.requestStatus = ExecuteResult.ERROR_REPORT_RESULT.toString();
                }
            }
        }
        return result;
    }

    /**
     * 将匹配的断言移除
     *
     * @param firstMap
     * @param secondMap
     * @param removeDataInSecondMap 是否移除map2中的数据？true:移除map2中的数据 false：移除map1中的数据
     * @return
     */
    private static List<ResponseAssertionResult> removeAssertions(Map<String, List<ResponseAssertionResult>> firstMap, Map<String, List<ResponseAssertionResult>> secondMap, boolean removeDataInSecondMap) {
        List<ResponseAssertionResult> returnList = new ArrayList<>();
        if (MapUtils.isNotEmpty(firstMap) && MapUtils.isNotEmpty(secondMap)) {
            if (removeDataInSecondMap) {
                for (String regex : firstMap.keySet()) {
                    if (secondMap.containsKey(regex)) {
                        returnList.addAll(secondMap.get(regex));
                        secondMap.remove(regex);
                    }
                }
            } else {
                for (String regex : secondMap.keySet()) {
                    if (firstMap.containsKey(regex)) {
                        returnList.addAll(firstMap.get(regex));
                        firstMap.remove(regex);
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * 解析并重新格式化请求中的所有断言
     *
     * @param assertions
     * @return
     */
    private static AssertionParserResult parserAndFormatAssertion(List<ResponseAssertionResult> assertions) {
        AssertionParserResult result = new AssertionParserResult();
        for (ResponseAssertionResult assertion : assertions) {
            if (assertion instanceof ErrorReportAssertionResult || StringUtils.startsWith(assertion.getName(), ERROR_REPORT_NAME_START)) {
                String expression = assertion.getContent().trim();
                if (StringUtils.contains(expression, ASSERTION_CONTENT_REGEX_DELIMITER)) {
                    String[] contentArr = expression.split(ASSERTION_CONTENT_REGEX_DELIMITER);
                    assertion.setContent(contentArr[0]);
                    if (contentArr.length == 3) {
                        result.higherThanSuccess = BooleanUtils.toBoolean(contentArr[1]);
                        result.higherThanError = BooleanUtils.toBoolean(contentArr[2]);
                    }
                }
                result.errorReportAssertionList.add(assertion);
            } else {
                if (StringUtils.isNotEmpty(assertion.getContent())) {
                    String expression = assertion.getContent().trim();
                    String regexString = expression;
                    if (regexString.contains(ASSERTION_CONTENT_REGEX_DELIMITER)) {
                        regexString = expression.split(ASSERTION_CONTENT_REGEX_DELIMITER)[1];
                        assertion.setContent(expression.split(ASSERTION_CONTENT_REGEX_DELIMITER)[0]);
                    }
                    if (assertion.isPass()) {
                        if (result.successAssertionMessageMap.containsKey(regexString))
                            result.successAssertionMessageMap.get(regexString).add(assertion);
                        else {
                            List<ResponseAssertionResult> list = new ArrayList<>();
                            list.add(assertion);
                            result.successAssertionMessageMap.put(regexString, list);
                        }
                    } else {
                        if (result.errorAssertionMessageMap.containsKey(regexString))
                            result.errorAssertionMessageMap.get(regexString).add(assertion);
                        else {
                            List<ResponseAssertionResult> list = new ArrayList<>();
                            list.add(assertion);
                            result.errorAssertionMessageMap.put(regexString, list);
                        }
                    }
                }
            }
        }
        return result;
    }
}

class AssertionParserResult {
    boolean higherThanSuccess = false;
    boolean higherThanError = false;
    List<ResponseAssertionResult> errorReportAssertionList = new ArrayList<>();
    Map<String, List<ResponseAssertionResult>> successAssertionMessageMap = new HashMap<>();
    Map<String, List<ResponseAssertionResult>> errorAssertionMessageMap = new HashMap<>();
}

class AssertionFilterResult {
    String requestStatus;
    List<String> errorCodeList = new ArrayList<>();
    List<ResponseAssertionResult> filteredSuccessAssertionList = new ArrayList<>();
    List<ResponseAssertionResult> filteredErrorAssertionList = new ArrayList<>();
}
