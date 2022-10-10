package io.metersphere.metadata.vo;

import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class FileRequest {
    private String projectId;
    private String storage;
    private String resourceId;
    private String type;
    private String fileName;
    // 修改前名称
    private String beforeName;
    // 2.1版本前的历史遗留数据会有这两个字段
    private String resourceType;
    private String path;

    //文件附属信息
    private RemoteFileAttachInfo fileAttachInfo;

    public FileRequest() {

    }

    public FileRequest(String projectId, String name, String type) {
        this.storage = StorageConstants.MINIO.name();
        this.projectId = projectId;
        this.type = StringUtils.isNotEmpty(type) ? type.toLowerCase() : null;
        this.fileName = name;
        if (StringUtils.isNotEmpty(this.type) && !name.endsWith(this.type)) {
            this.fileName = StringUtils.join(name, ".", this.type);
        }
    }

    public void setFileAttachInfoByString(String attachInfoByString) {
        if (StringUtils.isNotEmpty(attachInfoByString)) {
            if (StringUtils.equals(this.storage, StorageConstants.GIT.name())) {
                this.setFileAttachInfo(JSON.parseObject(attachInfoByString, RemoteFileAttachInfo.class));
            }
        }
    }
}
