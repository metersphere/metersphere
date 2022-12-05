package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.mock.config.MockConfigImportDTO;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.BaseProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.definition.ApiModuleService;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

@Setter
@Getter
public class ApiDefinitionImportParamDTO {
    private ApiDefinitionWithBLOBs apiDefinition;
    private ApiTestImportRequest apiTestImportRequest;
    private List<MockConfigImportDTO> mocks;
    private List<ApiDefinitionWithBLOBs> updateList;
    private List<ApiTestCaseWithBLOBs> caseList;

    private List<ApiDefinitionWithBLOBs> repeatList;


    private SqlSessionFactory sqlSessionFactory;

    private ThreadLocal<Long> currentApiOrder;

    private ThreadLocal<Long> currentApiCaseOrder;

    private BaseProjectVersionMapper baseProjectVersionMapper;

    private ProjectMapper projectMapper;

    private ExtApiDefinitionMapper extApiDefinitionMapper;

    private ApiModuleMapper apiModuleMapper;

    private ApiTestCaseMapper apiTestCaseMapper;





    public ApiDefinitionImportParamDTO() {
    }

    public ApiDefinitionImportParamDTO(ApiDefinitionWithBLOBs apiDefinition, ApiTestImportRequest apiTestImportRequest, List<MockConfigImportDTO> mocks, List<ApiDefinitionWithBLOBs> updateList, List<ApiTestCaseWithBLOBs> caseList) {
        this.apiDefinition = apiDefinition;
        this.apiTestImportRequest = apiTestImportRequest;
        this.mocks = mocks;
        this.updateList = updateList;
        this.caseList = caseList;
    }


}
