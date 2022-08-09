package io.metersphere.base.mapper;

import io.metersphere.base.domain.FileAssociation;
import io.metersphere.base.domain.FileAssociationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileAssociationMapper {
    long countByExample(FileAssociationExample example);

    int deleteByExample(FileAssociationExample example);

    int deleteByPrimaryKey(String id);

    int insert(FileAssociation record);

    int insertSelective(FileAssociation record);

    List<FileAssociation> selectByExample(FileAssociationExample example);

    FileAssociation selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FileAssociation record, @Param("example") FileAssociationExample example);

    int updateByExample(@Param("record") FileAssociation record, @Param("example") FileAssociationExample example);

    int updateByPrimaryKeySelective(FileAssociation record);

    int updateByPrimaryKey(FileAssociation record);
}