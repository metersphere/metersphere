package io.metersphere.plugin.platform.enums;

public enum SyncAttachmentType {

    /**
     * 附件上传
     */
    UPLOAD("upload"),

    /**
     * 附件删除
     */
    DELETE("delete");

    private final String syncOperateType;

    SyncAttachmentType(String syncOperateType) {
        this.syncOperateType = syncOperateType;
    }

    public String syncOperateType() {
        return syncOperateType;
    }
}
