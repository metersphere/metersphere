package io.metersphere.api.constants;

public class ApiCaseAiPromptConstants {

    public static final String AI_CASE_TRANSFORM_MODULE_PROMPT = "请先把匹配条件对应的值替换为英文，可替换范围为：等于/不等于/包含/不包含/不校验/大于/大于或等于/小于/小于或等于/以...开始/以...结束/为空/不为空/正则匹配/长度等于/长度不等于/长度大于/长度大于或等于/长度小于/长度小于或等于，" +
            "对应的英文值为：EQUALS/NOT_EQUALS/CONTAINS/NOT_CONTAINS/UNCHECK/GT/GT_OR_EQUALS/LT/LT_OR_EQUALS/START_WITH/END_WITH/EMPTY/NOT_EMPTY/REGEX/LENGTH_EQUALS/LENGTH_NOT_EQUALS/LENGTH_GT/LENGTH_GT_OR_EQUALS/LENGTH_LT/LENGTH_LT_OR_EQUALS，替换完成后再解析格式并转为java对象:";
}
