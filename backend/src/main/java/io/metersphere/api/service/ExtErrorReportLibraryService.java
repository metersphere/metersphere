package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.base.domain.ErrorReportLibraryExample;
import io.metersphere.base.domain.ErrorReportLibraryWithBLOBs;
import io.metersphere.base.mapper.ErrorReportLibraryMapper;
import io.metersphere.commons.utils.ErrorReportLibraryUtil;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2022/1/3 3:51 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ExtErrorReportLibraryService {
    @Resource
    private ErrorReportLibraryMapper errorReportLibraryMapper;

    public List<MsAssertions> getAssertionByProjectIdAndStatusIsOpen(String projectId, boolean higherThanSuccess, boolean higherThanError) {
        List<MsAssertions> returnList = new ArrayList<>();
        ErrorReportLibraryExample example = new ErrorReportLibraryExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andStatusEqualTo(true);
        List<ErrorReportLibraryWithBLOBs> bloBs = errorReportLibraryMapper.selectByExampleWithBLOBs(example);
        bloBs.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getContent())) {
                try {
                    MsAssertions assertions = JSONObject.parseObject(item.getContent(), MsAssertions.class);
                    if (assertions != null && CollectionUtils.isNotEmpty(assertions.getRegex())) {
                        if (StringUtils.isEmpty(assertions.getRegex().get(0).getDescription())) {
                            String desc = assertions.getRegex().get(0).getSubject()+":"+assertions.getRegex().get(0).getExpression()
                                    + ErrorReportLibraryUtil.ASSERTION_CONTENT_REGEX_DELIMITER + higherThanSuccess
                                    + ErrorReportLibraryUtil.ASSERTION_CONTENT_REGEX_DELIMITER + higherThanError;
                            assertions.getRegex().get(0).setDescription(desc);
                        }
                        assertions.setName("ErrorReportAssertion:"+item.getErrorCode());
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
