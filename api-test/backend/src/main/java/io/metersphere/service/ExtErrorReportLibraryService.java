package io.metersphere.service;


import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.base.domain.ErrorReportLibraryExample;
import io.metersphere.base.domain.ErrorReportLibraryWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ErrorReportLibraryUtil;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.xpack.fake.error.ErrorReportLibraryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2022/1/3 3:51 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ExtErrorReportLibraryService {
    public List<MsAssertions> getAssertionByProjectIdAndStatusIsOpen(String projectId, boolean higherThanSuccess, boolean higherThanError) {
        ErrorReportLibraryService service = CommonBeanFactory.getBean(ErrorReportLibraryService.class);
        List<MsAssertions> returnList = new ArrayList<>();
        ErrorReportLibraryExample example = new ErrorReportLibraryExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andStatusEqualTo(true);
        List<ErrorReportLibraryWithBLOBs> bloBs = service.selectByExampleWithBLOBs(example);
        bloBs.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getContent())) {
                try {
                    MsAssertions assertions = JSONUtil.parseObject(item.getContent(), MsAssertions.class);
                    if (assertions != null && CollectionUtils.isNotEmpty(assertions.getRegex())) {
                        if (StringUtils.isEmpty(assertions.getRegex().get(0).getDescription())) {
                            String desc = assertions.getRegex().get(0).getSubject() + ":" + assertions.getRegex().get(0).getExpression()
                                    + ErrorReportLibraryUtil.ASSERTION_CONTENT_REGEX_DELIMITER + higherThanSuccess
                                    + ErrorReportLibraryUtil.ASSERTION_CONTENT_REGEX_DELIMITER + higherThanError;
                            assertions.getRegex().get(0).setDescription(desc);
                        }
                        assertions.setName("ErrorReportAssertion:" + item.getErrorCode());
                        returnList.add(assertions);
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        });
        return returnList;
    }
}
