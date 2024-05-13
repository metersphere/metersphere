package io.metersphere.api.dto.debug;

import io.metersphere.sdk.constants.ApiFileResourceType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  10:22
 */
@Data
public class ApiFileResourceUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 资源类型
     */
    private ApiFileResourceType apiResourceType;
    /**
     * 关联的资源ID
     */
    private String resourceId;
    /**
     * 文件存储的目录
     */
    private String folder;
    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    private List<String> uploadFileIds;
    /**
     * 新关联文件管理的文件ID
     */
    private List<String> linkFileIds;
    /**
     * 删除本地上传的文件ID
     */
    private List<String> deleteFileIds;
    /**
     * 删除关联的文件ID
     */
    private List<String> unLinkFileIds;
    /**
     * 文件关联的sourceType
     * @see io.metersphere.sdk.util.FileAssociationSourceUtil
     */
    private String FileAssociationSourceType;
    /**
     * 操作人
     * 记录文件相关操作日志
     */
    private String operator;

    /**
     * 记录日志模块
     * 记录文件相关操作日志
     */
    private String logModule;
}
