package io.metersphere.api.tcp.server;

import io.metersphere.api.dto.TCPMockReturnDTO;
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
        try {
            is = s.getInputStream();
            os = s.getOutputStream();
            int len = is.read(b);
            String message = new String(b, 0, len);
            TCPMockReturnDTO returnDTO = this.getReturnMsg(message);

            if (StringUtils.isNotEmpty(returnDTO.getEncode())) {
                os.write(returnDTO.getReturnMessage().getBytes(returnDTO.getEncode()));
            } else {
                os.write(returnDTO.getReturnMessage().getBytes());
            }

        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            this.close();
        }
    }

    private TCPMockReturnDTO getReturnMsg(String message) {
        LogUtil.info("TCP-Mock start. port: " + this.port + "; Message:" + message);
        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        MockExpectConfigDTO matchMockExpectDTO = mockConfigService.matchTcpMockExpect(message, this.port);
        TCPMockReturnDTO returnDTO = new TCPMockReturnDTO();
        returnDTO.setReturnMessage(StringUtils.EMPTY);
        if (matchMockExpectDTO != null && matchMockExpectDTO.getMockExpectConfig() != null) {
            String response = matchMockExpectDTO.getMockExpectConfig().getResponse();
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
                    returnDTO.setReturnMessage(
                            mockApiUtils.getResultByResponseResult(matchMockExpectDTO.getProjectId(), respResultObj.optJSONObject("body"), StringUtils.EMPTY, null, requestMockParams, useScript)
                    );
                }
                if (respResultObj.has("rsp_encode")) {
                    String encode = respResultObj.getString("rsp_encode");
                    if (StringUtils.isNotBlank(encode)) {
                        returnDTO.setEncode(encode);
                    }
                }
                try {
                    if (respResultObj.has("delayed")) {
                        delayed = respResultObj.getInt("delayed");
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            } else {
                returnDTO.setReturnMessage(responseObj.optString("body"));
            }

            try {
                Thread.sleep(delayed);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        LogUtil.info("TCP-Mock start. port: " + this.port + "; Message:" + message + "; response:" + returnDTO.getReturnMessage());
        return returnDTO;
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
