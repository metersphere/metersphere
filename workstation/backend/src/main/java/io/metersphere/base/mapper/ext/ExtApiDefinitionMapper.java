package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtApiDefinitionMapper {

    int countByIds(@Param("ids") List<String> ids);

    int getCountFollow(@Param("projectIds") List<String> projectIds, @Param("userId") String userId);

    int getCountUpcoming(@Param("projectIds") List<String> projectIds, @Param("userId") String userId);

}
