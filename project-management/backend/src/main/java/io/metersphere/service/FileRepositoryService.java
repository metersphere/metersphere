package io.metersphere.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileAssociationMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestFileMapper;
import io.metersphere.commons.constants.FileAssociationType;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.FileRelevanceCaseDTO;
import io.metersphere.dto.FileVersionDTO;
import io.metersphere.dto.ReplaceFileIdRequest;
import io.metersphere.metadata.vo.RemoteFileAttachInfo;
import io.metersphere.remote.dto.LoadTestFileDTO;
import io.metersphere.remote.service.ApiRelateService;
import io.metersphere.remote.service.LoadTestRemoteService;
import io.metersphere.remote.service.TestTrackRemoteService;
import io.metersphere.request.QueryProjectFileRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileRepositoryService {
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private ApiRelateService apiRelateService;
    @Resource
    private LoadTestRemoteService loadTestRemoteService;
    @Resource
    private TestTrackRemoteService testTrackRemoteService;
    @Resource
    private ExtLoadTestFileMapper extLoadTestFileMapper;

    public List<FileVersionDTO> selectFileVersion(String refId) {
        List<FileVersionDTO> returnList = new ArrayList<>();
        if (StringUtils.isNotBlank(refId)) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andRefIdEqualTo(refId);
            List<FileMetadataWithBLOBs> fileMetadataList = this.fileMetadataMapper.selectByExampleWithBLOBs(example);
            fileMetadataList.sort(Comparator.comparing(FileMetadataWithBLOBs::getCreateTime).reversed());
            for (FileMetadataWithBLOBs fileMetadata : fileMetadataList) {
                try {
                    if (StringUtils.isNotBlank(fileMetadata.getAttachInfo())) {
                        RemoteFileAttachInfo gitFileAttachInfo = JSON.parseObject(fileMetadata.getAttachInfo(), RemoteFileAttachInfo.class);
                        User user = baseUserService.getUserDTO(fileMetadata.getCreateUser());
                        FileVersionDTO dto = new FileVersionDTO(fileMetadata.getId(), gitFileAttachInfo.getCommitId(), gitFileAttachInfo.getCommitMessage(),
                                user == null ? fileMetadata.getCreateUser() : user.getName(), fileMetadata.getCreateTime());
                        returnList.add(dto);
                    }
                } catch (Exception e) {
                    LogUtil.error("解析多版本下fileMetadata的attachInfo出错！", e);
                }
            }
        }
        return returnList;
    }

    public Pager<List<FileRelevanceCaseDTO>> getFileRelevanceCase(String refId, int goPage, int pageSize) {
        List<FileRelevanceCaseDTO> list = new ArrayList<>();
        Page<Object> page = null;
        if (StringUtils.isNotBlank(refId)) {
            FileMetadataExample fileMetadataExample = new FileMetadataExample();
            fileMetadataExample.createCriteria().andRefIdEqualTo(refId);
            List<FileMetadataWithBLOBs> fileMetadataWithBLOBsList = fileMetadataMapper.selectByExampleWithBLOBs(fileMetadataExample);
            if (CollectionUtils.isNotEmpty(fileMetadataWithBLOBsList)) {
                Map<String, String> fileCommitIdMap = new HashMap<>();
                fileMetadataWithBLOBsList.forEach(item -> {
                    if (StringUtils.isNotBlank(item.getAttachInfo()) && StringUtils.equals(item.getStorage(), StorageConstants.GIT.name())) {
                        try {
                            RemoteFileAttachInfo info = JSON.parseObject(item.getAttachInfo(), RemoteFileAttachInfo.class);
                            fileCommitIdMap.put(item.getId(), info.getCommitId());
                        } catch (Exception e) {
                            LogUtil.error("解析Git attachInfo失败！", e);
                        }
                    }
                });
                page = PageHelper.startPage(goPage, pageSize, true);
                FileAssociationExample associationExample = new FileAssociationExample();
                associationExample.createCriteria().andFileMetadataIdIn(new ArrayList<>(fileCommitIdMap.keySet())).andTypeIn(new ArrayList<>() {{
                    this.add(FileAssociationType.API.name());
                    this.add(FileAssociationType.CASE.name());
                    this.add(FileAssociationType.SCENARIO.name());
                    this.add("TEST_CASE");
                }});
                List<FileAssociation> fileAssociationList = fileAssociationMapper.selectByExample(associationExample);

                List<String> testCaseIdList = new ArrayList<>();
                List<String> apiIdList = new ArrayList<>();
                List<String> apiCaseIdList = new ArrayList<>();
                List<String> scenarioCaseIdList = new ArrayList<>();

                fileAssociationList.forEach(fileAssociation -> {
                    if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.API.name())) {
                        apiIdList.add(fileAssociation.getSourceId());
                    } else if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.CASE.name())) {
                        apiCaseIdList.add(fileAssociation.getSourceId());
                    } else if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.SCENARIO.name())) {
                        scenarioCaseIdList.add(fileAssociation.getSourceId());
                    } else if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.TEST_CASE.name())) {
                        testCaseIdList.add(fileAssociation.getSourceId());
                    }
                });

                List<ApiDefinition> apiDefinitionList = apiRelateService.selectApiDefinitionByIds(apiIdList);
                List<ApiTestCase> apiTestCaseList = apiRelateService.selectApiTestCaseByIds(apiCaseIdList);
                List<ApiScenario> apiScenarioList = apiRelateService.selectScenarioByIds(scenarioCaseIdList);
                List<TestCase> testCaseDTOList = testTrackRemoteService.selectTestCaseByIds(testCaseIdList);
                List<LoadTestFileDTO> loadTestFileList = loadTestRemoteService.selectByFileIds(new ArrayList<>(fileCommitIdMap.keySet()));

                Map<String, ApiDefinition> apiMap = apiDefinitionList.stream().collect(Collectors.toMap(ApiDefinition::getId, k1 -> k1));
                Map<String, ApiTestCase> apiCaseMap = apiTestCaseList.stream().collect(Collectors.toMap(ApiTestCase::getId, k1 -> k1));
                Map<String, TestCase> testCaseMap = testCaseDTOList.stream().collect(Collectors.toMap(TestCase::getId, k1 -> k1));
                Map<String, ApiScenario> scenarioMap = apiScenarioList.stream().collect(Collectors.toMap(ApiScenario::getId, k1 -> k1));
                for (FileAssociation fileAssociation : fileAssociationList) {
                    String caseId = null;
                    String caseName = null;
                    if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.API.name())) {
                        ApiDefinition apiDefinition = apiMap.get(fileAssociation.getSourceId());
                        if (apiDefinition != null) {
                            caseId = apiDefinition.getNum() == null ? StringUtils.EMPTY : apiDefinition.getNum().toString();
                            caseName = apiDefinition.getName();
                        }
                    } else if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.CASE.name())) {
                        ApiTestCase testCase = apiCaseMap.get(fileAssociation.getSourceId());
                        if (testCase != null) {
                            caseId = testCase.getNum() == null ? StringUtils.EMPTY : testCase.getNum().toString();
                            caseName = testCase.getName();
                        }
                    } else if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.SCENARIO.name())) {
                        ApiScenario testCase = scenarioMap.get(fileAssociation.getSourceId());
                        if (testCase != null) {
                            caseId = testCase.getNum() == null ? StringUtils.EMPTY : testCase.getNum().toString();
                            caseName = testCase.getName();
                        }
                    } else if (StringUtils.equals(fileAssociation.getType(), "TEST_CASE")) {
                        TestCase testCase = testCaseMap.get(fileAssociation.getSourceId());
                        if (testCase != null) {
                            caseId = testCase.getNum() == null ? StringUtils.EMPTY : testCase.getNum().toString();
                            caseName = testCase.getName();
                        }
                    }

                    if (!StringUtils.isAllBlank(caseId, caseName)) {
                        FileRelevanceCaseDTO dto = new FileRelevanceCaseDTO(fileAssociation.getId(), caseId, caseName, fileAssociation.getType(), fileCommitIdMap.get(fileAssociation.getFileMetadataId()));
                        list.add(dto);
                    }
                }
                for (LoadTestFileDTO loadTestFile : loadTestFileList) {
                    LoadTest loadTest = loadTestFile.getLoadTest();
                    if (loadTest != null) {
                        FileRelevanceCaseDTO dto = new FileRelevanceCaseDTO(loadTestFile.getFileId(), loadTest.getNum() + StringUtils.EMPTY, loadTest.getName(), "LOAD_CASE"
                                , fileCommitIdMap.get(loadTestFile.getFileId()));
                        list.add(dto);
                    }
                }
            }
        }

        if (page == null) {
            page = new Page<>(goPage, pageSize);
        }

        //排序一下
        list.sort(Comparator.comparing(FileRelevanceCaseDTO::getCaseName));
        return PageUtils.setPageInfo(page, list);
    }

    public String updateCaseVersion(String refId, QueryProjectFileRequest request) {
        int updateCount = 0;
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(request.getIds())) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andRefIdEqualTo(refId).andLatestEqualTo(true);
            List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(fileMetadataList)) {
                String latestId = fileMetadataList.get(0).getId();
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(request.getIds())) {
                    FileAssociationExample associationExample = new FileAssociationExample();
                    associationExample.createCriteria().andIdIn(request.getIds());
                    List<FileAssociation> fileAssociationList = fileAssociationMapper.selectByExample(associationExample);
                    //同步更新具体的场景/用例/接口的hashTree
                    this.updateResource(latestId, fileAssociationList);
                    FileAssociation fileAssociation = new FileAssociation();
                    fileAssociation.setFileMetadataId(latestId);
                    updateCount = fileAssociationMapper.updateByExampleSelective(fileAssociation, associationExample);
                }
                if (MapUtils.isNotEmpty(request.getLoadCaseFileIdMap())) {
                    for (Map.Entry<String, String> entry : request.getLoadCaseFileIdMap().entrySet()) {
                        String caseId = entry.getKey();
                        String fileId = entry.getValue();
                        updateCount += extLoadTestFileMapper.updateFileIdByTestIdAndFileId(latestId, caseId, fileId);
                    }
                }
            }
        }
        return String.valueOf(updateCount);
    }

    private void updateResource(String newFileMetadataId, List<FileAssociation> allFileAssociationList) {
        if (CollectionUtils.isNotEmpty(allFileAssociationList)) {
            Map<String, List<FileAssociation>> fileIdMap = allFileAssociationList.stream().collect(Collectors.groupingBy(FileAssociation::getFileMetadataId));
            List<ReplaceFileIdRequest> requestList = new ArrayList<>();
            for (Map.Entry<String, List<FileAssociation>> entry : fileIdMap.entrySet()) {
                String oldFileMetadataId = entry.getKey();
                List<FileAssociation> fileAssociationList = entry.getValue();

                List<String> apiIdList = new ArrayList<>();
                List<String> apiTestCaseIdList = new ArrayList<>();
                List<String> scenarioIdList = new ArrayList<>();

                fileAssociationList.forEach(item -> {
                    if (StringUtils.equals(item.getType(), FileAssociationType.API.name())) {
                        apiIdList.add(item.getSourceId());
                    } else if (StringUtils.equals(item.getType(), FileAssociationType.SCENARIO.name())) {
                        scenarioIdList.add(item.getSourceId());
                    } else if (StringUtils.equals(item.getType(), FileAssociationType.CASE.name())) {
                        apiTestCaseIdList.add(item.getSourceId());
                    }
                });

                ReplaceFileIdRequest request = new ReplaceFileIdRequest();
                request.setNewFileMetadataId(newFileMetadataId);
                request.setOldFileMetadataId(oldFileMetadataId);
                request.setApiIdList(apiIdList);
                request.setApiTestCaseIdList(apiTestCaseIdList);
                request.setApiScenarioIdList(scenarioIdList);
                requestList.add(request);
            }
            apiRelateService.updateFileMetadataIdByRequestList(requestList);
        }
    }
}
