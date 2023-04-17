package io.metersphere.api.tcp.server;

import io.metersphere.api.dto.mock.MockExpectConfigDTO;
import io.metersphere.api.dto.mock.RequestMockParams;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.mock.MockApiUtils;
import io.metersphere.service.MockConfigService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPService {
    private Socket s;
    private InputStream is;
    private OutputStream os;
    private int port;

    public TCPService(Socket s, int port) {
        this.s = s;
        this.port = port;
    }

    public void run() {
        byte[] b = new byte[1024];
        String returnMsg = StringUtils.EMPTY;
        String message = StringUtils.EMPTY;
        try {
            is = s.getInputStream();
            os = s.getOutputStream();
            int len = is.read(b);
            message = new String(b, 0, len);
            returnMsg = this.getReturnMsg(message);
            os.write(returnMsg.getBytes());
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            this.close();
        }
    }

    private String getReturnMsg(String message) {
        LogUtil.info("TCP-Mock start. port: " + this.port + "; Message:" + message);
        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        MockExpectConfigDTO matchdMockExpectDTO = mockConfigService.matchTcpMockExpect(message, this.port);
        String returnMsg = StringUtils.EMPTY;
        if (matchdMockExpectDTO != null && matchdMockExpectDTO.getMockExpectConfig() != null) {
            String response = matchdMockExpectDTO.getMockExpectConfig().getResponse();
            JSONObject responseObj = JSONUtil.parseObject(response);
            int delayed = 0;
            try {
                if (responseObj.has("delayed")) {
                    delayed = responseObj.getInt("delayed");
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
            if (responseObj.has("responseResult")) {
                JSONObject respResultObj = responseObj.optJSONObject("responseResult");
                if (respResultObj.has("body")) {
                    MockApiUtils mockApiUtils = new MockApiUtils();
                    boolean useScript = false;
                    if (respResultObj.has("usePostScript")) {
                        useScript = respResultObj.getBoolean("usePostScript");
                    }
                    RequestMockParams requestMockParams = new RequestMockParams();
                    requestMockParams.setTcpParam(message);
                    returnMsg = mockApiUtils.getResultByResponseResult(matchdMockExpectDTO.getProjectId(), respResultObj.optJSONObject("body"), StringUtils.EMPTY, null, requestMockParams, useScript);
                }
                try {
                    if (respResultObj.has("delayed")) {
                        delayed = respResultObj.getInt("delayed");
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            } else {
                returnMsg = responseObj.optString("body");
            }

            try {
                Thread.sleep(delayed);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        LogUtil.info("TCP-Mock start. port: " + this.port + "; Message:" + message + "; response:" + returnMsg);
        return returnMsg;
    }

    public void close() {
        //关闭资源
        try {
            is.close();
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                LogUtil.error(e);
            } finally {
                try {
                    s.close();
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        }
    }

}
