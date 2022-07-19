package io.metersphere.commons.constants;

/**
 * @author songcc
 */

public enum AttachmentSyncType {

    /**
     * 附件上传
     */
    UPLOAD("upload"),

    /**
     * 附件删除
     */
    DELETE("delete");

    private String syncOperateType;

    AttachmentSyncType(String syncOperateType) {
        this.syncOperateType = syncOperateType;
    }

    public String syncOperateType() {
        return syncOperateType;
    }
}
