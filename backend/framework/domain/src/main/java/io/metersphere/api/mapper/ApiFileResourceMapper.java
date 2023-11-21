package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiFileResource;
import io.metersphere.api.domain.ApiFileResourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiFileResourceMapper {
    long countByExample(ApiFileResourceExample example);

    int deleteByExample(ApiFileResourceExample example);

    int deleteByPrimaryKey(@Param("resourceId") String resourceId, @Param("fileId") String fileId);

    int insert(ApiFileResource record);

    int insertSelective(ApiFileResource record);

    List<ApiFileResource> selectByExample(ApiFileResourceExample example);

    ApiFileResource selectByPrimaryKey(@Param("resourceId") String resourceId, @Param("fileId") String fileId);

    int updateByExampleSelective(@Param("record") ApiFileResource record, @Param("example") ApiFileResourceExample example);

    int updateByExample(@Param("record") ApiFileResource record, @Param("example") ApiFileResourceExample example);

    int updateByPrimaryKeySelective(ApiFileResource record);

    int updateByPrimaryKey(ApiFileResource record);

    int batchInsert(@Param("list") List<ApiFileResource> list);

    int batchInsertSelective(@Param("list") List<ApiFileResource> list, @Param("selective") ApiFileResource.Column ... selective);
}