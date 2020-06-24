package io.metersphere.service;

import io.metersphere.base.domain.Schedule;
import io.metersphere.base.domain.ScheduleExample;
import io.metersphere.base.mapper.ScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleService {
    
    @Resource
    private ScheduleMapper scheduleMapper;

    public void addSchedule(Schedule schedule) {
        schedule.setId(UUID.randomUUID().toString());
        scheduleMapper.insert(schedule);
    }

    public Schedule getSchedule(String ScheduleId) {
        return scheduleMapper.selectByPrimaryKey(ScheduleId);
    }

    public int editSchedule(Schedule schedule) {
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    public Schedule getScheduleByResource(String resourceId, String group) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(resourceId).andGroupEqualTo(group);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (schedules.size() > 0) {
            return schedules.get(0);
        }
        return null;
    }

    public int deleteSchedule(String scheduleId) {
        return scheduleMapper.deleteByPrimaryKey(scheduleId);
    }

    public List<Schedule> listSchedule() {
        ScheduleExample example = new ScheduleExample();
        return scheduleMapper.selectByExample(example);
    }

    public List<Schedule> getEnableSchedule() {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andEnableEqualTo(true);
        return scheduleMapper.selectByExample(example);
    }
}
