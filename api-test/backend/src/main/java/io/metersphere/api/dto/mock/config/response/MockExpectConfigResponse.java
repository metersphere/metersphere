package io.metersphere.api.dto.mock.config.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.JSONUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/4/13 4:59 下午
 * @Description
 */
@Getter
@Setter
public class MockExpectConfigResponse {

    private String id;

    private String expectNum;

    private String mockConfigId;

    private String name;

    private List<String> tags;

    private boolean status;

    private Long createTime;

    private Long updateTime;

    private String createUserId;

    private ObjectNode request;

    private ObjectNode response;

    public MockExpectConfigResponse(MockExpectConfigWithBLOBs expectConfig) {
        this.id = expectConfig.getId();
        this.mockConfigId = expectConfig.getMockConfigId();
        this.expectNum = expectConfig.getExpectNum();
        this.name = expectConfig.getName();
        this.status = Boolean.parseBoolean(expectConfig.getStatus());
        this.createTime = expectConfig.getCreateTime();
        this.updateTime = expectConfig.getUpdateTime();
        this.createUserId = expectConfig.getCreateUserId();
        this.request = JSONUtil.parseObjectNode(expectConfig.getRequest());
        this.response = JSONUtil.parseObjectNode(expectConfig.getResponse());

        if (expectConfig.getTags() != null) {
            try {
                tags = JSON.parseArray(expectConfig.getTags(), String.class);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
    }
}
