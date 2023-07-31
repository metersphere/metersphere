package io.metersphere.system.mapper;

import io.metersphere.system.domain.License;
import io.metersphere.system.domain.LicenseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LicenseMapper {
    long countByExample(LicenseExample example);

    int deleteByExample(LicenseExample example);

    int deleteByPrimaryKey(String id);

    int insert(License record);

    int insertSelective(License record);

    List<License> selectByExampleWithBLOBs(LicenseExample example);

    List<License> selectByExample(LicenseExample example);

    License selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") License record, @Param("example") LicenseExample example);

    int updateByExampleWithBLOBs(@Param("record") License record, @Param("example") LicenseExample example);

    int updateByExample(@Param("record") License record, @Param("example") LicenseExample example);

    int updateByPrimaryKeySelective(License record);

    int updateByPrimaryKeyWithBLOBs(License record);

    int updateByPrimaryKey(License record);

    int batchInsert(@Param("list") List<License> list);

    int batchInsertSelective(@Param("list") List<License> list, @Param("selective") License.Column ... selective);
}