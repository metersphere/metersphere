package io.metersphere.reportstatistics.dto.table;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseCountTableRowDTO {
    private List<TestCaseCountTableItemDataDTO> tableDatas;
}
