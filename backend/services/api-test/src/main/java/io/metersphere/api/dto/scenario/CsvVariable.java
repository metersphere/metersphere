package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.ApiFile;
import lombok.Data;


/**
 * @Author: jianxing
 * @CreateTime: 2024-01-12  10:11
 */
@Data
public class CsvVariable {
    private String name;
    /**
     * @see CsvVariableScope
     */
    private String scope;
    /**
     * 文件信息
     */
    private ApiFile file;
    /**
     * 分隔符
     */
    private String delimiter;
    /**
     * 是否允许带引号
     */
    private Boolean allowQuotationMarks = false;
    /**
     * 是否忽略首行
     */
    private Boolean ignoreFirstLine = false;
    /**
     * 是否随机
     */
    private Boolean random = false;
    /**
     * 遇到文件结束符再次循环
     */
    private Boolean endFileLoopsAgain = true;
    /**
     * 遇到文件结束符停止线程
     */
    private Boolean endFileStopThread = false;
    /**
     * 文件编码
     * @see CsvEncodingType
     */
    private String encoding;


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
