package io.metersphere.system.controller;


import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Service
public class ExtGetPosService {

    public Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos) {
        return 100L;
    }

    public Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos) {
        return 120L;
    }
}