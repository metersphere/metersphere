package io.metersphere.project.service;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.FileModuleExample;
import io.metersphere.project.mapper.ExtFileModuleMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.request.filemanagement.FileModuleCreateRequest;
import io.metersphere.project.request.filemanagement.FileModuleUpdateRequest;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.dto.BaseModule;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.sdk.dto.request.NodeMoveRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.ModuleTreeService;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.service.CleanupProjectResourceService;
import io.metersphere.system.uid.UUID;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileModuleService extends ModuleTreeService implements CleanupProjectResourceService {
    @Resource
    private FileModuleLogService fileModuleLogService;
    @Resource
    private FileModuleMapper fileModuleMapper;
    @Resource
    private ExtFileModuleMapper extFileModuleMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private FileManagementService fileManagementService;

    public List<BaseTreeNode> getTree(String projectId) {
        BaseTreeNode defaultNode = this.getDefaultModule();
        List<BaseTreeNode> baseTreeNodeList = new ArrayList<>();
        baseTreeNodeList.add(defaultNode);
        List<FileModule> fileModuleList = extFileModuleMapper.selectBaseByProjectId(projectId);
        int lastSize = 0;
        Map<String, BaseTreeNode> baseTreeNodeMap = new HashMap<>();
        while (CollectionUtils.isNotEmpty(fileModuleList) && fileModuleList.size() != lastSize) {
            List<FileModule> notMatchedList = new ArrayList<>();
            for (FileModule fileModule : fileModuleList) {
                if (StringUtils.equals(fileModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    BaseTreeNode node = new BaseTreeNode(fileModule.getId(), fileModule.getName(), ModuleConstants.NODE_TYPE_DEFAULT);
                    baseTreeNodeList.add(node);
                    baseTreeNodeMap.put(fileModule.getId(), node);
                } else {
                    if (baseTreeNodeMap.containsKey(fileModule.getParentId())) {
                        BaseTreeNode node = new BaseTreeNode(fileModule.getId(), fileModule.getName(), ModuleConstants.NODE_TYPE_DEFAULT);
                        baseTreeNodeMap.get(fileModule.getParentId()).addChild(node);
                        baseTreeNodeMap.put(fileModule.getId(), node);
                    } else {
                        notMatchedList.add(fileModule);
                    }
                }
            }
            fileModuleList = notMatchedList;
        }
        return baseTreeNodeList;
    }

    public String add(FileModuleCreateRequest request, String operator) {
        FileModule fileModule = new FileModule();
        fileModule.setId(UUID.randomUUID().toString());
        fileModule.setName(request.getName());
        fileModule.setParentId(request.getParentId());
        fileModule.setProjectId(request.getProjectId());
        this.checkDataValidity(fileModule);
        fileModule.setCreateTime(System.currentTimeMillis());
        fileModule.setUpdateTime(fileModule.getCreateTime());
        fileModule.setPos(this.countPos(request.getParentId()));
        fileModule.setCreateUser(operator);
        fileModule.setUpdateUser(operator);
        fileModule.setModuleType(ModuleConstants.NODE_TYPE_DEFAULT);
        fileModuleMapper.insert(fileModule);

        //记录日志
        fileModuleLogService.saveAddLog(fileModule, operator);
        return fileModule.getId();
    }

    private Integer countPos(String parentId) {
        Integer maxPos = extFileModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    /**
     * 检查数据的合法性
     */
    private void checkDataValidity(FileModule fileModule) {
        FileModuleExample example = new FileModuleExample();
        if (!StringUtils.equals(fileModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //检查父ID是否存在
            example.createCriteria().andIdEqualTo(fileModule.getParentId());
            if (fileModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();

            if (StringUtils.isNotBlank(fileModule.getProjectId())) {
                //检查项目ID是否和父节点ID一致
                example.createCriteria().andProjectIdEqualTo(fileModule.getProjectId()).andIdEqualTo(fileModule.getParentId());
                if (fileModuleMapper.countByExample(example) == 0) {
                    throw new MSException(Translator.get("project.cannot.match.parent"));
                }
                example.clear();
            }
        }
        example.createCriteria().andParentIdEqualTo(fileModule.getParentId()).andNameEqualTo(fileModule.getName()).andIdNotEqualTo(fileModule.getId());
        if (fileModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
    }

    public void update(FileModuleUpdateRequest request, String userId) {
        FileModule module = fileModuleMapper.selectByPrimaryKey(request.getId());
        if (module == null) {
            throw new MSException("file_module.not.exist");
        }
        FileModule updateModule = new FileModule();
        updateModule.setId(request.getId());
        updateModule.setName(request.getName());
        updateModule.setParentId(module.getParentId());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        fileModuleMapper.updateByPrimaryKeySelective(updateModule);
        //记录日志
        fileModuleLogService.saveUpdateLog(updateModule, module.getProjectId(), userId);
    }


    public void deleteModule(String deleteId) {
        FileModule deleteModule = fileModuleMapper.selectByPrimaryKey(deleteId);
        if (deleteModule != null) {
            this.deleteModule(Collections.singletonList(deleteId));
            //记录日志
            fileModuleLogService.saveDeleteLog(deleteModule, SessionUtils.getUserId());
        }
    }
    public void deleteModule(List<String> deleteIds) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        extFileModuleMapper.deleteByIds(deleteIds);
        fileManagementService.deleteByModuleIds(deleteIds);

        List<String> childrenIds = extFileModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds);
        }
    }

    public void moveNode(NodeMoveRequest request) {
        FileModuleExample example = new FileModuleExample();
        example.createCriteria().andParentIdEqualTo(request.getParentId()).andIdEqualTo(request.getNodeId());
        if (fileModuleMapper.countByExample(example) == 0) {
            //节点换到了别的节点下
            FileModule fileModule = new FileModule();
            fileModule.setId(request.getNodeId());
            fileModule.setParentId(request.getParentId());
            fileModuleMapper.updateByPrimaryKeySelective(fileModule);
        }
        this.sort(request);

        //记录日志
        fileModuleLogService.saveMoveLog(request, SessionUtils.getUserId());
    }

    @Override
    public BaseModule getNode(String id) {
        FileModule module = fileModuleMapper.selectByPrimaryKey(id);
        if (module == null) {
            return null;
        } else {
            return new BaseModule(module.getId(), module.getName(), module.getPos(), module.getProjectId(), module.getParentId());
        }
    }

    @Override
    public void updatePos(String id, int pos) {
        FileModule updateModule = new FileModule();
        updateModule.setPos(pos);
        updateModule.setId(id);
        fileModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = extFileModuleMapper.selectChildrenIdsSortByPos(parentId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FileModuleMapper batchUpdateMapper = sqlSession.getMapper(FileModuleMapper.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            FileModule updateModule = new FileModule();
            updateModule.setId(nodeId);
            updateModule.setPos((i + 1) * LIMIT_POS);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModule);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    @Override
    public void deleteResources(String projectId) {
        List<String> fileModuleIdList = extFileModuleMapper.selectIdsByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(fileModuleIdList)) {
            this.deleteModule(fileModuleIdList);
        }
    }

    @Override
    public void cleanReportResources(String projectId) {

    }

}
