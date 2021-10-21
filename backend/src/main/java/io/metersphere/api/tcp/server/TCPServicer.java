package io.metersphere.api.tcp.server;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.mock.MockApiUtils;
import io.metersphere.api.service.MockConfigService;
import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPServicer {
    private Socket s;
    private InputStream is;
    private OutputStream os;
    private int port;
    public TCPServicer(Socket s, int port) {
        this.s = s;
        this.port = port;
    }

    public void run() {
        byte[] b = new byte[1024];
        String returnMsg = "";
        String message = "";
        try {
            is = s.getInputStream();
            os = s.getOutputStream();
            int len = is.read(b);
            message = new String(b,0,len);
            returnMsg = this.getReturnMsg(message);
            os.write(returnMsg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            this.close();
        }
    }

    private String getReturnMsg(String message) {
        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        MockExpectConfigWithBLOBs matchdMockExpect = mockConfigService.matchTcpMockExpect(message,this.port);
        String returnMsg = "";
        if(matchdMockExpect != null){
            String response = matchdMockExpect.getResponse();
            JSONObject responseObj = JSONObject.parseObject(response);
            try {
                int delayed = responseObj.getInteger("delayed");
                Thread.sleep(delayed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(responseObj.containsKey("responseResult")){
                JSONObject respResultObj = responseObj.getJSONObject("responseResult");
                if(respResultObj.containsKey("body")){
                    returnMsg = MockApiUtils.getResultByResponseResult(respResultObj.getJSONObject("body"),"",null,null);
                }
            }else {
                returnMsg = responseObj.getString("body");
            }
        }
        return returnMsg;
    }

    public void close() {
        //关闭资源
        try{
            is.close();
        }catch (Exception e){}finally {
            try{
                os.close();
            }catch (Exception e){}finally {
                try{
                    s.close();
                }catch (Exception e){}finally {

                }
            }
        }
    }

}