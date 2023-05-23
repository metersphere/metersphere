package io.metersphere.project.service;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectApplicationService {
    @Resource
    private JdbcAggregateTemplate jdbcAggregateTemplate;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    public ProjectApplication save(ProjectApplication application) {
        projectApplicationMapper.insert(application);
        return application;
    }

    public ProjectApplication update(ProjectApplication application) {
        projectApplicationMapper.update(application);
        return application;
    }

    public List<ProjectApplication> list(String projectId) {
        Query query = Query.query(Criteria.where("project_id").is(projectId));

        Iterable<ProjectApplication> all = jdbcAggregateTemplate.findAll(query, ProjectApplication.class);
        return IterableUtils.toList(all);
    }
}
