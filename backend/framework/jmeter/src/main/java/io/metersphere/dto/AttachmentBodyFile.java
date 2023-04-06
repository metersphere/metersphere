package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentBodyFile {
    private String id;
    private String name;
    private String fileType;
    // 调试附件处理
    private String refResourceId;

    //JMX中的路径
    private String filePath;

    //在文件服务器里的路径
    private String remotePath;

    //在本地节点的路径
    private String localPath;
    private boolean isRef;
    private String fileMetadataId;
    private String projectId;
    private long FileUpdateTime;
    private String fileStorage;
    private String fileAttachInfoJson;
    private byte[] fileBytes;
}
