package io.metersphere.log.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.OperatingLog;
import io.metersphere.base.mapper.OperatingLogMapper;
import io.metersphere.base.mapper.ext.ExtOperatingLogMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.log.vo.OperatingLogDTO;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.OperatingLogRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperatingLogService {
    @Resource
    private OperatingLogMapper operatingLogMapper;
    @Resource
    private ExtOperatingLogMapper extOperatingLogMapper;

    public void create(OperatingLog log) {
        operatingLogMapper.insert(log);
    }

    public List<OperatingLogDTO> list(OperatingLogRequest request) {
        if (CollectionUtils.isNotEmpty(request.getTimes())) {
            request.setStartTime(request.getTimes().get(0));
            request.setEndTime(request.getTimes().get(1));
        }
        return extOperatingLogMapper.list(request);
    }

    public OperatingLogDTO get(String id) {
        OperatingLog log = operatingLogMapper.selectByPrimaryKey(id);
        OperatingLogDTO dto = new OperatingLogDTO();
        BeanUtils.copyBean(dto, log);
        if (StringUtils.isNotEmpty(log.getOperContent())) {
            dto.setDetails(JSON.parseObject(log.getOperContent(), OperatingLogDetails.class));
        }
        return dto;
    }
}
