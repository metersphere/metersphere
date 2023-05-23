package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import jakarta.annotation.Resource;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService {
    @Resource
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    public List<Project> list() {
        jdbcAggregateTemplate.findAll(Project.class);
        return null;
    }
}
