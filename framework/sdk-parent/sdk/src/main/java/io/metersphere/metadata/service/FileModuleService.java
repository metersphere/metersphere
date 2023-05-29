package io.metersphere.metadata.service;


import io.metersphere.base.domain.FileMetadataExample;
import io.metersphere.base.domain.FileModule;
import io.metersphere.base.domain.FileModuleExample;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.FileModuleMapper;
import io.metersphere.base.mapper.ext.BaseFileMetadataMapper;
import io.metersphere.base.mapper.ext.BaseFileModuleMapper;
import io.metersphere.commons.constants.ApiTestConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.ModuleReference;
import io.metersphere.metadata.vo.DragFileModuleRequest;
import io.metersphere.metadata.vo.FileModuleVo;
import io.metersphere.service.NodeTreeService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileModuleService extends NodeTreeService<FileModuleVo> {

    @Resource
    private FileModuleMapper fileModuleMapper;
    @Resource
    private BaseFileModuleMapper baseFileModuleMapper;
    @Resource
    private BaseFileMetadataMapper baseFileMetadataMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private FileMetadataMapper fileMetadataMapper;

    public FileModuleService() {
        super(FileModuleVo.class);
    }

    public FileModule get(String id) {
        return fileModuleMapper.selectByPrimaryKey(id);
    }

    public List<FileModuleVo> getNodeTreeByProjectId(String projectId) {
        // 判断当前项目下是否有默认模块，没有添加默认模块
        this.initDefaultNode(projectId);


        List<FileModuleVo> modules = baseFileModuleMapper.getNodeTreeByProjectId(projectId);
        List<String> ids = modules.stream().map(FileModuleVo::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return getNodeTrees(modules);
        }
        List<Map<String, Object>> moduleCounts = baseFileMetadataMapper.moduleCountByMetadataIds(ids);
        Map<String, Integer> moduleCountMap = this.nodeCalculate(moduleCounts);
        // 逐层统计
        if (MapUtils.isNotEmpty(moduleCountMap)) {
            modules.forEach(node -> {
                int countNum = 0;
                List<String> moduleIds = new ArrayList<>();
                moduleIds = this.nodeList(modules, node.getId(), moduleIds);
                moduleIds.add(node.getId());
                for (String moduleId : moduleIds) {
                    if (moduleCountMap.containsKey(moduleId)) {
                        countNum += moduleCountMap.get(moduleId).intValue();
                    }
                }
                node.setCaseNum(countNum);
            });
        }
        return getNodeTrees(modules);
    }

    private Map<String, Integer> nodeCalculate(List<Map<String, Object>> moduleCounts) {
        Map<String, Integer> returnMap = new HashMap<>();
        for (Map<String, Object> map : moduleCounts) {
            if (map.containsKey("moduleId") && map.containsKey("countNum")) {
                try {
                    String moduleId = String.valueOf(map.get("moduleId"));
                    Integer countNumInteger = new Integer(String.valueOf(map.get("countNum")));
                    returnMap.put(moduleId, countNumInteger);
                } catch (Exception e) {
                    LogUtil.error("method nodeCalculate has error:", e);
                }
            }
        }
        return returnMap;
    }

    public static List<String> nodeList(List<FileModuleVo> apiNodes, String pid, List<String> list) {
        for (FileModuleVo node : apiNodes) {
            //遍历出父id等于参数的id，add进子节点集合
            if (StringUtils.equals(node.getParentId(), pid)) {
                list.add(node.getId());
                //递归遍历下一级
                nodeList(apiNodes, node.getId(), list);
            }
        }
        return list;
    }

    public String addNode(FileModule node) {
        validateNode(node);
        return addNodeWithoutValidate(node);
    }

    public double getNextLevelPos(String projectId, int level, String parentId) {
        List<FileModule> list = getPos(projectId, level, parentId, "pos desc");
        if (!CollectionUtils.isEmpty(list) && list.get(0) != null && list.get(0).getPos() != null) {
            return list.get(0).getPos() + DEFAULT_POS;
        } else {
            return DEFAULT_POS;
        }
    }

    private List<FileModule> getPos(String projectId, int level, String parentId, String order) {
        FileModuleExample example = new FileModuleExample();
        FileModuleExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId).andLevelEqualTo(level);
        if (level != 1 && StringUtils.isNotBlank(parentId)) {
            criteria.andParentIdEqualTo(parentId);
        }
        example.setOrderByClause(order);
        return fileModuleMapper.selectByExample(example);
    }

    public String addNodeWithoutValidate(FileModule node) {
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        if (StringUtils.isBlank(node.getCreateUser())) {
            node.setCreateUser(SessionUtils.getUserId());
        }
        double pos = getNextLevelPos(node.getProjectId(), node.getLevel(), node.getParentId());
        node.setPos(pos);
        fileModuleMapper.insertSelective(node);
        return node.getId();
    }

    public FileModule getNewModule(String name, String projectId, int level) {
        FileModule node = new FileModule();
        buildNewModule(node);
        node.setLevel(level);
        node.setName(name);
        node.setProjectId(projectId);
        return node;
    }

    public FileModule buildNewModule(FileModule node) {
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        return node;
    }

    private void validateNode(FileModule node) {
        checkApiModuleExist(node);
    }

    private void checkApiModuleExist(FileModule node) {
        if (node.getName() != null) {
            List<String> names = new ArrayList<>() {{
                this.add("默认模块");
                this.add("Default module");
                this.add("默認模塊");
            }};
            if (names.contains(node.getName())) {
                MSException.throwException(Translator.get("test_case_module_already_exists") + ": " + node.getName());
            }
            FileModuleExample example = new FileModuleExample();
            FileModuleExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName()).andProjectIdEqualTo(node.getProjectId());
            if (StringUtils.isNotBlank(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andLevelEqualTo(node.getLevel());
            }

            if (StringUtils.isNotBlank(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            if (fileModuleMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists") + ": " + node.getName());
            }
        }
    }

    public List<FileModule> selectSameModule(FileModule node) {
        FileModuleExample example = new FileModuleExample();
        FileModuleExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(node.getName())
                .andProjectIdEqualTo(node.getProjectId())
                .andLevelEqualTo(node.getLevel());

        if (StringUtils.isNotBlank(node.getId())) {
            criteria.andIdNotEqualTo(node.getId());
        }
        //同一个模块下不能有相同名字的子模块
        if (StringUtils.isNotBlank(node.getParentId())) {
            criteria.andParentIdEqualTo(node.getParentId());
        }
        return fileModuleMapper.selectByExample(example);
    }

    public int editNode(DragFileModuleRequest request) {
        checkApiModuleExist(request);
        request.setUpdateTime(System.currentTimeMillis());
        return fileModuleMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        if (CollectionUtils.isNotEmpty(nodeIds)) {
            //删除文件
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andModuleIdIn(nodeIds);
            fileMetadataMapper.deleteByExample(example);

            FileModuleExample apiDefinitionNodeExample = new FileModuleExample();
            apiDefinitionNodeExample.createCriteria().andIdIn(nodeIds);
            return fileModuleMapper.deleteByExample(apiDefinitionNodeExample);
        }
        return 0;
    }

    @Override
    public FileModuleVo getNode(String id) {
        FileModule module = fileModuleMapper.selectByPrimaryKey(id);
        if (module == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(module), FileModuleVo.class);
    }

    @Override
    public void updatePos(String id, Double pos) {
        baseFileModuleMapper.updatePos(id, pos);
    }

    public void dragNode(DragFileModuleRequest request) {
        checkApiModuleExist(request);
        List<String> nodeIds = request.getNodeIds();
        FileModuleVo nodeTree = request.getNodeTree();
        List<FileModule> updateNodes = new ArrayList<>();
        if (nodeTree == null) {
            return;
        }
        buildUpdateDefinition(nodeTree, updateNodes, "/", "0", 1);

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateModule(updateNodes);
    }

    private void buildUpdateDefinition(FileModuleVo rootNode, List<FileModule> updateNodes, String rootPath, String pId, int level) {
        rootPath = rootPath + rootNode.getName();
        if ("root".equals(rootNode.getId())) {
            rootPath = "";
        }
        FileModule fileModule = new FileModule();
        fileModule.setId(rootNode.getId());
        fileModule.setLevel(level);
        fileModule.setParentId(pId);
        updateNodes.add(fileModule);

        List<FileModuleVo> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateDefinition(children.get(i), updateNodes, rootPath + '/', rootNode.getId(), level + 1);
            }
        }
    }

    private void batchUpdateModule(List<FileModule> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FileModuleMapper fileModuleMapper = sqlSession.getMapper(FileModuleMapper.class);
        updateNodes.forEach((value) -> {
            fileModuleMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public String getLogDetails(List<String> ids) {
        FileModuleExample example = new FileModuleExample();
        FileModuleExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        List<FileModule> nodes = fileModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            List<String> names = nodes.stream().map(FileModule::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), nodes.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(FileModule node) {
        FileModule module = null;
        if (StringUtils.isNotEmpty(node.getId())) {
            module = fileModuleMapper.selectByPrimaryKey(node.getId());
        }
        if (module == null && StringUtils.isNotEmpty(node.getName())) {
            FileModuleExample example = new FileModuleExample();
            FileModuleExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName()).andProjectIdEqualTo(node.getProjectId());

            if (StringUtils.isNotEmpty(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andParentIdIsNull();
            }
            if (StringUtils.isNotEmpty(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            List<FileModule> list = fileModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(list)) {
                module = list.get(0);
            }
        }
        if (module != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(module, ModuleReference.moduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(module.getId()), module.getProjectId(), module.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public long countById(String nodeId) {
        FileModuleExample example = new FileModuleExample();
        example.createCriteria().andIdEqualTo(nodeId);
        return fileModuleMapper.countByExample(example);
    }

    public FileModule initDefaultNode(String projectId) {
        FileModuleExample example = new FileModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(ApiTestConstants.DEF_MODULE).andParentIdIsNull();
        List<FileModule> list = fileModuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            FileModule fileModule = new FileModule();
            fileModule.setId(UUID.randomUUID().toString());
            fileModule.setName(ApiTestConstants.DEF_MODULE);
            fileModule.setPos(1.0);
            fileModule.setLevel(1);
            fileModule.setCreateTime(System.currentTimeMillis());
            fileModule.setUpdateTime(System.currentTimeMillis());
            fileModule.setProjectId(projectId);
            fileModule.setCreateUser(SessionUtils.getUserId());
            fileModuleMapper.insert(fileModule);

            // 历史数据处理
            baseFileMetadataMapper.updateModuleIdByProjectId(fileModule.getId(), projectId);
            return fileModule;
        } else {
            return list.get(0);
        }
    }

    public String getDefaultNodeId(String projectId) {
        FileModuleExample example = new FileModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(ApiTestConstants.DEF_MODULE).andParentIdIsNull();
        List<FileModule> list = fileModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0).getId();
        }
        return null;
    }

    public String getModuleNameById(String moduleId) {
        return baseFileModuleMapper.getNameById(moduleId);
    }

}
