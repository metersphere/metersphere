package io.metersphere.service;

import io.metersphere.base.domain.Project;
import io.metersphere.commons.exception.MSException;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.function.Function;

public class TemplateBaseService {

    protected void checkTemplateUsed(String id, Function<String, List<Project>> getProjectFuc) {
        List<Project> projects = getProjectFuc.apply(id);
        if (CollectionUtils.isNotEmpty(projects)) {
            StringBuilder tip = new StringBuilder();
            projects.forEach(item -> {
                tip.append(item.getName() + ',');
            });
            tip.deleteCharAt(tip.length() - 1);
            MSException.throwException("该模板已关联项目：" + tip);
        }
    }
}
