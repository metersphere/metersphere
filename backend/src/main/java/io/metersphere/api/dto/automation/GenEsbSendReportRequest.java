package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/17 11:41 上午
 * @Description
 */
@Getter
@Setter
public class GenEsbSendReportRequest {
    String frontScript;
    List<EsbDataStruct> structList;
}
