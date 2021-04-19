package io.metersphere.api.dto.mockconfig.response;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
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

    private String mockConfigId;

    private String name;

    private List<String> tags;

    private boolean status;

    private Long createTime;

    private Long updateTime;

    private String createUserId;

    private JSONObject request;

    private JSONObject response;

    public MockExpectConfigResponse(MockExpectConfigWithBLOBs expectConfig) {
        this.id = expectConfig.getId();
        this.mockConfigId = expectConfig.getMockConfigId();
        this.name = expectConfig.getName();
        this.status = Boolean.parseBoolean(expectConfig.getStatus());
        this.createTime = expectConfig.getCreateTime();
        this.updateTime = expectConfig.getUpdateTime();
        this.createUserId = expectConfig.getCreateUserId();
        this.request = JSONObject.parseObject(expectConfig.getRequest());
        this.response = JSONObject.parseObject(expectConfig.getResponse());

        if (expectConfig.getTags() != null) {
            try {
                tags = JSONArray.parseArray(expectConfig.getTags(), String.class);
            } catch (Exception e) {

            }
        }
    }
}
