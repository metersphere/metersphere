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
import org.apache.jmeter.protocol.tcp.sampler.ReadException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPService {
    private final Socket socket;
    private final int port;

    public TCPService(Socket socket, int port) {
        this.socket = socket;
        this.port = port;
    }

    public String read(InputStream is) throws ReadException {
        try (ByteArrayOutputStream w = new ByteArrayOutputStream()) {
            final int size = 1024;
            byte[] buffer = new byte[size];
            while (true) {
                try {
                    int x = is.read(buffer);
                    if (x < size) {
                        w.write(buffer, 0, x);
                        break;
                    }
                    w.write(buffer, 0, x);
                } catch (IOException e) {
                    break;
                }
            }
            return w.toString(StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new ReadException("Error decoding bytes from server with " + StandardCharsets.UTF_8 + ", bytes read: ",
                    e, "<Read bytes with bad encoding>");
        } catch (IOException e) {
            throw new ReadException("Error reading from server, bytes read: ", e, StringUtils.EMPTY);
        }
    }

    public void run() throws IOException {
        try (InputStream is = socket.getInputStream();
             OutputStream os = socket.getOutputStream()) {
            String message = this.read(is);
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

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
}
