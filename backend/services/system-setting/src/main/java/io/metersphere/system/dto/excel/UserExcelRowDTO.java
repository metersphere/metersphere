package io.metersphere.system.dto.excel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserExcelRowDTO extends UserExcel {
    public int dataIndex;
    public String errorMessage;
    public String userRoleId;
}
