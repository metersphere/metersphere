package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

public interface ExtLoadTestFileMapper {
    public int updateFileIdByTestIdAndFileId(@Param("fileId") String fileId, @Param("testId") String loadTestId, @Param("oldFileId") String oldFileId);
}
