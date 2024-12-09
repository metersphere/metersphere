package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.*;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioFileService {
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ApiScenarioCsvMapper apiScenarioCsvMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiScenarioStepMapper apiScenarioStepMapper;
    @Resource
    private ApiScenarioCsvStepMapper apiScenarioCsvStepMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;

    /**
     * 处理保存时，copy的步骤中文件
     * @param apiScenarioCopyStepMap
     * @param stepDetails
     */
    public void handleSaveCopyStepFiles(ApiScenarioCopyStepMap apiScenarioCopyStepMap, Map<String, Object> stepDetails, ApiScenario scenario, String userId) {
        try {
            // 处理本地上传的文件
            List<ApiFileResource> apiFileResources = new ArrayList<>();
            apiFileResources.addAll(handleCopyFromStepFiles(stepDetails, apiScenarioCopyStepMap.getCopyFromStepIdMap(), scenario));
            apiFileResources.addAll(handleCopyApiFiles(stepDetails, apiScenarioCopyStepMap.getIsNewApiResourceMap(), scenario));
            apiFileResources.addAll(handleCopyApiCaseFiles(stepDetails, apiScenarioCopyStepMap.getIsNewApiCaseResourceMap(), scenario));
            if (CollectionUtils.isNotEmpty(apiFileResources)) {
                // 插入步骤和文件的关联关系
                apiFileResources.forEach(apiFileResource -> apiFileResource.setProjectId(scenario.getProjectId()));
                apiFileResourceMapper.batchInsert(apiFileResources);
            }

            // 处理关联的文件
            handleCopyStepAssociationFiles(apiScenarioCopyStepMap.getCopyFromStepIdMap(), scenario, userId);
            handleCopyStepAssociationFiles(apiScenarioCopyStepMap.getIsNewApiResourceMap(), scenario, userId);
            handleCopyStepAssociationFiles(apiScenarioCopyStepMap.getIsNewApiCaseResourceMap(), scenario, userId);
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * 处理保存时，copy的步骤中的关联文件
     * @param stepResourceIdMap
     * @param scenario
     * @param userId
     */
    private void handleCopyStepAssociationFiles(Map<String, String> stepResourceIdMap, ApiScenario scenario, String userId) {
        List<String> copyFromStepIds = new ArrayList<>(stepResourceIdMap.values());
        List<FileAssociation> fileAssociations = fileAssociationService.selectBySourceIds(copyFromStepIds);
        //  key 为资源ID，value 为步骤ID
        Map<String, String> resourceStepIdMap = new HashMap<>();
        stepResourceIdMap.forEach((k, v) -> resourceStepIdMap.put(v, k));
        for (FileAssociation fileAssociation : fileAssociations) {
            String stepId = resourceStepIdMap.get(fileAssociation.getSourceId());
            if (StringUtils.isNotBlank(stepId)) {
                fileAssociation.setSourceId(stepId);
                fileAssociation.setSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_SCENARIO_STEP);
                // 关联文件
                fileAssociationService.association(stepId, FileAssociationSourceUtil.SOURCE_TYPE_API_SCENARIO_STEP, List.of(fileAssociation.getFileId()),
                        apiFileResourceService.createFileLogRecord(userId, scenario.getProjectId(), OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO));
            }
        }
    }

    /**
     * 处理调试执行时，copy的步骤中文件
     * @param debugRequest
     * @param apiScenarioCopyStepMap
     * @param stepDetails
     */
    public void handleRunCopyStepFiles(ApiScenarioDebugRequest debugRequest, ApiScenarioCopyStepMap apiScenarioCopyStepMap, Map<String, Object> stepDetails) {
        try {
            List<ApiFileResource> apiFileResources = new ArrayList<>();
            apiFileResources.addAll(handleCopyFromStepFiles(stepDetails, apiScenarioCopyStepMap.getCopyFromStepIdMap(), null));
            apiFileResources.addAll(handleCopyApiFiles(stepDetails, apiScenarioCopyStepMap.getIsNewApiResourceMap(), null));
            apiFileResources.addAll(handleCopyApiCaseFiles(stepDetails, apiScenarioCopyStepMap.getIsNewApiCaseResourceMap(), null));

            if (debugRequest.getStepFileParam() == null) {
                debugRequest.setStepFileParam(new HashMap<>());
            }
            // 将copy的步骤中的文件设置为新上传的临时文件，执行时从临时目录获取
            Map<String, ResourceAddFileParam> stepFileParam = debugRequest.getStepFileParam();
            for (ApiFileResource apiFileResource : apiFileResources) {
                stepFileParam.putIfAbsent(apiFileResource.getResourceId(), new ResourceAddFileParam());
                ResourceAddFileParam resourceAddFileParam = stepFileParam.get(apiFileResource.getResourceId());
                if (resourceAddFileParam.getUploadFileIds() == null) {
                    resourceAddFileParam.setUploadFileIds(new ArrayList<>());
                }
                resourceAddFileParam.getUploadFileIds().add(apiFileResource.getFileId());
            }

            handleCopyStepAssociationFiles(debugRequest, apiScenarioCopyStepMap.getCopyFromStepIdMap());
            handleCopyStepAssociationFiles(debugRequest, apiScenarioCopyStepMap.getIsNewApiResourceMap());
            handleCopyStepAssociationFiles(debugRequest, apiScenarioCopyStepMap.getIsNewApiCaseResourceMap());
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * 处理执行时，copy的步骤中的关联文件
     * @param stepResourceIdMap
     */
    private void handleCopyStepAssociationFiles(ApiScenarioDebugRequest debugRequest, Map<String, String> stepResourceIdMap) {
        List<String> copyFromStepIds = new ArrayList<>(stepResourceIdMap.values());
        List<FileAssociation> fileAssociations = fileAssociationService.selectBySourceIds(copyFromStepIds);
        //  key 为资源ID，value 为步骤ID
        Map<String, String> resourceStepIdMap = new HashMap<>();
        stepResourceIdMap.forEach((k, v) -> resourceStepIdMap.put(v, k));
        for (FileAssociation fileAssociation : fileAssociations) {
            String stepId = resourceStepIdMap.get(fileAssociation.getSourceId());
            if (StringUtils.isNotBlank(stepId)) {
                debugRequest.getStepFileParam().putIfAbsent(stepId, new ResourceAddFileParam());
                ResourceAddFileParam resourceAddFileParam = debugRequest.getStepFileParam().get(stepId);
                if (resourceAddFileParam.getLinkFileIds() == null) {
                    resourceAddFileParam.setLinkFileIds(new ArrayList<>());
                }
                // 将copy的步骤中的文件设置为新上传的关联文件，执行时从关联文件中获取
                resourceAddFileParam.getLinkFileIds().add(fileAssociation.getFileId());
            }
        }
    }

    /**
     * 处理 copy 的步骤中的文件
     * 复制文件
     * 创建关联关系
     * 替换文件ID
     * @param stepDetails
     * @param copyFromStepIdMap
     */
    private List<ApiFileResource> handleCopyFromStepFiles(Map<String, Object> stepDetails, Map<String, String> copyFromStepIdMap, ApiScenario scenario) {
        if (copyFromStepIdMap.isEmpty()) {
            return List.of();
        }
        // 查询 copyFrom 的步骤所关联的文件
        List<String> copyFromStepIds = new ArrayList<>(copyFromStepIdMap.values());
        List<ApiFileResource> apiFileResources = apiFileResourceService.selectByResourceIds(copyFromStepIds);
        if (apiFileResources.isEmpty()) {
            return List.of();
        }
        Map<String, List<ApiFileResource>> stepFileMap = apiFileResources.stream().collect(Collectors.groupingBy(ApiFileResource::getResourceId));

        // 查询 copyFrom 步骤的场景ID Map
        List<String> hasFileCopyFromStepIds = stepFileMap.keySet().stream().toList();
        Map<String, String> copyFromStepScenarioMap  = getApiScenarioStepByIds(hasFileCopyFromStepIds).stream()
                .collect(Collectors.toMap(ApiScenarioStep::getId, ApiScenarioStep::getScenarioId));

        Map<String, String> fileIdMap = new HashMap<>();
        List<ApiFileResource> newApiFileResources = new ArrayList<>();
        for (String stepId : copyFromStepIdMap.keySet()) {
            List<ApiFileResource> originApiFileResources = stepFileMap.get(copyFromStepIdMap.get(stepId));
            if (CollectionUtils.isEmpty(originApiFileResources)) {
                continue;
            }

            boolean isSave = scenario != null;

            String newFileId = IDGenerator.nextStr();
            for (ApiFileResource originApiFileResource : originApiFileResources) {
                String sourceDir = DefaultRepositoryDir.getApiScenarioStepDir(originApiFileResource.getProjectId(),
                        copyFromStepScenarioMap.get(originApiFileResource.getResourceId()), originApiFileResource.getResourceId());

                // 如果是保存，则copy到正式目录，如果是执行，则copy到临时目录
                String targetDir = isSave ? DefaultRepositoryDir.getApiScenarioStepDir(scenario.getProjectId(), scenario.getId(), stepId)
                        : DefaultRepositoryDir.getSystemTempDir();

                // 复制文件
                apiFileResourceService.copyFile(sourceDir + "/" + originApiFileResource.getFileId(),
                        targetDir + "/" + newFileId,
                        originApiFileResource.getFileName());

                // 记录步骤和文件信息
                ApiFileResource newApiFileResource = getStepApiFileResource(stepId, newFileId, originApiFileResource.getFileName());
                newApiFileResources.add(newApiFileResource);

                // 记录文件ID映射
                fileIdMap.put(originApiFileResource.getFileId(), newFileId);
            }

            // 替换详情中的文件ID
            replaceCopyStepFileId(stepDetails, fileIdMap, stepId);
        }
        return newApiFileResources;
    }

    /**
     * 处理复制的接口定义步骤中的文件
     * 复制文件
     * 创建关联关系
     * 替换文件ID
     * @param stepDetails
     */
    private List<ApiFileResource> handleCopyApiFiles(Map<String, Object> stepDetails, Map<String, String> copyApiIdMap, ApiScenario scenario) {
        if (copyApiIdMap.isEmpty()) {
            return List.of();
        }
        // 查询 copy 的接口定义所关联的文件
        List<String> copyApiIds = new ArrayList<>(copyApiIdMap.values());
        List<ApiFileResource> apiFileResources = apiFileResourceService.selectByResourceIds(copyApiIds);
        if (apiFileResources.isEmpty()) {
            return List.of();
        }
        Map<String, List<ApiFileResource>> stepFileMap = apiFileResources.stream().collect(Collectors.groupingBy(ApiFileResource::getResourceId));

        Map<String, String> fileIdMap = new HashMap<>();
        List<ApiFileResource> newApiFileResources = new ArrayList<>();
        for (String stepId : copyApiIdMap.keySet()) {
            List<ApiFileResource> originApiFileResources = stepFileMap.get(copyApiIdMap.get(stepId));
            if (CollectionUtils.isEmpty(originApiFileResources)) {
                continue;
            }

            String newFileId = IDGenerator.nextStr();
            for (ApiFileResource originApiFileResource : originApiFileResources) {
                String sourceDir = DefaultRepositoryDir.getApiDefinitionDir(originApiFileResource.getProjectId(), originApiFileResource.getResourceId());

                boolean isSave = scenario != null;

                // 如果是保存，则copy到正式目录，如果是执行，则copy到临时目录
                String targetDir = isSave ? DefaultRepositoryDir.getApiScenarioStepDir(scenario.getProjectId(), scenario.getId(), stepId)
                        : DefaultRepositoryDir.getSystemTempDir();

                // 复制文件
                apiFileResourceService.copyFile(sourceDir + "/" + originApiFileResource.getFileId(),
                        targetDir + "/" + newFileId,
                        originApiFileResource.getFileName());

                // 记录步骤和文件信息
                ApiFileResource newApiFileResource = getStepApiFileResource(stepId, newFileId, originApiFileResource.getFileName());
                newApiFileResources.add(newApiFileResource);

                // 记录文件ID映射
                fileIdMap.put(originApiFileResource.getFileId(), newFileId);
            }

            // 替换详情中的文件ID
            replaceCopyStepFileId(stepDetails, fileIdMap, stepId);
        }

        return newApiFileResources;
    }

    /**
     * 处理复制的接口用例步骤中的文件
     * 复制文件
     * 创建关联关系
     * 替换文件ID
     * @param stepDetails
     */
    private List<ApiFileResource> handleCopyApiCaseFiles(Map<String, Object> stepDetails, Map<String, String> copyApiCaseIdMap, ApiScenario scenario) {
        if (copyApiCaseIdMap.isEmpty()) {
            return List.of();
        }
        // 查询 copy 的接口定义所关联的文件
        List<String> copyApiIds = new ArrayList<>(copyApiCaseIdMap.values());
        List<ApiFileResource> apiFileResources = apiFileResourceService.selectByResourceIds(copyApiIds);
        if (apiFileResources.isEmpty()) {
            return List.of();
        }
        Map<String, List<ApiFileResource>> stepFileMap = apiFileResources.stream().collect(Collectors.groupingBy(ApiFileResource::getResourceId));

        boolean isSave = scenario != null;
        Map<String, String> fileIdMap = new HashMap<>();
        List<ApiFileResource> newApiFileResources = new ArrayList<>();
        for (String stepId : copyApiCaseIdMap.keySet()) {
            List<ApiFileResource> originApiFileResources = stepFileMap.get(copyApiCaseIdMap.get(stepId));
            if (CollectionUtils.isEmpty(originApiFileResources)) {
                continue;
            }

            String newFileId = IDGenerator.nextStr();
            for (ApiFileResource originApiFileResource : originApiFileResources) {
                String sourceDir = DefaultRepositoryDir.getApiCaseDir(originApiFileResource.getProjectId(), originApiFileResource.getResourceId());

                // 如果是保存，则copy到正式目录，如果是执行，则copy到临时目录
                String targetDir = isSave ? DefaultRepositoryDir.getApiScenarioStepDir(scenario.getProjectId(), scenario.getId(), stepId)
                        : DefaultRepositoryDir.getSystemTempDir();

                // 复制文件
                apiFileResourceService.copyFile(sourceDir + "/" + originApiFileResource.getFileId(),
                        targetDir + "/" + newFileId,
                        originApiFileResource.getFileName());

                // 记录步骤和文件信息
                ApiFileResource newApiFileResource = getStepApiFileResource(stepId, newFileId, originApiFileResource.getFileName());
                newApiFileResources.add(newApiFileResource);

                // 记录文件ID映射
                fileIdMap.put(originApiFileResource.getFileId(), newFileId);
            }

            // 替换详情中的文件ID
            replaceCopyStepFileId(stepDetails, fileIdMap, stepId);
        }

        return newApiFileResources;
    }

    /**
     * 替换复制的步骤中详情的文件ID
     * @param stepDetails
     * @param fileIdMap
     * @param stepId
     */
    private void replaceCopyStepFileId(Map<String, Object> stepDetails, Map<String, String> fileIdMap, String stepId) {
        Object stepDetail = stepDetails.get(stepId);
        if (stepDetail != null) {
            // 替换详情中的文件ID
            if (stepDetail instanceof byte[] detailBytes) {
                AbstractMsTestElement msTestElement = apiCommonService.getAbstractMsTestElement(detailBytes);
                for (ApiFile apiFile : apiCommonService.getApiFiles(msTestElement)) {
                    if (fileIdMap.get(apiFile.getFileId()) != null) {
                        apiFile.setFileId(fileIdMap.get(apiFile.getFileId()));
                    }
                }
                stepDetails.put(stepId, msTestElement);
            } else if (stepDetail instanceof AbstractMsTestElement msTestElement) {
                for (ApiFile apiFile : apiCommonService.getApiFiles(msTestElement)) {
                    if (fileIdMap.get(apiFile.getFileId()) != null) {
                        apiFile.setFileId(fileIdMap.get(apiFile.getFileId()));
                    }
                }
            }
        }
    }

    private ApiFileResource getStepApiFileResource(String stepId, String fileId, String fileName) {
        ApiFileResource apiFileResource = new ApiFileResource();
        apiFileResource.setFileId(fileId);
        apiFileResource.setResourceId(stepId);
        apiFileResource.setResourceType(ApiFileResourceType.API_SCENARIO_STEP.name());
        apiFileResource.setCreateTime(System.currentTimeMillis());
        apiFileResource.setFileName(fileName);
        return apiFileResource;
    }

    private List<ApiScenarioStep> getApiScenarioStepByIds(List<String> stepIds) {
        if (CollectionUtils.isEmpty(stepIds)) {
            List.of();
        }
        ApiScenarioStepExample example = new ApiScenarioStepExample();
        example.createCriteria().andIdIn(stepIds);
        return apiScenarioStepMapper.selectByExample(example);
    }

    private ApiFileResourceUpdateRequest getStepApiFileResourceUpdateRequest(String userId, ApiScenario scenario, String stepId, ResourceAddFileParam fileParam) {
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(stepId, scenario.getProjectId(), userId);
        String apiScenarioStepDir = DefaultRepositoryDir.getApiScenarioStepDir(scenario.getProjectId(), scenario.getId(), stepId);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_SCENARIO_STEP);
        resourceUpdateRequest.setApiResourceType(ApiFileResourceType.API_SCENARIO_STEP);
        resourceUpdateRequest.setFolder(apiScenarioStepDir);
        resourceUpdateRequest = BeanUtils.copyBean(resourceUpdateRequest, fileParam);
        return resourceUpdateRequest;
    }

    public void handleStepFilesUpdate(ApiScenarioUpdateRequest request, String updater, ApiScenario scenario) {
        Map<String, ResourceUpdateFileParam> stepFileParam = request.getStepFileParam();
        if (MapUtils.isNotEmpty(stepFileParam)) {
            stepFileParam.forEach((stepId, fileParam) -> {
                // 处理步骤文件
                ApiFileResourceUpdateRequest resourceUpdateRequest = getStepApiFileResourceUpdateRequest(updater, scenario, stepId, fileParam);
                apiFileResourceService.updateFileResource(resourceUpdateRequest);
            });
        }
    }

    public void handleStepFilesAdd(ApiScenarioAddRequest request, String creator, ApiScenario scenario) {
        Map<String, ResourceAddFileParam> stepFileParam = request.getStepFileParam();
        if (MapUtils.isNotEmpty(stepFileParam)) {
            stepFileParam.forEach((stepId, fileParam) -> {
                // 处理步骤文件
                ApiFileResourceUpdateRequest resourceUpdateRequest = getStepApiFileResourceUpdateRequest(creator, scenario, stepId, fileParam);
                apiFileResourceService.addFileResource(resourceUpdateRequest);
            });
        }
    }

    public void handleCsvFileAdd(List<CsvVariable> csvVariables, List<ApiScenarioCsv> dbCsv, ApiScenario scenario, String userId) {
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(scenario.getId(), scenario.getProjectId(), userId);
        // 设置本地文件相关参数
        setCsvLocalFileParam(csvVariables, dbCsv, resourceUpdateRequest);
        // 设置关联文件相关参数
        setCsvLinkFileParam(csvVariables, dbCsv, resourceUpdateRequest);
        apiFileResourceService.addFileResource(resourceUpdateRequest);
    }

    public void handleCsvFileUpdate(List<CsvVariable> csvVariables, List<ApiScenarioCsv> dbCsv, ApiScenario scenario, String userId) {
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(scenario.getId(), scenario.getProjectId(), userId);
        // 设置本地文件相关参数
        setCsvLocalFileParam(csvVariables, dbCsv, resourceUpdateRequest);
        // 设置关联文件相关参数
        setCsvLinkFileParam(csvVariables, dbCsv, resourceUpdateRequest);
        apiFileResourceService.updateFileResource(resourceUpdateRequest);
    }

    public ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiScenarioDir = DefaultRepositoryDir.getApiScenarioDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiScenarioDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiFileResourceType.API_SCENARIO);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_SCENARIO);
        return resourceUpdateRequest;
    }

    public void setCsvLocalFileParam(List<CsvVariable> csvVariables, List<ApiScenarioCsv> dbCsv, ApiFileResourceUpdateRequest resourceUpdateRequest) {
        // 获取数据库中的本地文件
        List<String> dbLocalFileIds = dbCsv.stream()
                .filter(c -> BooleanUtils.isFalse(c.getAssociation()))
                .map(ApiScenarioCsv::getFileId)
                .toList();

        // 获取请求中的本地文件
        List<String> localFileIds = csvVariables.stream()
                .map(CsvVariable::getFile)
                .filter(c -> BooleanUtils.isTrue(c.getLocal()))
                .map(ApiFile::getFileId).toList();

        // 待删除文件
        List<String> deleteLocals = ListUtils.subtract(dbLocalFileIds, localFileIds);
        resourceUpdateRequest.setDeleteFileIds(deleteLocals);
        // 新上传文件
        List<String> addLocal = ListUtils.subtract(localFileIds, dbLocalFileIds);
        resourceUpdateRequest.setUploadFileIds(addLocal);
    }

    public List<ApiScenarioCsv> getApiScenarioCsv(String scenarioId) {
        ApiScenarioCsvExample apiScenarioCsvExample = new ApiScenarioCsvExample();
        apiScenarioCsvExample.createCriteria().andScenarioIdEqualTo(scenarioId);
        return apiScenarioCsvMapper.selectByExample(apiScenarioCsvExample);
    }

    public void deleteCsvResource(List<String> deleteCsvIds) {
        if (CollectionUtils.isNotEmpty(deleteCsvIds)) {
            ApiScenarioCsvExample example = new ApiScenarioCsvExample();
            example.createCriteria().andIdIn(deleteCsvIds);
            apiScenarioCsvMapper.deleteByExample(example);

            ApiScenarioCsvStepExample stepExample = new ApiScenarioCsvStepExample();
            stepExample.createCriteria().andIdIn(deleteCsvIds);
            apiScenarioCsvStepMapper.deleteByExample(stepExample);
        }
    }

    public void setCsvLinkFileParam(List<CsvVariable> csvVariables, List<ApiScenarioCsv> dbCsv, ApiFileResourceUpdateRequest resourceUpdateRequest) {
        // 获取数据库中关联的文件id
        List<String> dbRefFileIds = dbCsv.stream()
                .filter(c -> BooleanUtils.isTrue(c.getAssociation()) && StringUtils.isNotBlank(c.getFileId()))
                .map(ApiScenarioCsv::getFileId)
                .toList();

        // 获取请求中关联的文件id
        List<String> refFileIds = csvVariables.stream()
                .map(CsvVariable::getFile)
                .filter(c -> BooleanUtils.isFalse(c.getLocal()) && StringUtils.isNotBlank(c.getFileId()))
                .map(ApiFile::getFileId).toList();

        List<String> unlinkFileIds = ListUtils.subtract(dbRefFileIds, refFileIds);
        resourceUpdateRequest.setUnLinkFileIds(unlinkFileIds);
        List<String> linkFileIds = ListUtils.subtract(refFileIds, dbRefFileIds);
        resourceUpdateRequest.setLinkFileIds(linkFileIds);
    }

    public void handleCsvUpdate(ScenarioConfig scenarioConfig, ApiScenario scenario, String userId) {
        if (scenarioConfig == null) {
            return;
        }

        List<CsvVariable> csvVariables = getCsvVariables(scenarioConfig);
        List<ApiScenarioCsv> dbCsv = getApiScenarioCsv(scenario.getId());
        List<String> dbCsvIds = dbCsv.stream()
                .map(ApiScenarioCsv::getId)
                .toList();

        handleRefUpgradeFile(csvVariables, dbCsv);

        // 更新 csv 相关数据表
        handleCsvDataUpdate(csvVariables, scenario, dbCsvIds);

        // 处理文件的上传和删除
        handleCsvFileUpdate(csvVariables, dbCsv, scenario, userId);
    }

    public List<CsvVariable> getCsvVariables(ScenarioConfig scenarioConfig) {
        if (scenarioConfig == null || scenarioConfig.getVariable() == null || scenarioConfig.getVariable().getCsvVariables() == null) {
            return List.of();
        }
        return scenarioConfig.getVariable().getCsvVariables();
    }

    public void handleCsvDataUpdate(List<CsvVariable> csvVariables, ApiScenario scenario, List<String> dbCsvIds) {

        List<String> csvIds = csvVariables.stream()
                .map(CsvVariable::getId)
                .toList();

        List<String> deleteCsvIds = ListUtils.subtract(dbCsvIds, csvIds);

        //删除不存在的数据
        deleteCsvResource(deleteCsvIds);

        Set<String> dbCsvIdSet = dbCsvIds.stream().collect(Collectors.toSet());

        List<ApiScenarioCsv> addCsvList = new ArrayList<>();
        csvVariables.stream().forEach(item -> {
            ApiScenarioCsv scenarioCsv = new ApiScenarioCsv();
            BeanUtils.copyBean(scenarioCsv, item);
            scenarioCsv.setScenarioId(scenario.getId());
            scenarioCsv.setProjectId(scenario.getProjectId());

            ApiFile file = item.getFile();
            scenarioCsv.setFileId(file.getFileId());
            scenarioCsv.setFileName(file.getFileName());
            scenarioCsv.setAssociation(BooleanUtils.isFalse(file.getLocal()));
            if (!dbCsvIdSet.contains(item.getId())) {
                addCsvList.add(scenarioCsv);
            } else {
                apiScenarioCsvMapper.updateByPrimaryKey(scenarioCsv);
            }
        });

        if (CollectionUtils.isNotEmpty(addCsvList)) {
            apiScenarioCsvMapper.batchInsert(addCsvList);
        }
    }

    public List<CsvVariable> getCsvVariables(String scenarioId) {
        ApiScenarioCsvExample example = new ApiScenarioCsvExample();
        example.createCriteria().andScenarioIdEqualTo(scenarioId);
        List<ApiScenarioCsv> csvList = apiScenarioCsvMapper.selectByExample(example);
        List<CsvVariable> csvVariables = csvList.stream().map(apiScenarioCsv -> {
            CsvVariable csvVariable = BeanUtils.copyBean(new CsvVariable(), apiScenarioCsv);
            ApiFile apiFile = new ApiFile();
            apiFile.setFileId(apiScenarioCsv.getFileId());
            apiFile.setLocal(!apiScenarioCsv.getAssociation());
            apiFile.setFileName(apiScenarioCsv.getFileName());
            csvVariable.setFile(apiFile);
            return csvVariable;
        }).collect(Collectors.toList());
        return csvVariables;
    }

    /**
     * 当文件管理更新了关联资源的 csv 文件版本时
     * 前端文件并未更新，这里使用时，进行对比，使用较新的文件版本
     *
     * @param csvVariables
     * @param dbCsv
     */
    public void handleRefUpgradeFile(List<CsvVariable> csvVariables, List<ApiScenarioCsv> dbCsv) {
        try {
            // 获取数据库中关联的 csv 文件
            List<ApiScenarioCsv> dbAssociationCsvList = dbCsv.stream().filter(ApiScenarioCsv::getAssociation).toList();
            Map<String, ApiScenarioCsv> dbAssociationCsvIdMap = dbAssociationCsvList.stream()
                    .collect(Collectors.toMap(ApiScenarioCsv::getId, Function.identity()));

            // 获取与数据库中数据 fileId 不一致的 csv
            List<CsvVariable> changeAssociationCsvList = csvVariables.stream().filter(csvVariable -> {
                ApiScenarioCsv apiScenarioCsv = dbAssociationCsvIdMap.get(csvVariable.getId());
                if (apiScenarioCsv != null && csvVariable.getFile() != null && StringUtils.isNotBlank(csvVariable.getFile().getFileId())
                        && !StringUtils.equals(apiScenarioCsv.getFileId(), csvVariable.getFile().getFileId())) {
                    return true;
                }
                return false;
            }).toList();

            if (CollectionUtils.isEmpty(changeAssociationCsvList)) {
                return;
            }

            // 查询关联的csv文件信息
            List<String> dbAssociationCsvFileIds = changeAssociationCsvList.stream().map(csvVariable -> csvVariable.getFile().getFileId()).toList();
            FileMetadataExample fileMetadataExample = new FileMetadataExample();
            fileMetadataExample.createCriteria().andIdIn(dbAssociationCsvFileIds);
            List<FileMetadata> dbAssociationCsvFiles = fileMetadataMapper.selectByExample(fileMetadataExample);
            Map<String, FileMetadata> dbAssociationCsvFileMap = dbAssociationCsvFiles.stream()
                    .collect(Collectors.toMap(FileMetadata::getId, Function.identity()));

            // 查询csv文件的版本信息
            List<String> refIds = dbAssociationCsvFiles.stream().map(FileMetadata::getRefId).toList();
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andRefIdIn(refIds);
            List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(example)
                    .stream()
                    .sorted(Comparator.comparing(FileMetadata::getUpdateTime).reversed())
                    .collect(Collectors.toList());
            Map<String, List<FileMetadata>> refFileMap = fileMetadataList.stream().collect(Collectors.groupingBy(FileMetadata::getRefId));

            for (CsvVariable changeAssociation : changeAssociationCsvList) {
                String fileId = changeAssociation.getFile().getFileId();
                FileMetadata fileMetadata = dbAssociationCsvFileMap.get(fileId);
                ApiScenarioCsv dbAssociationCsv = dbAssociationCsvIdMap.get(changeAssociation.getId());
                // 遍历同一文件的不同版本
                List<FileMetadata> refFileList = refFileMap.get(fileMetadata.getRefId());
                if (refFileList != null) {
                    for (FileMetadata refFile : refFileList) {
                        if (StringUtils.equals(refFile.getId(), fileId)) {
                            // 如果前端参数的版本较新，则不处理
                            break;
                        } else if (StringUtils.equals(refFile.getId(), dbAssociationCsv.getFileId())) {
                            // 如果数据库中的文件版本较新，则说明文件管理中更新了当前引用的文件版本，使用数据库中的文件信息
                            changeAssociation.getFile().setFileId(dbAssociationCsv.getFileId());
                            changeAssociation.getFile().setFileName(dbAssociationCsv.getFileName());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }
}
