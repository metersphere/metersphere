package io.metersphere.system.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.excel.UserExcel;
import io.metersphere.system.dto.excel.UserExcelRowDTO;
import io.metersphere.system.dto.excel.UserExcelValidateHelper;
import io.metersphere.system.dto.sdk.ExcelParseDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class UserImportEventListener extends AnalysisEventListener<UserExcel> {
    private ExcelParseDTO<UserExcelRowDTO> excelParseDTO;

    public UserImportEventListener() {
        excelParseDTO = new ExcelParseDTO<>();
    }

    @Override
    public void invoke(UserExcel data, AnalysisContext analysisContext) {
        String errMsg;
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        try {
            //使用javax.validation校验excel数据
            errMsg = UserExcelValidateHelper.validateEntity(data);
            if (StringUtils.isEmpty(errMsg)) {
                errMsg = businessValidate(data);
            }
        } catch (NoSuchFieldException e) {
            errMsg = Translator.get("excel.parse.error");
            LogUtils.error(e.getMessage(), e);
        }
        UserExcelRowDTO userExcelRowDTO = new UserExcelRowDTO();
        BeanUtils.copyBean(userExcelRowDTO, data);
        userExcelRowDTO.setDataIndex(rowIndex);
        if (StringUtils.isEmpty(errMsg)) {
            excelParseDTO.addRowData(userExcelRowDTO);
        } else {
            userExcelRowDTO.setErrorMessage(errMsg);
            excelParseDTO.addErrorRowData(rowIndex, userExcelRowDTO);
        }
    }

    private String businessValidate(UserExcel rowData) {
        if (CollectionUtils.isNotEmpty(excelParseDTO.getDataList())) {
            for (UserExcelRowDTO userExcelRowDTO : excelParseDTO.getDataList()) {
                if (StringUtils.equalsIgnoreCase(userExcelRowDTO.getEmail(), rowData.getEmail())) {
                    return Translator.get("user.email.repeat") + ":" + rowData.getEmail();
                }
            }
        }
        return null;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    public ExcelParseDTO<UserExcelRowDTO> getExcelParseDTO() {
        return excelParseDTO;
    }
}
