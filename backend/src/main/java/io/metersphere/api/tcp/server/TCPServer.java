package io.metersphere.api.tcp.server;

import io.metersphere.commons.utils.LogUtil;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author song.tianyang
 * @Date 2021/8/11 10:35 上午
 */
public class TCPServer implements Runnable {
    private int port;
    private ServerSocket serverSocket;

    private TCPServicer servicer;

    public TCPServer(int port){
        this.port = port;
    }

    public void openSocket() throws Exception {
        this.serverSocket = new ServerSocket(this.port);

        while (true) {
            if (!this.serverSocket.isClosed()) {
                Socket socket = this.serverSocket.accept();
                servicer = new TCPServicer(socket,port);
                servicer.run();
            }
            if (this.serverSocket.isClosed()) {
                break;
            }
        }
    }

    public boolean  isSocketOpen(){
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            return true;
        }else {
            return false;
        }
    }

    public void closeSocket() throws Exception {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            if(servicer != null){
                servicer.close();
            }
            this.serverSocket.close();
        }
    }

    @Override
    public void run() {
        try {
            this.openSocket();
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
