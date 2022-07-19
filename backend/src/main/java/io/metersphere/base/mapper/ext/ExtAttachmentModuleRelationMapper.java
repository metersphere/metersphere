package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.AttachmentModuleRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author songcc
 */
public interface ExtAttachmentModuleRelationMapper {

    /**
     * 批量插入
     * @param attachmentModuleRelations 附件关系记录
     */
    void batchInsert(@Param("attachmentModuleRelations") List<AttachmentModuleRelation> attachmentModuleRelations);
}
