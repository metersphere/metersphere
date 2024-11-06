package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugCustomField;
import io.metersphere.bug.domain.BugCustomFieldExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugCustomFieldMapper {
    long countByExample(BugCustomFieldExample example);

    int deleteByExample(BugCustomFieldExample example);

    int deleteByPrimaryKey(@Param("bugId") String bugId, @Param("fieldId") String fieldId);

    int insert(BugCustomField record);

    int insertSelective(BugCustomField record);

    List<BugCustomField> selectByExampleWithBLOBs(BugCustomFieldExample example);

    List<BugCustomField> selectByExample(BugCustomFieldExample example);

    BugCustomField selectByPrimaryKey(@Param("bugId") String bugId, @Param("fieldId") String fieldId);

    int updateByExampleSelective(@Param("record") BugCustomField record, @Param("example") BugCustomFieldExample example);

    int updateByExampleWithBLOBs(@Param("record") BugCustomField record, @Param("example") BugCustomFieldExample example);

    int updateByExample(@Param("record") BugCustomField record, @Param("example") BugCustomFieldExample example);

    int updateByPrimaryKeySelective(BugCustomField record);

    int updateByPrimaryKeyWithBLOBs(BugCustomField record);

    int batchInsert(@Param("list") List<BugCustomField> list);

    int batchInsertSelective(@Param("list") List<BugCustomField> list, @Param("selective") BugCustomField.Column ... selective);
}