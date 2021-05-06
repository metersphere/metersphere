package io.metersphere.api.dto.definition.parse.esb;

import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/5/6 3:25 下午
 * @Description
 */
@Getter
@Setter
public class ESBExcelSheetInfo {
    //交易码下标
    private int simpleCodeIndex = 0;
    //交易名称下标
    private int simpleNameIndex = 0;
    //服务名下标
    private int serviceNameIndex = 0;
    //服务场景下标
    private int serviceScenarioIndex = 0;

    //接口名字下标
    private int apiNameIndex = 0;
    //数据类型下标
    private int dataTypeIndex = 0;
    //中文名称
    private int chineNameIndex = 0;
    //描述
    private int descIndex = 0;
    //api位置
    private int apiPositionIndex = 0;
    //每一行的单元格最大长度
    private int cellCount = 0;
    //请求信息起始行
    private int requestMessageRow = 0;
    //允许的最大空白行数（连续超过这个数字的空白行则认为sheet读取结束）
    private int maxEmptyRowCount = 20;

    public boolean installedApiInfoIndex() {
        return (apiNameIndex != 0 && dataTypeIndex != 0);
    }

    public void countApiPosisionIndex() {
        apiPositionIndex = apiNameIndex > apiPositionIndex ? apiNameIndex : apiPositionIndex;
        apiPositionIndex = dataTypeIndex > apiPositionIndex ? dataTypeIndex : apiPositionIndex;
        apiPositionIndex = chineNameIndex > apiPositionIndex ? chineNameIndex : apiPositionIndex;
        apiPositionIndex = descIndex > apiPositionIndex ? descIndex : apiPositionIndex;
        apiPositionIndex++;
        cellCount = apiPositionIndex;
    }
}
