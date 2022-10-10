package io.metersphere.excel.converter;

import io.metersphere.base.domain.User;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.TestCaseDTO;
import io.metersphere.service.BaseUserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCaseExportCreatorConverter implements TestCaseExportConverter {

    private Map<String, String> userMap = new HashMap<>();

    public TestCaseExportCreatorConverter() {
        BaseUserService userService = CommonBeanFactory.getBean(BaseUserService.class);
        List<User> memberOption = userService.getProjectMemberOption(SessionUtils.getCurrentProjectId());
        memberOption.forEach(option -> userMap.put(option.getId(), option.getName()));
    }

    @Override
    public String parse(TestCaseDTO testCase) {
        return getFromMapOfNullable(userMap, testCase.getCreateUser());
    }
}
