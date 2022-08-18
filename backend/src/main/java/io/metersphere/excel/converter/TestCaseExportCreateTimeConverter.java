package io.metersphere.excel.converter;

import io.metersphere.commons.utils.DateUtils;
import io.metersphere.track.dto.TestCaseDTO;

public class TestCaseExportCreateTimeConverter implements TestCaseExportConverter {

    @Override
    public String parse(TestCaseDTO testCase) {
        return DateUtils.getTimeString(testCase.getCreateTime());
    }
}
