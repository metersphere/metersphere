package io.metersphere.base.mapper;

import io.metersphere.base.domain.ShareInfo;
import io.metersphere.base.domain.ShareInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ShareInfoMapper {
    long countByExample(ShareInfoExample example);

    int deleteByExample(ShareInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(ShareInfo record);

    int insertSelective(ShareInfo record);

    List<ShareInfo> selectByExampleWithBLOBs(ShareInfoExample example);

    List<ShareInfo> selectByExample(ShareInfoExample example);

    ShareInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ShareInfo record, @Param("example") ShareInfoExample example);

    int updateByExampleWithBLOBs(@Param("record") ShareInfo record, @Param("example") ShareInfoExample example);

    int updateByExample(@Param("record") ShareInfo record, @Param("example") ShareInfoExample example);

    int updateByPrimaryKeySelective(ShareInfo record);

    int updateByPrimaryKeyWithBLOBs(ShareInfo record);

    int updateByPrimaryKey(ShareInfo record);
}