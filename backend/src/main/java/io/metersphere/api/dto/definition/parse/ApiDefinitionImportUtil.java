package io.metersphere.api.dto.definition.parse;

import io.metersphere.api.dto.definition.ApiModuleDTO;
import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.ApiModuleService;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;

public class ApiDefinitionImportUtil {

    public static ApiModule getSelectModule(String moduleId) {
        return getSelectModule(moduleId, null);
    }

    public static ApiModule getSelectModule(String moduleId, String userId) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        if (StringUtils.isNotBlank(moduleId) && !StringUtils.equals("root", moduleId)) {
            ApiModule module = new ApiModule();
            ApiModuleDTO moduleDTO = apiModuleService.getNode(moduleId);
            if (moduleDTO != null) {
                BeanUtils.copyBean(module, moduleDTO);
            } else {
                if (StringUtils.isNotBlank(userId)) {
                    module.setCreateUser(userId);
                }
            }
            return module;
        }
        return null;
    }

    public static String getSelectModulePath(String path, String pid) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        if (StringUtils.isNotBlank(pid)) {
            ApiModuleDTO moduleDTO = apiModuleService.getNode(pid);
            if (moduleDTO != null) {
                return getSelectModulePath(moduleDTO.getName() + "/" + path, moduleDTO.getParentId());
            }
        }
        return "/" + path;
    }

    public static ApiModule getNodeTree(String projectId) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        List<ApiModuleDTO> nodeTrees = apiModuleService.getNodeTreeByProjectId(projectId, RequestType.HTTP);

        return null;
    }

    public static ApiModule buildModule(ApiModule parentModule, String name, String projectId) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        ApiModule module;
        if (parentModule != null) {
            module = apiModuleService.getNewModule(name, projectId, parentModule.getLevel() + 1);
            module.setParentId(parentModule.getId());
        } else {
            module = apiModuleService.getNewModule(name, projectId, 1);
        }
        createModule(module);
        return module;
    }

    private static void createNodeTree(NodeTree nodeTree, String pid, String projectId,
                                       ApiModuleService apiModuleService, String path, int baseLevel) {
        ApiModule module = new ApiModule();
        BeanUtils.copyBean(module, nodeTree);
        apiModuleService.buildNewModule(module);
        module.setProjectId(projectId);
        module.setParentId(pid);
        module.setLevel(module.getLevel() + baseLevel);
        createModule(module, SessionUtils.getUserId());
        nodeTree.setNewId(module.getId());
        path = path + nodeTree.getName();
        nodeTree.setPath(path);
        List<NodeTree> children = nodeTree.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            String finalPath = path;
            children.forEach(item -> {
                createNodeTree(item, module.getId(), projectId, apiModuleService, finalPath + "/", baseLevel);
            });
        }
    }

    /**
     * 根据导出的模块树，创建新的模块树
     * @param nodeTree
     * @param projectId
     */
    public static void createNodeTree(List<NodeTree> nodeTree, String projectId, String moduleId) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        Iterator<NodeTree> iterator = nodeTree.iterator();
        boolean hasModuleSelected = false;
        ApiModuleDTO selectModule = null;
        if (StringUtils.isNotBlank(moduleId) && !"root".equals(moduleId)) {
            selectModule = apiModuleService.getNode(moduleId);
            hasModuleSelected = true;
        }
        while (iterator.hasNext()) {
            NodeTree node = iterator.next();
            createNodeTree(node, hasModuleSelected ? selectModule.getId() : null,
                    projectId, apiModuleService, "/", hasModuleSelected ? selectModule.getLevel() : 0);
        }
    }

    public static ApiModule buildModule(ApiModule parentModule, String name, String projectId, String userId) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        ApiModule module;
        if (parentModule != null) {
            module = apiModuleService.getNewModule(name, projectId, parentModule.getLevel() + 1);
            module.setParentId(parentModule.getId());
        } else {
            module = apiModuleService.getNewModule(name, projectId, 1);
        }
        createModule(module, userId);
        return module;
    }

    public static void createModule(ApiModule module) {
       createModule(module, null);
    }

    public static void createModule(ApiModule module, String userId) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        module.setProtocol(RequestType.HTTP);
        if (module.getName().length() > 64) {
            module.setName(module.getName().substring(0, 64));
        }
        List<ApiModule> apiModules = apiModuleService.selectSameModule(module);
        if (CollectionUtils.isEmpty(apiModules)) {
            if (StringUtils.isNotBlank(userId)) {
                module.setCreateUser(userId);
            }
            apiModuleService.addNode(module);
        } else {
            module.setId(apiModules.get(0).getId());
        }
    }
}
