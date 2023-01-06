package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveApiTestCaseRequest extends ApiTestCase {

    private MsTestElement request;

    private String description;

    private String response;

    private String crateUserId;

    private List<String> bodyUploadIds;

    private List<String> follows;

    private String versionId;

    //复制用例时的源ID。主要用于复制本地文件等操作。
    private String sourceIdByCopy;
}
