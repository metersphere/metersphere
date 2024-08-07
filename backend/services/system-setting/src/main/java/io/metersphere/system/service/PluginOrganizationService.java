package io.metersphere.system.service;

import io.metersphere.system.domain.PluginOrganization;
import io.metersphere.system.domain.PluginOrganizationExample;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.ExtOrganizationMapper;
import io.metersphere.system.mapper.PluginOrganizationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PluginOrganizationService {

    @Resource
    private PluginOrganizationMapper pluginOrganizationMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;

    public void add(String pluginId, List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return;
        }
        List<PluginOrganization> pluginOrganizations = new ArrayList<>(orgIds.size());
        for (String orgId : orgIds) {
            PluginOrganization pluginOrganization = new PluginOrganization();
            pluginOrganization.setPluginId(pluginId);
            pluginOrganization.setOrganizationId(orgId);
            pluginOrganizations.add(pluginOrganization);
        }
        pluginOrganizationMapper.batchInsert(pluginOrganizations);
    }

    public void deleteByPluginId(String pluginId) {
        PluginOrganizationExample example = new PluginOrganizationExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        pluginOrganizationMapper.deleteByExample(example);
    }

    public void deleteByOrgId(String orgId) {
        PluginOrganizationExample example = new PluginOrganizationExample();
        example.createCriteria().andOrganizationIdEqualTo(orgId);
        pluginOrganizationMapper.deleteByExample(example);
    }

    public void update(String pluginId, List<String> organizationIds) {
        if (organizationIds == null) {
            // 如果参数没填，则不更新
            return;
        }
        // 先删除关联关系
        deleteByPluginId(pluginId);
        // 重新添加关联关系
        add(pluginId, organizationIds);
    }

    public Map<String, List<OptionDTO>> getOrgMap(List<String> pluginIds) {
        if (CollectionUtils.isEmpty(pluginIds)) {
            return Collections.emptyMap();
        }
        // 查询插件和组织的关联关系
        PluginOrganizationExample example = new PluginOrganizationExample();
        example.createCriteria().andPluginIdIn(pluginIds);
        List<PluginOrganization> pluginOrganizations = pluginOrganizationMapper.selectByExample(example);

        // 查询组织信息
        List<String> orgIds = pluginOrganizations.stream().map(PluginOrganization::getOrganizationId).toList();
        List<OptionDTO> orgList = getOptionsByIds(orgIds);
        Map<String, OptionDTO> orgInfoMap = orgList.stream().collect(Collectors.toMap(OptionDTO::getId, i -> i));

        // 组装成 map
        Map<String, List<OptionDTO>> orgMap = new HashMap<>();
        for (PluginOrganization pluginOrganization : pluginOrganizations) {
            String pluginId = pluginOrganization.getPluginId();
            String orgId = pluginOrganization.getOrganizationId();
            OptionDTO orgInfo = orgInfoMap.get(orgId);
            if (orgInfo == null) {
                continue;
            }
            orgMap.computeIfAbsent(pluginId, k -> new ArrayList<>())
                    .add(orgInfo);
        }
        return orgMap;
    }

    public List<OptionDTO> getOptionsByIds(List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>(0);
        }
        return extOrganizationMapper.getOptionsByIds(orgIds);
    }
}
