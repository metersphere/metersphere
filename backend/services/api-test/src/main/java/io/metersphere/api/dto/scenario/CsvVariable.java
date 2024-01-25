package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @Author: jianxing
 * @CreateTime: 2024-01-12  10:11
 */
@Data
public class CsvVariable {

    @Schema(description = "id")
    private String id;

    @Schema(description = "文件id/引用文件id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;

    @Schema(description = "场景id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String scenarioId;

    @Schema(description = "csv变量名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "作用域 SCENARIO/STEP")
    /**
     * @see CsvVariableScope
     */
    private String scope;

    @Schema(description = "启用/禁用")
    private Boolean enable = true;

    @Schema(description = "是否引用")
    private Boolean association = false;

    @Schema(description = "文件编码")
    /**
     * 文件编码
     *
     * @see CsvEncodingType
     */
    private String encoding;

    @Schema(description = "是否随机")
    private Boolean random = false;

    @Schema(description = "变量名称(西文逗号间隔)")
    private String variableNames;

    @Schema(description = "忽略首行(只有在设置了变量名称后才生效)")
    private Boolean ignoreFirstLine = false;

    @Schema(description = "分隔符")
    private String delimiter;

    @Schema(description = "是否允许带引号")
    private Boolean allowQuotedData = false;

    @Schema(description = "遇到文件结束符再次循环")
    private Boolean recycleOnEof = true;

    @Schema(description = "遇到文件结束符停止线程")
    private Boolean stopThreadOnEof = false;


    public enum CsvEncodingType {
        UTF8("UTF-8"), UFT16("UTF-16"), ISO885915("ISO-8859-15"), US_ASCII("US-ASCII");
        private String value;

        CsvEncodingType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum CsvVariableScope {
        /**
         * 场景级：执行场景前加载CSV，当前场景任意步骤均可从CSV中读取到数据
         */
        SCENARIO,
        /**
         * 步骤级：需在测试步骤的“更多操作”中指定使用添加该CSV，执行该步骤时加载CSV，作用域为步骤内的请求
         */
        STEP
    }
}
