package io.metersphere.track.request.testplan;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/4/6 4:18 下午
 * @Description
 */
@Getter
@Setter
public class BatchCondition extends LoadCaseRequest{
    //是否针对的是全体数据
    boolean selectAll;
    //if selectAll is true， 那么要在结果中排除掉的id集合
    List<String> unSelectIds;
}
