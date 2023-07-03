package io.metersphere.system.dto.excel;

import lombok.Data;

@Data
public class UserExcelRowDTO extends UserExcel {
    public int dataIndex;
    public String errorMessage;
    public String userRoleId;
}
