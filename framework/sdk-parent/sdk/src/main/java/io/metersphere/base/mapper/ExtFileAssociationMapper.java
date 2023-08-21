package io.metersphere.base.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileAssociationMapper {
    List<String> getFileIdsByProjectIdAndType(@Param("projectId") String projectId, @Param("fileAssociationType") String fileAssociationType);
}