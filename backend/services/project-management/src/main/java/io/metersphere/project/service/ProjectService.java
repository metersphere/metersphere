package io.metersphere.project.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService extends ServiceImpl<ProjectMapper, Project> {

}
