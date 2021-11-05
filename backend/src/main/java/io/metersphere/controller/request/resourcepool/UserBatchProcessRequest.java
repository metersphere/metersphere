package io.metersphere.controller.request.resourcepool;

import io.metersphere.controller.request.UserRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.locks.Condition;

/**
 * @author song.tianyang
 * @Date 2021/3/3 5:21 下午
 * @Description
 */
@Getter
@Setter
public class UserBatchProcessRequest {
    List<String> ids;
    String projectId;
    String batchType;
    List<String> batchProcessValue;
    String workspaceId;
    UserRequest condition;

    String selectUserGroupId;
}

