package io.metersphere.service;

import io.metersphere.base.domain.Project;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.TreeNodeDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NodeTreeService<T extends TreeNodeDTO> {

    protected static final double LIMIT_POS = 64;
    protected static final double DEFAULT_POS = 65536;
    protected Class clazz;

    public NodeTreeService(Class clazz) {
        this.clazz = clazz;
    }

    public T getClassInstance() {
        T instance = null;
        try {
            instance = (T) clazz.newInstance();
        } catch (InstantiationException e) {
            LogUtil.error(e);
        } catch (IllegalAccessException e) {
            LogUtil.error(e);
        }
        return instance;
    }


    public List<T> getNodeTrees(List<T> nodes) {
        return getNodeTrees(nodes, null);
    }

    public Map<String, Integer> getCountMap(List<T> nodes) {
        Map<String, Integer> countMap = nodes.stream()
                .collect(Collectors.toMap(TreeNodeDTO::getId, TreeNodeDTO::getCaseNum));
        return countMap;
    }

    public List<T> getNodeTrees(List<T> nodes, Map<String, Integer> countMap) {
        List<T> nodeTreeList = new ArrayList<>();
        Map<Integer, List<T>> nodeLevelMap = new HashMap<>();
        nodes.forEach(node -> {
            Integer level = node.getLevel();
            if (nodeLevelMap.containsKey(level)) {
                nodeLevelMap.get(level).add(node);
            } else {
                List<T> testCaseNodes = new ArrayList<>();
                testCaseNodes.add(node);
                nodeLevelMap.put(node.getLevel(), testCaseNodes);
            }
        });
        List<T> rootNodes = Optional.ofNullable(nodeLevelMap.get(1)).orElse(new ArrayList<>());
        rootNodes.forEach(rootNode -> nodeTreeList.add(buildNodeTree(nodeLevelMap, rootNode, countMap)));
        return nodeTreeList;
    }

    /**
     * 递归构建节点树
     * 并统计设置 CaseNum
     *
     * @param nodeLevelMap
     * @param rootNode
     * @return
     */
    public T buildNodeTree(Map<Integer, List<T>> nodeLevelMap, T rootNode, Map<String, Integer> countMap) {

        T nodeTree = getClassInstance();
        BeanUtils.copyBean(nodeTree, rootNode);
        nodeTree.setLabel(rootNode.getName());
        setCaseNum(countMap, nodeTree);

        List<T> lowerNodes = nodeLevelMap.get(rootNode.getLevel() + 1);
        if (lowerNodes == null) {
            return nodeTree;
        }

        List<T> children = new ArrayList<>();

        lowerNodes.forEach(node -> {
            if (node.getParentId() != null && node.getParentId().equals(rootNode.getId())) {
                children.add(buildNodeTree(nodeLevelMap, node, countMap));
                nodeTree.setChildren(children);
            }
        });
        if (countMap != null && CollectionUtils.isNotEmpty(children)) {
            Integer childrenCount = children.stream().map(TreeNodeDTO::getCaseNum).reduce(Integer::sum).get();
            nodeTree.setCaseNum(nodeTree.getCaseNum() + childrenCount);
        }
        return nodeTree;
    }

    public T buildNodeTree(Map<Integer, List<T>> nodeLevelMap, T rootNode) {
        return buildNodeTree(nodeLevelMap, rootNode, null);
    }

    private void setCaseNum(Map<String, Integer> countMap, T nodeTree) {
        if (countMap != null) {
            if (countMap.get(nodeTree.getId()) != null) {
                nodeTree.setCaseNum(countMap.get(nodeTree.getId()));
            } else {
                nodeTree.setCaseNum(0);
            }
        }
    }

    /**
     * 用户测试计划评审或者公共用例库查询多个项目的模块
     *
     * @param countModules          带有用例的节点的信息
     * @param getProjectModulesFunc 根据 projectIds 获取多个项目下的模块
     * @return
     */
    public List<T> getNodeTreeWithPruningTree(List<T> countModules,
                                              Function<List<String>, List<T>> getProjectModulesFunc) {
        if (org.springframework.util.CollectionUtils.isEmpty(countModules)) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();

        Set<String> projectIdSet = new HashSet<>();
        countModules.forEach(x -> projectIdSet.add(x.getProjectId()));
        List<String> projectIds = new ArrayList<>(projectIdSet);

        BaseProjectService projectService = CommonBeanFactory.getBean(BaseProjectService.class);
        List<Project> projects = projectService.getProjectByIds(new ArrayList<>(projectIds));

        // 项目->对应项目下的模块
        Map<String, List<T>> projectModuleMap = getProjectModulesFunc.apply(projectIds)
                .stream()
                .collect(Collectors.groupingBy(TreeNodeDTO::getProjectId));

        // 模块->用例数
        Map<String, Integer> countMap = countModules.stream()
                .collect(Collectors.toMap(TreeNodeDTO::getId, TreeNodeDTO::getCaseNum));

        projects.forEach((project) -> {
            if (project != null) {
                List<T> testCaseNodes = projectModuleMap.get(project.getId());

                testCaseNodes = testCaseNodes.stream().sorted(Comparator.comparingDouble(TreeNodeDTO::getPos))
                        .collect(Collectors.toList());

                testCaseNodes = getNodeTreeWithPruningTreeByCaseCount(testCaseNodes, countMap);

                // 项目设置成根节点
                T projectNode = getClassInstance();
                projectNode.setId(project.getId());
                projectNode.setName(project.getName());
                projectNode.setLabel(project.getName());
                projectNode.setChildren(testCaseNodes);
                projectNode.setCaseNum(testCaseNodes.stream().mapToInt(TreeNodeDTO::getCaseNum).sum());
                if (countMap.get(null) != null) {
                    // 如果模块删除了, 回收站中的用例归到项目模块下
                    projectNode.setCaseNum(projectNode.getCaseNum() + countMap.get(null));
                }
                if (!org.springframework.util.CollectionUtils.isEmpty(testCaseNodes)) {
                    list.add(projectNode);
                }
            }
        });
        return list;
    }

    /**
     * 生成模块树并剪枝
     *
     * @return
     */
    public List<T> getNodeTreeWithPruningTreeByCaseCount(List<T> testCaseNodes, Map<String, Integer> countMap) {
        List<T> nodeTrees = getNodeTrees(testCaseNodes, countMap);
        Iterator<T> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            T rootNode = iterator.next();
            if (pruningTreeByCaseCount(rootNode)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }

    /**
     * 去除没有数据的节点
     *
     * @param rootNode
     * @param nodeIds
     * @return 是否剪枝
     */
    public boolean pruningTree(T rootNode, List<String> nodeIds) {

        List<T> children = rootNode.getChildren();

        if (children == null || children.isEmpty()) {
            //叶子节点,并且该节点无数据
            if (!nodeIds.contains(rootNode.getId())) {
                return true;
            }
        }

        if (children != null) {
            Iterator<T> iterator = children.iterator();
            while (iterator.hasNext()) {
                T subNode = iterator.next();
                if (pruningTree(subNode, nodeIds)) {
                    iterator.remove();
                }
            }

            if (children.isEmpty() && !nodeIds.contains(rootNode.getId())) {
                return true;
            }
        }

        return false;
    }

    public boolean pruningTreeByCaseCount(T rootNode) {
        List<T> children = rootNode.getChildren();

        if (rootNode.getCaseNum() == null || rootNode.getCaseNum() < 1) {
            // 没有用例的模块剪掉
            return true;
        }

        if (children != null) {
            Iterator<T> iterator = children.iterator();
            while (iterator.hasNext()) {
                T subNode = iterator.next();
                if (pruningTreeByCaseCount(subNode)) {
                    iterator.remove();
                }
            }
        }
        return false;
    }

    /**
     * 根据目标节点路径，创建相关节点
     *
     * @param pathIterator 遍历子路径
     * @param path         当前路径
     * @param treeNode     当前节点
     * @param pathMap      记录节点路径对应的nodeId
     */
    protected void createNodeByPathIterator(Iterator<String> pathIterator, String path, T treeNode,
                                            Map<String, String> pathMap, String projectId, Integer level) {

        List<T> children = treeNode.getChildren();

        if (children == null || children.isEmpty() || !pathIterator.hasNext()) {
            pathMap.put(path, treeNode.getId());
            if (pathIterator.hasNext()) {
                createNodeByPath(pathIterator, pathIterator.next().trim(), treeNode, projectId, level, path, pathMap);
            }
            return;
        }

        String nodeName = pathIterator.next().trim();

        Boolean hasNode = false;

        for (T child : children) {
            if (StringUtils.equals(nodeName, child.getName())) {
                hasNode = true;
                createNodeByPathIterator(pathIterator, path + "/" + child.getName(),
                        child, pathMap, projectId, level + 1);
            }
            ;
        }

        //若子节点中不包含该目标节点，则在该节点下创建
        if (!hasNode) {
            createNodeByPath(pathIterator, nodeName, treeNode, projectId, level, path, pathMap);
        }

    }

    /**
     * @param pathIterator 迭代器，遍历子节点
     * @param nodeName     当前节点
     * @param pNode        父节点
     */
    protected void createNodeByPath(Iterator<String> pathIterator, String nodeName,
                                    T pNode, String projectId, Integer level,
                                    String rootPath, Map<String, String> pathMap) {

        StringBuilder path = new StringBuilder(rootPath);

        path.append("/" + nodeName.trim());

        String pid;
        //创建过不创建
        if (pathMap.get(path.toString()) != null) {
            pid = pathMap.get(path.toString());
            level++;
        } else {
            pid = insertNode(nodeName, pNode == null ? null : pNode.getId(), projectId, level, path.toString());
            pathMap.put(path.toString(), pid);
            level++;
        }

        while (pathIterator.hasNext()) {
            String nextNodeName = pathIterator.next().trim();
            path.append("/" + nextNodeName);
            if (pathMap.get(path.toString()) != null) {
                pid = pathMap.get(path.toString());
                level++;
            } else {
                pid = insertNode(nextNodeName, pid, projectId, level, path.toString());
                pathMap.put(path.toString(), pid);
                level++;
            }
        }
    }


    /**
     * 测试用例同级模块排序
     *
     * @param ids 被拖拽模块相邻的前一个模块 id，
     *            被拖拽的模块 id，
     *            被拖拽模块相邻的后一个模块 id
     */
    public void sort(List<String> ids) {
        // 获取相邻节点 id
        if (CollectionUtils.isEmpty(ids))
            return;
        String before = ids.get(0);
        String id = ids.get(1);
        String after = ids.get(2);

        T beforeNode = null;
        T afterNode = null;

        T node = getNode(id);

        // 获取相邻节点
        if (StringUtils.isNotBlank(before)) {
            beforeNode = getNode(before);
            beforeNode = beforeNode.getLevel().equals(node.getLevel()) ? beforeNode : null;
        }

        if (StringUtils.isNotBlank(after)) {
            afterNode = getNode(after);
            afterNode = afterNode.getLevel().equals(node.getLevel()) ? afterNode : null;
        }

        double pos;

        if (beforeNode == null) {
            pos = afterNode != null ? afterNode.getPos() / 2.0 : DEFAULT_POS;
        } else {
            pos = afterNode != null ? (beforeNode.getPos() + afterNode.getPos()) / 2.0 : beforeNode.getPos() + DEFAULT_POS;
        }

        node.setPos(pos);
        updatePos(node.getId(), node.getPos());

        // pos 低于阈值时，触发更新方法，重新计算此目录的所有同级目录的 pos 值
        if (pos < LIMIT_POS) {
            refreshPos(node.getProjectId(), node.getLevel(), node.getParentId());
        }
    }


    public String insertNode(String nodeName, String pId, String projectId, Integer level, String path) {
        return StringUtils.EMPTY;
    }

    public void updatePos(String id, Double pos) {
    }

    protected void refreshPos(String projectId, int level, String parentId) {
    }

    public T getNode(String id) {
        return null;
    }

}
