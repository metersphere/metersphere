package io.metersphere.system.notice.sender.impl;

import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkrobot_1_0.Client;
import com.aliyun.dingtalkrobot_1_0.models.OrgGroupSendHeaders;
import com.aliyun.dingtalkrobot_1_0.models.OrgGroupSendRequest;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.Receiver;
import io.metersphere.system.notice.sender.AbstractNoticeSender;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DingEnterPriseNoticeSender extends AbstractNoticeSender {

    public void sendDing(MessageDetail messageDetail, String context) throws Exception {
        Client client = DingEnterPriseNoticeSender.createClient();
        GetAccessTokenResponse accessToken = getAccessToken(messageDetail.getAppKey(), messageDetail.getAppSecret());
        OrgGroupSendHeaders orgGroupSendHeaders = new OrgGroupSendHeaders();
        orgGroupSendHeaders.xAcsDingtalkAccessToken = accessToken.getBody().accessToken;
        String tokenIndex = getTokenIndex(messageDetail.getWebhook());
        OrgGroupSendRequest orgGroupSendRequest = new OrgGroupSendRequest()
                .setMsgParam("{\"content\":\""+context+"\"}")
                .setMsgKey("sampleText")
                .setToken(tokenIndex);
        try {
            client.orgGroupSendWithOptions(orgGroupSendRequest, orgGroupSendHeaders, new RuntimeOptions());
        } catch (TeaException err) {
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                LogUtils.error(err.message);
            }

        } catch (Exception error) {
            TeaException err = new TeaException(error.getMessage(), error);
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                LogUtils.error(err.message);
            }

        }
    }

    /**
     * 使用 Token 初始化账号Client
     * @return Client
     * @throws Exception
     */
    private static Client createClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new Client(config);
    }

    private static com.aliyun.dingtalkoauth2_1_0.Client createAuthClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkoauth2_1_0.Client(config);
    }




    private String getTokenIndex(String webhook) {
        int tokenIndex = webhook.indexOf("=");
        return webhook.substring(tokenIndex+ 1);
    }

    /**
     * 通过机器人标识获取企业内部机器人的accessToken
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    private GetAccessTokenResponse getAccessToken(String appKey, String appSecret) throws Exception {
        com.aliyun.dingtalkoauth2_1_0.Client authClient = DingEnterPriseNoticeSender.createAuthClient();

        com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest getAccessTokenRequest = new com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest()
                .setAppKey(appKey)
                .setAppSecret(appSecret);

        return  authClient.getAccessToken(getAccessTokenRequest);
    }

    @Override
    public void send(MessageDetail messageDetail, NoticeModel noticeModel) {
        String context = super.getContext(messageDetail, noticeModel);
        List<Receiver> receivers = super.getReceivers(noticeModel.getReceivers(), noticeModel.isExcludeSelf(), noticeModel.getOperator());
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }
        List<String> userIds = receivers.stream()
                .map(Receiver::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<User> users = super.getUsers(userIds, messageDetail.getProjectId(), true);
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        try {
            sendDing(messageDetail, context);
            LogUtils.debug("发送钉钉内部机器人结束");
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }
}
