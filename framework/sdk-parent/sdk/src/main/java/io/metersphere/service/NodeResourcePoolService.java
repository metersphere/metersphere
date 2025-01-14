package io.metersphere.service;

import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourceExample;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.i18n.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class NodeResourcePoolService {
    private final static String nodeControllerUrl = "http://%s:%s/status";


    @Resource
    private TestResourceMapper testResourceMapper;


    public boolean validate(TestResourcePoolDTO testResourcePool) {
        if (CollectionUtils.isEmpty(testResourcePool.getResources())) {
            MSException.throwException(Translator.get("no_nodes_message"));
        }


        List<ImmutablePair<String, Integer>> ipPort = testResourcePool.getResources().stream()
                .map(resource -> {
                    NodeDTO nodeDTO = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
                    return new ImmutablePair<>(nodeDTO.getIp(), nodeDTO.getPort());
                })
                .distinct()
                .toList();
        if (ipPort.size() < testResourcePool.getResources().size()) {
            MSException.throwException(Translator.get("duplicate_node_ip_port"));
        }

        List<TestResource> resourcesFromDB = getResourcesFromDB(testResourcePool);
        List<String> resourceIdsFromDB = resourcesFromDB.stream().map(TestResource::getId).collect(Collectors.toList());
        List<String> resourceIdsFromPage = testResourcePool.getResources().stream().map(TestResource::getId).collect(Collectors.toList());
        Collection<String> deletedResources = CollectionUtils.subtract(resourceIdsFromDB, resourceIdsFromPage);
        // 删除不关联的资源节点
        deleteTestResources(deletedResources);

        testResourcePool.setStatus(ResourceStatusEnum.VALID.name());
        // 动态线程池验证资源节点
        return validateResources(testResourcePool);
    }

    /**
     *  验证资源节点
     */
    public boolean validateResources(TestResourcePoolDTO testResourcePool) {
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(testResourcePool.getResources().size(), 10));
        AtomicBoolean shouldStop = new AtomicBoolean(false); // 共享变量
        try {
            List<Future<Boolean>> futures = new ArrayList<>();

            for (TestResource resource : testResourcePool.getResources()) {
                futures.add(executorService.submit(() -> {
                    if (shouldStop.get()) { // 检查是否需要提前停止
                        return false;
                    }
                    NodeDTO nodeDTO = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
                    boolean isValidate = validateNode(nodeDTO);
                    if (!isValidate) {
                        shouldStop.set(true); // 标志所有线程应该停止
                        testResourcePool.setStatus(ResourceStatusEnum.INVALID.name());
                        resource.setStatus(ResourceStatusEnum.INVALID.name());
                    } else {
                        resource.setStatus(ResourceStatusEnum.VALID.name());
                    }
                    resource.setTestResourcePoolId(testResourcePool.getId());
                    updateTestResource(resource);
                    return isValidate;
                }));
            }

            boolean isValid = true;
            for (Future<Boolean> future : futures) {
                try {
                    if (!future.get()) {
                        isValid = false;
                        break;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    isValid = false;
                    break;
                }
            }

            if (!isValid) {
                testResourcePool.setStatus(ResourceStatusEnum.INVALID.name());
            }
            return isValid;
        } finally {
            executorService.shutdownNow(); // 尝试终止所有线程
        }

    }

    private void deleteTestResources(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        TestResourceExample example = new TestResourceExample();
        example.createCriteria().andIdIn(new ArrayList<>(ids));
        testResourceMapper.deleteByExample(example);
    }

    private List<TestResource> getResourcesFromDB(TestResourcePoolDTO testResourcePool) {
        TestResourceExample example = new TestResourceExample();
        example.createCriteria().andTestResourcePoolIdEqualTo(testResourcePool.getId());
        example.setOrderByClause("create_time");
        return testResourceMapper.selectByExample(example);
    }

    private boolean validateNode(NodeDTO node) {
        try {
            URL url = new URL(String.format(nodeControllerUrl, node.getIp(), node.getPort()));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);  // 设置连接超时为 2 秒
            connection.setReadTimeout(2000);     // 设置读取超时为 2 秒
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;  // 判断是否返回 200 OK
        } catch (Exception e) {
            return false;
        }
    }

    private void updateTestResource(TestResource testResource) {
        testResource.setUpdateTime(System.currentTimeMillis());
        testResource.setCreateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(testResource.getId())) {
            testResource.setId(UUID.randomUUID().toString());
            testResourceMapper.insertSelective(testResource);
        } else {
            testResourceMapper.updateByPrimaryKeySelective(testResource);
        }
    }
}
