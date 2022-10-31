package io.metersphere.commons.utils;

import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;

public class JmeterThreadUtils {

    /**
     * 获取并行集合线程名称
     *
     * @param dto
     * @return
     */
    public static String getThreadName(ResultDTO dto){
        return dto.getReportId()+ "@"+ dto.getTestId();
    }

    /**
     * 获取线程名称
     *
     * @param dto
     * @return
     */
    public static String getThreadName(JmeterRunRequestDTO dto){
        return dto.getReportId()+ "@"+ dto.getTestId();
    }

    /**
     * 根据线程名称获取reportId
     *
     * @param threadName
     * @return
     */
    public static String getReportId(String threadName){
        return threadName.split("@")[0];
    }
}
