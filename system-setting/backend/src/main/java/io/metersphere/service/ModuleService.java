package io.metersphere.service;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.log.vo.OperatingLogDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedList;

@Service
@Transactional(rollbackFor = Exception.class)
public class ModuleService {
    private static final String PREFIX = "metersphere.module.";
    @Resource
    private SystemParameterMapper systemParameterMapper;

    public void updateModuleStatus(String key, String status) {
        SystemParameter record = new SystemParameter();
        record.setParamKey(PREFIX + key);
        record.setParamValue(status);
        record.setType("text");
        record.setSort(1);
        if (systemParameterMapper.updateByPrimaryKey(record) == 0) {
            systemParameterMapper.insert(record);
        }
    }

    public String getLogDetails(String key, String status) {
        if (StringUtils.isNotEmpty(key)) {
            switch (key) {
                case "performance":
                    key = "性能测试";
                    break;
                case "report":
                    key = "报表统计";
                    break;
                case "api":
                    key = "接口测试";
                    break;
                case "track":
                    key = "测试跟踪";
                    break;
                case "project":
                    key = "项目设置";
                    break;
                case "setting":
                    key = "系统设置";
                    break;
            }
        }

        if (status.equals("ENABLE")) {
            status = "开启";
        } else {
            status = "禁用";
        }
        OperatingLogDetails details = new OperatingLogDetails(null, null, "修改：" + key + " 状态为：" + status, null, new LinkedList<>());
        return JSON.toJSONString(details);

    }
}
