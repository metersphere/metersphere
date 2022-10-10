package io.metersphere.api.parse.api.esb;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/23 12:55 下午
 * @Description
 */
@Getter
@Setter
public class EsbExcelDataStruct {
    private EsbSheetDataStruct headData;
    private List<EsbSheetDataStruct> interfaceList = new ArrayList<>();
}
