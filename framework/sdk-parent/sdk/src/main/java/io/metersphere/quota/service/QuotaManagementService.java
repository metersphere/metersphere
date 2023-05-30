package io.metersphere.quota.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.Quota;
import io.metersphere.base.domain.QuotaExample;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.QuotaMapper;
import io.metersphere.base.mapper.ext.ExtQuotaMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.quota.dto.QuotaConstants;
import io.metersphere.quota.dto.QuotaResult;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Quota 中 project_id 和 workspace_id 不同时存在
 * project_id不为空: 表示数据是项目配额
 * workspace_id不为空: 表示数据是工作空间配额
 * project_id、workspace_id都为空: 表示数据是默认配额
 * 工作空间默认配额(全局唯一)：QuotaConstants.DefaultType.workspace
 * 项目默认配额(工作空间下唯一)：QuotaConstants.prefix + workspace_id
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class QuotaManagementService {

    @Resource
    private QuotaMapper quotaMapper;
    @Resource
    private ExtQuotaMapper extQuotaMapper;
    @Resource
    private ProjectMapper projectMapper;

    public Quota getDefaultQuota(QuotaConstants.DefaultType type) {
        Quota quota = quotaMapper.selectByPrimaryKey(type.name());
        if (quota == null) {
            quota = new Quota();
            quota.setId(type.name());
        }
        return quota;
    }

    public Quota getProjectDefaultQuota(String workspaceId) {
        if (StringUtils.isBlank(workspaceId)) {
            return new Quota();
        }
        String workspaceGlobalQuotaId = getWorkspaceGlobalQuotaId(workspaceId);
        Quota quota = quotaMapper.selectByPrimaryKey(workspaceGlobalQuotaId);
        Quota workspaceQuota = getWorkspaceQuota(workspaceId);
        if (ObjectUtils.isNotEmpty(workspaceQuota) && ObjectUtils.isNotEmpty(quota) && StringUtils.isNotBlank(workspaceQuota.getResourcePool())) {
            List<String> works = Arrays.asList(workspaceQuota.getResourcePool().split(","));
            List<String> projects = Arrays.asList(quota.getResourcePool().split(","));
            List<String> intersection = works.stream()
                    .filter(projects::contains)
                    .collect(Collectors.toList());
            quota.setResourcePool(String.join(",", intersection));
        }
        if (quota == null) {
            quota = new Quota();
            quota.setId(workspaceGlobalQuotaId);
        }
        return quota;
    }

    private String getWorkspaceGlobalQuotaId(String workspaceId) {
        if (StringUtils.isBlank(workspaceId)) {
            MSException.throwException("get workspace global quota id error, workspace id is null.");
        }
        return QuotaConstants.prefix + workspaceId;
    }

    public void saveQuota(Quota quota) {
        if (!isDefaultQuota(quota)) {
            // 保存项目配额时，检查工作空间下所有项目配额总和是否大于工作空间配额
            if (StringUtils.isNotBlank(quota.getProjectId())) {
                // 使用默认配额
                if (BooleanUtils.isTrue(quota.getUseDefault())) {
                    Project project = projectMapper.selectByPrimaryKey(quota.getProjectId());
                    if (project == null || StringUtils.isBlank(project.getWorkspaceId())) {
                        MSException.throwException("save project quota fail. project is null");
                    }
                    useDefaultQuota(quota, getProjectDefaultQuota(project.getWorkspaceId()));
                }
                vumCompare(quota.getVumTotal(), quota.getVumUsed());
                checkProjectQuota(quota);
                quota.setWorkspaceId(null);
            }

            // 保存工作空间配额时，检查是否小于项目配额总和
            if (StringUtils.isBlank(quota.getProjectId()) && StringUtils.isNotBlank(quota.getWorkspaceId())) {
                // 使用默认配额
                if (BooleanUtils.isTrue(quota.getUseDefault())) {
                    useDefaultQuota(quota, getDefaultQuota(QuotaConstants.DefaultType.workspace));
                }
                wsVumCompare(quota);
                checkWorkspaceQuota(quota);
            }
        } else {
            checkDefaultQuota(quota);
        }

        quota.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotBlank(quota.getId())) {
            Quota qt = quotaMapper.selectByPrimaryKey(quota.getId());
            if (qt != null) {
                quota.setVumUsed(qt.getVumUsed());
            }
        }
        quotaMapper.deleteByPrimaryKey(quota.getId());
        if (StringUtils.isBlank(quota.getId())) {
            quota.setId(UUID.randomUUID().toString());
        }

        this.checkQuotaSaveParam(quota);

        BigDecimal vumTotal = quota.getVumTotal();
        BigDecimal max = BigDecimal.valueOf(99999999.00);
        if (vumTotal != null && vumTotal.compareTo(max) > 0) {
            MSException.throwException("总vum数量不能超过99999999！");
        }
        quotaMapper.insert(quota);
    }

    private void checkQuotaSaveParam(Quota quota) {
        if (StringUtils.isNotBlank(quota.getWorkspaceId()) && StringUtils.isNotBlank(quota.getProjectId())) {
            MSException.throwException("illegal parameter, workspace id and project id cannot exist at the same time");
        } else if (StringUtils.isNotBlank(quota.getWorkspaceId()) && StringUtils.isBlank(quota.getProjectId())) {
            QuotaExample quotaExample = new QuotaExample();
            quotaExample.createCriteria().andWorkspaceIdEqualTo(quota.getWorkspaceId())
                    .andProjectIdIsNull();
            if (quotaMapper.countByExample(quotaExample) > 0) {
                MSException.throwException("repeat insert workspace quota, id is: " + quota.getWorkspaceId());
            }
        } else if (StringUtils.isNotBlank(quota.getProjectId()) && StringUtils.isBlank(quota.getWorkspaceId())) {
            QuotaExample quotaExample = new QuotaExample();
            quotaExample.createCriteria().andProjectIdEqualTo(quota.getProjectId())
                    .andWorkspaceIdIsNull();
            if (quotaMapper.countByExample(quotaExample) > 0) {
                MSException.throwException("repeat insert project quota, id is: " + quota.getProjectId());
            }
        } else if (this.isDefaultQuota(quota)) {
            QuotaExample quotaExample = new QuotaExample();
            quotaExample.createCriteria().andIdEqualTo(quota.getId());
            if (quotaMapper.countByExample(quotaExample) > 0) {
                MSException.throwException("repeat insert default quota, id is: " + quota.getId());
            }
        }
    }

    public void wsVumCompare(Quota quota) {
        if (quota == null) {
            return;
        }
        if (StringUtils.isNotBlank(quota.getWorkspaceId())) {
            // 工作空间消耗的vum数量
            Quota vumUsedSum = extQuotaMapper.getProjectQuotaSum(quota.getWorkspaceId());
            if (vumUsedSum != null && vumUsedSum.getVumUsed() != null) {
                vumCompare(quota.getVumTotal(), vumUsedSum.getVumUsed());
            }
        }
    }

    public void vumCompare(BigDecimal vumTotal, BigDecimal vumUsed) {
        if (isValid(vumTotal) && isValid(vumUsed)) {
            if (vumUsed.compareTo(vumTotal) > 0) {
                MSException.throwException(Translator.get("quota_vum_used_gt_vum_total"));
            }
        }
    }

    public List<QuotaResult> listWorkspaceQuota(String name) {
        return extQuotaMapper.listWorkspaceQuota(name);
    }

    public List<QuotaResult> listProjectQuota(String workspaceId, String name) {
        return extQuotaMapper.listProjectQuota(workspaceId, name);
    }

    public void deleteQuota(String id) {
        Quota quota = quotaMapper.selectByPrimaryKey(id);
        // 保留vum使用数量
        Quota qt = new Quota();
        qt.setId(UUID.randomUUID().toString());
        qt.setVumUsed(quota.getVumUsed());
        qt.setWorkspaceId(quota.getWorkspaceId());
        qt.setProjectId(quota.getProjectId());
        qt.setUseDefault(false);
        qt.setUpdateTime(System.currentTimeMillis());
        quotaMapper.insert(qt);
        quotaMapper.deleteByPrimaryKey(id);
    }

    public void checkDefaultQuota(Quota quota) {
        if (StringUtils.equals(quota.getId(), QuotaConstants.DefaultType.workspace.name())) {
            List<Quota> wsQuotas = extQuotaMapper.listUseDefaultWsQuota();
            for (Quota q : wsQuotas) {
                useDefaultQuota(q, quota);
                wsVumCompare(q);
                checkWorkspaceQuota(q);
            }
        }

        if (StringUtils.startsWith(quota.getId(), QuotaConstants.prefix)) {
            String workspaceId = quota.getId().substring(QuotaConstants.prefix.length());
            Quota wsQuota = getWorkspaceQuota(workspaceId);
            checkQuota(quota, wsQuota);
            List<Quota> pjQuotas = extQuotaMapper.listUseDefaultProjectQuota(workspaceId);
            for (Quota q : pjQuotas) {
                useDefaultQuota(q, quota);
                vumCompare(q.getVumTotal(), q.getVumUsed());
                checkProjectQuota(q);
            }
        }
    }


    private boolean isDefaultQuota(Quota quota) {
        return StringUtils.startsWith(quota.getId(), QuotaConstants.prefix)
                || StringUtils.equals(quota.getId(), QuotaConstants.DefaultType.workspace.name());
    }

    private void checkProjectQuota(Quota quota) {
        // 获取工作空间下所有项目配额总和(接口和性能测试数量)
        Quota sumQuota = getProjectSumQuota(quota.getWorkspaceId(), quota.getProjectId());
        if (quota.getApi() != null) {
            sumQuota.setApi(sumQuota.getApi() + quota.getApi());
        }
        if (quota.getPerformance() != null) {
            sumQuota.setPerformance(sumQuota.getPerformance() + quota.getPerformance());
        }
        if (quota.getVumTotal() != null) {
            sumQuota.setVumTotal(sumQuota.getVumTotal().add(quota.getVumTotal()));
        }
        sumQuota.setMaxThreads(quota.getMaxThreads());
        sumQuota.setDuration(quota.getDuration());
        sumQuota.setResourcePool(quota.getResourcePool());

        // 工作空间配额
        Quota wsQuota = getWorkspaceQuota(quota.getWorkspaceId());

        // 检查是否超过工作空间配额
        checkQuota(sumQuota, wsQuota);
    }

    private void checkWorkspaceQuota(Quota quota) {
        // 获取工作空间下所有项目配额总和(接口和性能测试数量)
        Quota sumQuota = getProjectSumQuota(quota.getWorkspaceId(), null);
        sumQuota.setResourcePool(quota.getResourcePool());

        // 检查是否超过工作空间配额
        checkQuota(sumQuota, quota);
    }

    private void checkQuota(Quota sumQuota, Quota wsQuota) {
        if (wsQuota == null) {
            return;
        }

        if (isValid(wsQuota.getApi()) && isValid(sumQuota.getApi())) {
            if (sumQuota.getApi() > wsQuota.getApi()) {
                MSException.throwException(Translator.get("quota_project_excess_ws_api"));
            }
        }

        if (isValid(wsQuota.getPerformance()) && isValid(sumQuota.getPerformance())) {
            if (sumQuota.getPerformance() > wsQuota.getPerformance()) {
                MSException.throwException(Translator.get("quota_project_excess_ws_performance"));
            }
        }

        if (isValid(wsQuota.getMaxThreads()) && isValid(sumQuota.getMaxThreads())) {
            if (sumQuota.getMaxThreads() > wsQuota.getMaxThreads()) {
                MSException.throwException(Translator.get("quota_project_excess_ws_max_threads"));
            }
        }

        if (isValid(wsQuota.getDuration()) && isValid(sumQuota.getDuration())) {
            if (sumQuota.getDuration() > wsQuota.getDuration()) {
                MSException.throwException(Translator.get("quota_project_excess_ws_max_duration"));
            }
        }

        if (isValid(wsQuota.getVumTotal()) && isValid(sumQuota.getVumTotal())) {
            if (sumQuota.getVumTotal().compareTo(wsQuota.getVumTotal()) > 0) {
                MSException.throwException(Translator.get("quota_project_excess_ws_vum_total"));
            }
        }

        if (isValid(wsQuota.getResourcePool()) && isValid(sumQuota.getResourcePool())) {
            for (String id : sumQuota.getResourcePool().split(",")) {
                if (!wsQuota.getResourcePool().contains(id)) {
                    MSException.throwException(Translator.get("quota_project_excess_ws_resource_pool"));
                }
            }
        }
    }

    private Quota getProjectSumQuota(String workspaceId, String currentProjectId) {
        List<QuotaResult> quotaResults = listProjectQuota(workspaceId, null);
        Quota projectDefaultQuota = this.getProjectDefaultQuota(workspaceId);
        AtomicInteger api = new AtomicInteger();
        AtomicInteger performance = new AtomicInteger();
        AtomicReference<BigDecimal> vumTotal = new AtomicReference<>(new BigDecimal("0.00"));
        int thread = 0;
        int duration = 0;
        for (QuotaResult quotaResult : quotaResults) {
            if (BooleanUtils.isTrue(quotaResult.getUseDefault())) {
                useDefaultQuota(quotaResult, projectDefaultQuota);
            }
            // 不计算当前project
            if (StringUtils.isNotBlank(currentProjectId) && StringUtils.equals(currentProjectId, quotaResult.getProjectId())) {
                continue;
            }
            if (quotaResult.getApi() != null) {
                api.addAndGet(quotaResult.getApi());
            }
            if (quotaResult.getPerformance() != null) {
                performance.addAndGet(quotaResult.getPerformance());
            }
            if (quotaResult.getVumTotal() != null) {
                vumTotal.updateAndGet(v -> v.add(quotaResult.getVumTotal()));
            }
            if (quotaResult.getMaxThreads() != null) {
                if (quotaResult.getMaxThreads() > thread) {
                    thread = quotaResult.getMaxThreads();
                }
            }
            if (quotaResult.getDuration() != null) {
                if (quotaResult.getDuration() > duration) {
                    duration = quotaResult.getDuration();
                }
            }
        }
        Quota quota = new Quota();
        quota.setApi(api.get());
        quota.setPerformance(performance.get());
        quota.setVumTotal(vumTotal.get());
        quota.setMaxThreads(thread);
        quota.setDuration(duration);
        return quota;
    }

    public boolean isValid(Integer value) {
        return value != null && value > 0;
    }

    public boolean isValid(String value) {
        return StringUtils.isNotBlank(value);
    }

    public boolean isValid(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) != 0;
    }

    private void useDefaultQuota(Quota quota, Quota defaultQuota) {
        quota.setApi(defaultQuota.getApi());
        quota.setPerformance(defaultQuota.getPerformance());
        quota.setMaxThreads(defaultQuota.getMaxThreads());
        quota.setDuration(defaultQuota.getDuration());
        quota.setResourcePool(defaultQuota.getResourcePool());
        quota.setVumTotal(defaultQuota.getVumTotal());
    }

    public Quota getWorkspaceQuota(String workspaceId) {
        Quota quota = extQuotaMapper.getWorkspaceQuota(workspaceId);
        if (quota != null && BooleanUtils.isTrue(quota.getUseDefault())) {
            return getDefaultQuota(QuotaConstants.DefaultType.workspace);
        }
        return quota;
    }

    public Quota getProjectQuota(String projectId) {
        Quota quota = extQuotaMapper.getProjectQuota(projectId);
        if (quota != null && BooleanUtils.isTrue(quota.getUseDefault())) {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            if (project == null || StringUtils.isBlank(project.getWorkspaceId())) {
                MSException.throwException("project is null or workspace_id of project is null");
            }
            return getProjectDefaultQuota(project.getWorkspaceId());
        }
        return quota;
    }

    public String getLogDetails(String id) {
        Quota pool = quotaMapper.selectByPrimaryKey(id);
        if (pool != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(pool, SystemReference.quotaColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(pool.getId()), null, "默认配额", null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
