package io.metersphere.novice.service;

import io.metersphere.base.domain.NoviceStatistics;
import io.metersphere.base.domain.NoviceStatisticsExample;
import io.metersphere.base.mapper.NoviceStatisticsMapper;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.novice.request.NoviceRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author: LAN
 * @date: 2023/3/17 14:18
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class NoviceService {
    @Resource
    private NoviceStatisticsMapper noviceStatisticsMapper;

    public List<NoviceStatistics> getNoviceInfo() {
        NoviceStatisticsExample example = new NoviceStatisticsExample();
        example.createCriteria().andUserIdEqualTo(SessionUtils.getUserId());
        return noviceStatisticsMapper.selectByExample(example);
    }

    public void saveNoviceInfo(NoviceStatistics noviceStatistics) {
        List<NoviceStatistics> noviceInfo = getNoviceInfo();
        long systemTime = System.currentTimeMillis();
        if(noviceInfo != null && noviceInfo.size() > 0){
            NoviceStatistics record = noviceInfo.get(0);
            NoviceStatisticsExample example = new NoviceStatisticsExample();
            example.createCriteria().andUserIdEqualTo(SessionUtils.getUserId()).andIdEqualTo(record.getId());
            noviceStatistics.setUpdateTime(systemTime);
            noviceStatisticsMapper.updateByExampleSelective(noviceStatistics, example);
        }else{
            noviceStatistics.setId(UUID.randomUUID().toString());
            noviceStatistics.setUserId(SessionUtils.getUserId());
            noviceStatistics.setStatus(1);
            noviceStatistics.setCreateTime(systemTime);
            noviceStatistics.setUpdateTime(systemTime);
            noviceStatisticsMapper.insertSelective(noviceStatistics);
        }
    }

    public void saveStep(NoviceRequest noviceRequest){
        List<NoviceStatistics> noviceInfo = getNoviceInfo();
        long systemTime = System.currentTimeMillis();
        if(noviceInfo != null && noviceInfo.size() > 0){
            NoviceStatistics noviceStatistics = noviceInfo.get(0);
            noviceStatistics.setGuideNum(noviceStatistics.getGuideNum() + 1);
            noviceStatistics.setUpdateTime(systemTime);
            if(noviceStatistics.getGuideStep() == 0) {
                noviceStatistics.setGuideStep(noviceRequest.getGuideStep());
            }
            NoviceStatisticsExample example = new NoviceStatisticsExample();
            example.createCriteria().andUserIdEqualTo(SessionUtils.getUserId()).andIdEqualTo(noviceStatistics.getId());

            noviceStatisticsMapper.updateByExampleSelective(noviceStatistics, example);
        } else {
            NoviceStatistics noviceStatistics = new NoviceStatistics();
            noviceStatistics.setId(UUID.randomUUID().toString());
            noviceStatistics.setUserId(SessionUtils.getUserId());
            noviceStatistics.setGuideStep(noviceRequest.getGuideStep());
            noviceStatistics.setGuideNum(1);
            noviceStatistics.setStatus(1);
            noviceStatistics.setDataOption(noviceRequest.getDataOption());
            noviceStatistics.setCreateTime(systemTime);
            noviceStatistics.setUpdateTime(systemTime);

            noviceStatisticsMapper.insertSelective(noviceStatistics);
        }
    }

    public void updateStatus(NoviceRequest noviceRequest){
        List<NoviceStatistics> noviceInfo = getNoviceInfo();
        long systemTime = System.currentTimeMillis();
        if(noviceInfo != null && noviceInfo.size() > 0){
            NoviceStatistics noviceStatistics = noviceInfo.get(0);
            noviceStatistics.setStatus(noviceRequest.getStatus());
            noviceStatistics.setUpdateTime(systemTime);
            NoviceStatisticsExample example = new NoviceStatisticsExample();
            example.createCriteria().andUserIdEqualTo(SessionUtils.getUserId()).andIdEqualTo(noviceStatistics.getId());

            noviceStatisticsMapper.updateByExampleSelective(noviceStatistics, example);
        } else {
            NoviceStatistics noviceStatistics = new NoviceStatistics();
            noviceStatistics.setId(UUID.randomUUID().toString());
            noviceStatistics.setUserId(SessionUtils.getUserId());
            noviceStatistics.setGuideStep(1);
            noviceStatistics.setGuideNum(1);
            noviceStatistics.setStatus(1);
            noviceStatistics.setDataOption(noviceRequest.getDataOption());
            noviceStatistics.setCreateTime(systemTime);
            noviceStatistics.setUpdateTime(systemTime);

            noviceStatisticsMapper.insertSelective(noviceStatistics);
        }
    }
}


