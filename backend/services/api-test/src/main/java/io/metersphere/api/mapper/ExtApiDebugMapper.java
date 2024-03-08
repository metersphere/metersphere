package io.metersphere.api.mapper;


import io.metersphere.api.dto.debug.ApiDebugSimpleDTO;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Mapper
public interface ExtApiDebugMapper {
    List<ApiDebugSimpleDTO> list(@Param("protocol") String protocol, @Param("userId") String userId);

    Long getPos(@Param("userId") String userId);

    Long getPrePos(@Param("userId") String userId, @Param("basePos") Long basePos);

    Long getLastPos(@Param("userId") String userId, @Param("basePos") Long basePos);

    void updatePos(String id, long pos);

    List<String> selectIdByProjectIdOrderByPos(String userId);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);
}