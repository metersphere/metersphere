package io.metersphere.api.dto.debug;

import io.metersphere.api.constants.ApiResourceType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  10:22
 */
@Data
public class ApiFileResourceUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 资源类型
     */
    private ApiResourceType apiResourceType;
    /**
     * 关联的资源ID
     */
    private String resourceId;
    /**
     * 文件存储的目录
     */
    private String folder;
    /**
     * 接口所需的所有文件资源ID
     */
    private List<String> fileIds;

    /**
     * 新上传的文件ID
     * 更新时记录新上传的文件ID，与上传的文件顺序保持一致
     */
    private List<String> addFileIds;
}
