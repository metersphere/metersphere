package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiDefinitionDocType;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiDefinitionDocDTO;
import io.metersphere.api.dto.definition.ApiDefinitionDocRequest;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtShareInfoMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.ShareInfoType;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiShareControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/share/";

    private static final String SHARE_DOC = BASE_PATH + "/doc/gen";
    private static final String SHARE_VIEW = BASE_PATH + "/doc/view/";

    private static final String ALL_API = "api_definition_module.api.all";

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    private ExtShareInfoMapper extShareInfoMapper;

    @Resource
    private ShareInfoMapper shareInfoMapper;


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testShareDoc() throws Exception {
        ApiDefinitionDocRequest request = new ApiDefinitionDocRequest();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        request.setApiId(apiDefinition.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ApiDefinitionDocType.API.name());
        // @@请求成功
        this.requestPostWithOkAndReturn(SHARE_DOC, request);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(SHARE_DOC, request);
        ShareInfoDTO shareInfoDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ShareInfoDTO.class);
        // 校验数据是否正确
        List<ShareInfo> shareInfos = extShareInfoMapper.selectByShareTypeAndShareApiIdWithBLOBs(ShareInfoType.SINGLE.name(), JSON.toJSONString(request).getBytes(), "zh_CN");
        Assertions.assertNotNull(shareInfos);
        Assertions.assertEquals(1, shareInfos.size());
        Assertions.assertEquals(shareInfoDTO.getId(), shareInfos.getFirst().getId());
        Assertions.assertTrue(shareInfoDTO.getShareUrl().contains("?shareId="));

        request.setApiId("111");
        assertErrorCode(this.requestPost(SHARE_DOC, request), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@分享模块文档
        request.setApiId(null);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ApiDefinitionDocType.MODULE.name());
        request.setModuleIds(List.of("10001"));
        MvcResult mvcResultModule = this.requestPostWithOkAndReturn(SHARE_DOC, request);
        ShareInfoDTO shareInfoDTOModule = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultModule).get("data")), ShareInfoDTO.class);
        // 校验数据是否正确
        List<ShareInfo> shareInfosModule = extShareInfoMapper.selectByShareTypeAndShareApiIdWithBLOBs(ShareInfoType.BATCH.name(), JSON.toJSONString(request).getBytes(), "zh_CN");
        Assertions.assertNotNull(shareInfosModule);
        Assertions.assertEquals(1, shareInfosModule.size());
        Assertions.assertEquals(shareInfoDTOModule.getId(), shareInfosModule.getFirst().getId());
        Assertions.assertTrue(shareInfoDTOModule.getShareUrl().contains("?shareId="));

        // @@分享全部文档
        request.setApiId(null);
        request.setModuleIds(null);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ApiDefinitionDocType.ALL.name());
        MvcResult mvcResultAll = this.requestPostWithOkAndReturn(SHARE_DOC, request);
        ShareInfoDTO allShareInfoDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultAll).get("data")), ShareInfoDTO.class);

        // 校验数据是否正确
        List<ShareInfo> allShareInfos = extShareInfoMapper.selectByShareTypeAndShareApiIdWithBLOBs(ShareInfoType.BATCH.name(), JSON.toJSONString(request).getBytes(), "zh_CN");
        Assertions.assertNotNull(allShareInfos);
        Assertions.assertEquals(1, allShareInfos.size());
        Assertions.assertEquals(allShareInfoDTO.getId(), allShareInfos.getFirst().getId());
        Assertions.assertTrue(allShareInfoDTO.getShareUrl().contains("?shareId="));
        assertTestShareView(allShareInfoDTO.getId());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, SHARE_DOC, request);
    }


    public void assertTestShareView(String shareId) throws Exception {
        // @@请求成功
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        ApiDefinitionDocRequest apiDefinitionDocRequest = ApiDataUtils.parseObject(new String(shareInfo.getCustomData()), ApiDefinitionDocRequest.class);
        MvcResult mvcResultAll = this.requestGetWithOkAndReturn(SHARE_VIEW + shareId);
        ApiDefinitionDocDTO allApiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultAll).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyAllApiDefinitionDocDTO = new ApiDefinitionDocDTO();

        List<ApiDefinitionDTO> allList = extApiDefinitionMapper.listDoc(apiDefinitionDocRequest);
        if (null != allList) {
            ApiDefinitionDTO info = allList.stream().findFirst().orElseThrow(() -> new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST));
            ApiDefinitionBlob allApiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(info.getId());
            if (allApiDefinitionBlob != null) {
                info.setRequest(ApiDataUtils.parseObject(new String(allApiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
                info.setResponse(ApiDataUtils.parseArray(new String(allApiDefinitionBlob.getResponse()), HttpResponse.class));
            }
            if (StringUtils.isBlank(copyAllApiDefinitionDocDTO.getDocTitle())) {
                copyAllApiDefinitionDocDTO.setDocTitle(Translator.get(ALL_API));
            }
            copyAllApiDefinitionDocDTO.setType(ApiDefinitionDocType.ALL.name());
            copyAllApiDefinitionDocDTO.setDocInfo(info);
        }

        Assertions.assertEquals(allApiDefinitionDocDTO.getType(), copyAllApiDefinitionDocDTO.getType());
        Assertions.assertEquals(allApiDefinitionDocDTO.getDocTitle(), copyAllApiDefinitionDocDTO.getDocTitle());
        Assertions.assertEquals(allApiDefinitionDocDTO.getDocInfo().getId(), copyAllApiDefinitionDocDTO.getDocInfo().getId());
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, SHARE_VIEW + shareId);
    }

}
