package io.metersphere.project.service;

import io.metersphere.project.mapper.ExtFileModuleMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileRepositoryService extends FileModuleService {

    //    @Resource
    //    private FileModuleMapper fileModuleMapper;
    @Resource
    private ExtFileModuleMapper extFileModuleMapper;
    //    @Resource
    //    private FileManagementService fileManagementService;
    //    @Resource
    //    private SqlSessionFactory sqlSessionFactory;

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> fileModuleList = extFileModuleMapper.selectBaseByProjectId(projectId, ModuleConstants.NODE_TYPE_GIT);
        return super.buildTreeAndCountResource(fileModuleList, false, Translator.get("default.module"));
    }
}
