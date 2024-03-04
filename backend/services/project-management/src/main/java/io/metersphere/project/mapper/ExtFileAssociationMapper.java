package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.dto.filemanagement.FileAssociationSource;
import io.metersphere.project.dto.filemanagement.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 注：本类中所有带有querySql参数的方法，不能直接传入sql，
 *  要使用FileAssociationResourceUtil.getQuerySql(sourceType)。防止SQL注入
 */
public interface ExtFileAssociationMapper {
    FileAssociationSource selectNameBySourceTableAndId(@Param("querySql") String querySql, @Param("sourceId") String sourceId);
    List<FileAssociationSource> selectAssociationSourceBySourceTableAndIdList(@Param("querySql") String querySql, @Param("idList") List<String> sourceIdList);

    List<FileInfo> selectAssociationFileInfo(@Param("sourceId") String sourceId);

    List<FileInfo> selectFileInfoBySourceIds(@Param("sourceIds") List<String> sourceIds);

    List<FileAssociation> selectFileIdsBySourceId(@Param("sourceIds")List<String> sourceIds, @Param("sourceType")String sourceType);
}
