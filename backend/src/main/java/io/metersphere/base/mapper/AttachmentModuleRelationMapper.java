package io.metersphere.base.mapper;

import io.metersphere.base.domain.AttachmentModuleRelation;
import io.metersphere.base.domain.AttachmentModuleRelationExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AttachmentModuleRelationMapper {
    long countByExample(AttachmentModuleRelationExample example);

    int deleteByExample(AttachmentModuleRelationExample example);

    int insert(AttachmentModuleRelation record);

    int insertSelective(AttachmentModuleRelation record);

    List<AttachmentModuleRelation> selectByExample(AttachmentModuleRelationExample example);

    int updateByExampleSelective(@Param("record") AttachmentModuleRelation record, @Param("example") AttachmentModuleRelationExample example);

    int updateByExample(@Param("record") AttachmentModuleRelation record, @Param("example") AttachmentModuleRelationExample example);
}