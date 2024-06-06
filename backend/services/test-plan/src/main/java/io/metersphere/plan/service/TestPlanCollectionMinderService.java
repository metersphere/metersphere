package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.TestPlanCollectionMinderTreeDTO;
import io.metersphere.plan.dto.TestPlanCollectionMinderTreeNodeDTO;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.ModuleConstants;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanCollectionMinderService {

    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    /**
     * 测试计划-脑图用例列表查询
     *
     * @return FunctionalMinderTreeDTO
     */
    public List<TestPlanCollectionMinderTreeDTO> getMindFunctionalCase(String planId) {
        List<TestPlanCollectionMinderTreeDTO> list = new ArrayList<>();
        TestPlanCollectionExample testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanCollection> testPlanCollections = testPlanCollectionMapper.selectByExample(testPlanCollectionExample);
        TestPlanCollectionMinderTreeNodeDTO testPlanCollectionMinderTreeNodeDTO = buildRoot();
        //
        TestPlanCollectionMinderTreeDTO testPlanCollectionMinderTreeDTO = new TestPlanCollectionMinderTreeDTO();
        testPlanCollectionMinderTreeDTO.setData(testPlanCollectionMinderTreeNodeDTO);
        List<TestPlanCollectionMinderTreeDTO> children = new ArrayList<>();
        List<TestPlanCollection> parentList = testPlanCollections.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)).sorted(Comparator.comparing(TestPlanCollection::getPos)).toList();
        buildTypeChildren(parentList, testPlanCollections, children);
        testPlanCollectionMinderTreeDTO.setChildren(children);
        list.add(testPlanCollectionMinderTreeDTO);
        return list;
    }

    private static void buildTypeChildren(List<TestPlanCollection> parentList, List<TestPlanCollection> testPlanCollections, List<TestPlanCollectionMinderTreeDTO> children) {
        for (TestPlanCollection testPlanCollection : parentList) {
            TestPlanCollectionMinderTreeDTO treeDTO = new TestPlanCollectionMinderTreeDTO();
            TestPlanCollectionMinderTreeNodeDTO typeTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
            typeTreeNodeDTO.setId(testPlanCollection.getId());
            typeTreeNodeDTO.setText(testPlanCollection.getName());
            typeTreeNodeDTO.setPos(testPlanCollection.getPos());
            if (StringUtils.equalsIgnoreCase(testPlanCollection.getExecuteMethod(), ApiBatchRunMode.PARALLEL.toString())) {
                typeTreeNodeDTO.setPriority("并");
            } else {
                typeTreeNodeDTO.setPriority("串");
            }
            typeTreeNodeDTO.setExecuteMethod(testPlanCollection.getExecuteMethod());
            List<TestPlanCollectionMinderTreeDTO> collectionChildren = new ArrayList<>();
            List<TestPlanCollection> childrenList = testPlanCollections.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getParentId(), testPlanCollection.getId())).sorted(Comparator.comparing(TestPlanCollection::getPos)).toList();
            buildCollectionChildren(childrenList, collectionChildren);
            treeDTO.setData(typeTreeNodeDTO);
            treeDTO.setChildren(collectionChildren);
            children.add(treeDTO);
        }
    }

    private static void buildCollectionChildren(List<TestPlanCollection> childrenList, List<TestPlanCollectionMinderTreeDTO> collectionChildren) {
        for (TestPlanCollection planCollection : childrenList) {
            TestPlanCollectionMinderTreeDTO collectionTreeDTO = new TestPlanCollectionMinderTreeDTO();
            TestPlanCollectionMinderTreeNodeDTO collectionTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
            collectionTreeNodeDTO.setId(planCollection.getId());
            collectionTreeNodeDTO.setText(planCollection.getName());
            collectionTreeNodeDTO.setPos(planCollection.getPos());
            if (StringUtils.equalsIgnoreCase(planCollection.getExecuteMethod(), ApiBatchRunMode.PARALLEL.toString())) {
                collectionTreeNodeDTO.setPriority("并");
            } else {
                collectionTreeNodeDTO.setPriority("串");
            }
            collectionTreeNodeDTO.setExecuteMethod(planCollection.getExecuteMethod());
            collectionTreeNodeDTO.setResource(new ArrayList<>());
            //TODO:构造子集


            collectionTreeDTO.setData(collectionTreeNodeDTO);
            collectionTreeDTO.setChildren(new ArrayList<>());
            collectionChildren.add(collectionTreeDTO);
        }
    }

    @NotNull
    private static TestPlanCollectionMinderTreeNodeDTO buildRoot() {
        TestPlanCollectionMinderTreeNodeDTO testPlanCollectionMinderTreeNodeDTO = new TestPlanCollectionMinderTreeNodeDTO();
        testPlanCollectionMinderTreeNodeDTO.setId(ModuleConstants.DEFAULT_NODE_ID);
        testPlanCollectionMinderTreeNodeDTO.setText("测试规划");
        testPlanCollectionMinderTreeNodeDTO.setResource(new ArrayList<>());
        return testPlanCollectionMinderTreeNodeDTO;
    }


}
