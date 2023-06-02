package io.metersphere.bug.service;

import io.metersphere.bug.controller.result.BugMgtResultCode;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugExample;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.sdk.exception.MSException;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jianxing
 * @date : 2023-5-17
 */
@Service
@Transactional
public class BugService {

    @Resource
    private BugMapper bugMapper;


    public List<Bug> list() {
        return new ArrayList<>();
    }

    public Bug get(String id) {
        return bugMapper.selectByPrimaryKey(id);
    }

    public Bug add(Bug bug) {
        BugExample example = new BugExample();
        example.createCriteria().andTitleEqualTo(bug.getTitle());
        example.createCriteria().andProjectIdEqualTo(bug.getProjectId());
        List<Bug> bugs = bugMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(bugs)) {
            MSException.throwException(BugMgtResultCode.BUG_EXIST_EXCEPTION);
        }
        bug.setCreateTime(System.currentTimeMillis());
        bug.setUpdateTime(System.currentTimeMillis());
        bugMapper.insert(bug);
        return bug;
    }

    public Bug update(Bug bug) {
        bugMapper.updateByPrimaryKeySelective(bug);
        return bug;
    }

    public int delete(String id) {
        return bugMapper.deleteByPrimaryKey(id);
    }
}
