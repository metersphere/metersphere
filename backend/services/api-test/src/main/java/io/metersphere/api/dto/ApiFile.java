package io.metersphere.api.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 接口执行所需要的文件
 */
@Data
public class ApiFile {
    /**
     * 记录文件的ID，防止重名
     * 生成脚本时，通过 fileId + fileName(文件名) 获取文件路径
     */
    private String fileId;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 是否是本地上传的文件
     */
    private Boolean local = true;
    /**
     * 文件别名，引用的文件需要展示别名，
     * 查询时，获取最新的
     */
    private String fileAlias;
    /**
     * 文件是否别删除
     * 引用的文件被删除，需要标识
     */
    private Boolean delete = false;

    public boolean isValid() {
        return StringUtils.isNotBlank(fileId) && StringUtils.isNotBlank(fileName) ;
    }
}
