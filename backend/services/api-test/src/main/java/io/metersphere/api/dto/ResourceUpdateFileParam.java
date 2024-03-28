package io.metersphere.api.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-03-26  17:53
 */
@Data
public class ResourceUpdateFileParam extends ResourceAddFileParam {
    /**
     * 删除本地上传的文件ID
     */
    private List<String> deleteFileIds;

    /**
     * 删除关联的文件ID
     */
    private List<String> unLinkFileIds;
}
