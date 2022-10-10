package io.metersphere.xpack.fake.error;

import io.metersphere.base.domain.ErrorReportLibraryExample;
import io.metersphere.base.domain.ErrorReportLibraryWithBLOBs;

import java.util.List;

public interface ErrorReportLibraryService {
    public List<ErrorReportLibraryWithBLOBs> selectByExampleWithBLOBs(ErrorReportLibraryExample example);
}
