package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportAttachment;
import io.metersphere.plan.domain.TestPlanReportAttachmentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanReportAttachmentMapper {
    long countByExample(TestPlanReportAttachmentExample example);

    int deleteByExample(TestPlanReportAttachmentExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportAttachment record);

    int insertSelective(TestPlanReportAttachment record);

    List<TestPlanReportAttachment> selectByExample(TestPlanReportAttachmentExample example);

    TestPlanReportAttachment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportAttachment record, @Param("example") TestPlanReportAttachmentExample example);

    int updateByExample(@Param("record") TestPlanReportAttachment record, @Param("example") TestPlanReportAttachmentExample example);

    int updateByPrimaryKeySelective(TestPlanReportAttachment record);

    int updateByPrimaryKey(TestPlanReportAttachment record);

    int batchInsert(@Param("list") List<TestPlanReportAttachment> list);

    int batchInsertSelective(@Param("list") List<TestPlanReportAttachment> list, @Param("selective") TestPlanReportAttachment.Column ... selective);
}