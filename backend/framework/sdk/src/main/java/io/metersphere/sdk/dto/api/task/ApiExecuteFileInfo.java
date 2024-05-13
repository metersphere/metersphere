package io.metersphere.sdk.dto.api.task;

import io.metersphere.sdk.dto.FileMetadataRepositoryDTO;
import io.metersphere.sdk.dto.FileModuleRepositoryDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-15  16:59
 */
@Data
public class ApiExecuteFileInfo  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String fileId;
    private String fileName;
    /**
     * 文件存储方式
     */
    private String storage;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 场景ID
     * 当 resourceType 为 API_SCENARIO_STEP 时
     * 该字段保存场景ID
     */
    private String scenarioId;

    /**
     * 资源类型
     * {@link io.metersphere.sdk.constants.ApiFileResourceType}
     */
    private String resourceType;

    /**
     * git文件的版本信息
     */
    private FileMetadataRepositoryDTO fileMetadataRepositoryDTO;
    /**
     * 文件的仓库信息
     */
    private FileModuleRepositoryDTO fileModuleRepositoryDTO;
}
