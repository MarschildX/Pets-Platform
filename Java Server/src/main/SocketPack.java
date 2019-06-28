package main;

import java.net.Socket;

public class SocketPack {
    private Socket socket;
    private String id;

    public SocketPack(Socket socket,String id){
        this.socket=socket;
        this.id=id;
    }

    public Socket getSocket(){
        return socket;
    }

    public String getId(){
        return id;
    }
}
