package io.metersphere.log.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.OperatingLogWithBLOBs;
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
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperatingLogService {
    @Resource
    private OperatingLogMapper operatingLogMapper;
    @Resource
    private ExtOperatingLogMapper extOperatingLogMapper;

    public void create(OperatingLogWithBLOBs log) {
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
        OperatingLogWithBLOBs log = operatingLogMapper.selectByPrimaryKey(id);
        OperatingLogDTO dto = new OperatingLogDTO();
        BeanUtils.copyBean(dto, log);
        if (StringUtils.isNotEmpty(log.getOperContent())) {
            dto.setDetails(JSON.parseObject(log.getOperContent(), OperatingLogDetails.class));
        }
        return dto;
    }

    public List<OperatingLogDTO> findBySourceId(String id) {
        OperatingLogRequest request = new OperatingLogRequest();
        request.setSourceId("%" + id + "%");
        List<OperatingLogDTO> logWithBLOBs = extOperatingLogMapper.findBySourceId(request);
        List<OperatingLogDTO> dtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(logWithBLOBs)) {
            for (OperatingLogDTO logWithBLOB : logWithBLOBs) {
                if (StringUtils.isNotEmpty(logWithBLOB.getOperContent())) {
                    logWithBLOB.setDetails(JSON.parseObject(logWithBLOB.getOperContent(), OperatingLogDetails.class));
                }
                if (CollectionUtils.isEmpty(logWithBLOB.getDetails().getColumns())) {
                    dtos.add(logWithBLOB);
                }

            }
        }
        if (CollectionUtils.isNotEmpty(dtos)) {
            logWithBLOBs.removeAll(dtos);
        }
        return logWithBLOBs;
    }
}
