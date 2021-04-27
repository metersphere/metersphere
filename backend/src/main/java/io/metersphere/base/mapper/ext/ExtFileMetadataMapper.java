package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileMetadataMapper {
    List<FileMetadata> selectFileOrJar(@Param("request") QueryCustomFieldRequest requests, @Param("projectIds") List<String> projectIds);
}
