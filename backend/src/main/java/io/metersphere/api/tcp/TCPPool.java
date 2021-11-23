package io.metersphere.api.tcp;

import io.metersphere.api.tcp.server.TCPServer;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/8/10 3:04 下午
 */
public class TCPPool {

    private  static HashMap<Integer, TCPServer> serverSockedMap = new HashMap<>();

    private TCPPool(){}

    public static String createTcp(int port){
        String returnString = "";
        if(port > 0){
            TCPServer tcpServer = null;
            if(serverSockedMap.containsKey(port)){
                tcpServer = serverSockedMap.get(port);
            }else {
                tcpServer = new TCPServer(port);
                serverSockedMap.put(port,tcpServer);
            }
            try {
                if(!tcpServer.isSocketOpen()){
                    Thread t = new Thread(tcpServer);
                    t.start();
                }
                returnString = "OK";
            }catch (Exception e){
                returnString = e.getMessage();
                LogUtil.error(e);
                MSException.throwException(e.getMessage());
            }
        }

        return returnString;
    }

    public static boolean isTcpOpen(int port){
        TCPServer server = serverSockedMap.get(port);
        if(server != null ){
            return  server.isSocketOpen();
        }
        return  false;
    }

    public static String getTcpStatus() {
        if(serverSockedMap.isEmpty()){
            return "null";
        }else {
            StringBuffer stringBuffer = new StringBuffer();
            for (Map.Entry<Integer, TCPServer> entry:serverSockedMap.entrySet()) {
                int port = entry.getKey();
                TCPServer tcpServer = entry.getValue();
                if(tcpServer == null){
                    stringBuffer.append("Port is "+port + ";");
                    stringBuffer.append("Server is null;");
                }else {
                    stringBuffer.append("Port is "+port + ";");
                    stringBuffer.append("Server is open: "+ tcpServer.isSocketOpen()+";");
                }
            }
            return stringBuffer.toString();
        }
    }

    public static String closeTcp(int portNum) {
        TCPServer server = serverSockedMap.get(portNum);
        if(server == null){
            return "Tcp Is not create!";
        }else {
            String returnMsg = null;
            try {
                server.closeSocket();
                returnMsg = "OK";
            }catch (Exception e){
                returnMsg = e.getMessage();
                LogUtil.error(e);
            }
            return returnMsg;
        }
    }
}
