package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanCollectionMapper {
    long countByExample(TestPlanCollectionExample example);

    int deleteByExample(TestPlanCollectionExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanCollection record);

    int insertSelective(TestPlanCollection record);

    List<TestPlanCollection> selectByExample(TestPlanCollectionExample example);

    TestPlanCollection selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanCollection record, @Param("example") TestPlanCollectionExample example);

    int updateByExample(@Param("record") TestPlanCollection record, @Param("example") TestPlanCollectionExample example);

    int updateByPrimaryKeySelective(TestPlanCollection record);

    int updateByPrimaryKey(TestPlanCollection record);

    int batchInsert(@Param("list") List<TestPlanCollection> list);

    int batchInsertSelective(@Param("list") List<TestPlanCollection> list, @Param("selective") TestPlanCollection.Column ... selective);
}