package io.metersphere.track.request.testreview;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/4/7 2:38 下午
 * @Description
 */
@Getter
@Setter
public class QueryCaseReviewCondition extends QueryCaseReviewRequest {
    private boolean selectAll;
    private List<String> unSelectIds;
}
