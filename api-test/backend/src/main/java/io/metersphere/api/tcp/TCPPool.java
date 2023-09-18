package io.metersphere.api.tcp;

import io.metersphere.api.tcp.server.TCPServer;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;

import java.util.HashMap;

/**
 * @author song.tianyang
 * @Date 2021/8/10 3:04 下午
 */
public class TCPPool {

    private static final HashMap<Integer, TCPServer> serverSockedMap = new HashMap<>();

    private TCPPool() {
    }

    public static void createTcp(int port) {
        if (port > 0) {
            TCPServer tcpServer;
            if (serverSockedMap.containsKey(port)) {
                tcpServer = serverSockedMap.get(port);
            } else {
                tcpServer = new TCPServer(port);
                serverSockedMap.put(port, tcpServer);
            }
            try {
                if (!tcpServer.isSocketOpen()) {
                    Thread t = new Thread(tcpServer);
                    t.start();
                }
            } catch (Exception e) {
                LogUtil.error(e);
                MSException.throwException(e.getMessage());
            }
        }

    }

    public static boolean isTcpOpen(int port) {
        TCPServer server = serverSockedMap.get(port);
        if (server != null) {
            return server.isSocketOpen();
        }
        return false;
    }

    public static void closeTcp(int portNum) {
        TCPServer server = serverSockedMap.get(portNum);
        if (server != null) {
            try {
                server.closeSocket();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
