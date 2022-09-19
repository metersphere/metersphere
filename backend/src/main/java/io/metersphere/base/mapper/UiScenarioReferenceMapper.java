package io.metersphere.base.mapper;

import io.metersphere.base.domain.UiScenarioReference;
import io.metersphere.base.domain.UiScenarioReferenceExample;
import java.util.List;

import io.metersphere.xpack.ui.dto.*;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReferenceMapper {
    long countByExample(UiScenarioReferenceExample example);

    int deleteByExample(UiScenarioReferenceExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReference record);

    int insertSelective(UiScenarioReference record);

    List<UiScenarioReference> selectByExample(UiScenarioReferenceExample example);

    UiScenarioReference selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReference record, @Param("example") UiScenarioReferenceExample example);

    int updateByExample(@Param("record") UiScenarioReference record, @Param("example") UiScenarioReferenceExample example);

    int updateByPrimaryKeySelective(UiScenarioReference record);

    int updateByPrimaryKey(UiScenarioReference record);

    /**
     * 获取引用信息
     */
    List<RefResp> refList(@Param("request") RefReq request);

    List<TestPlanRefResp> getTestPlanRef(@Param("request") RefReq request);

    List<UiCheckRefDTO> checkRef(@Param("request")UiCheckRefReq request);
}