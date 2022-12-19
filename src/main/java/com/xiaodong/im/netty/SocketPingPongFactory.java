package com.xiaodong.im.netty;

public class SocketPingPongFactory {

    public SocketPingPongFactory(){
    }

    public String getPingMsg(){
        return "~#HHHBBB#~";
    }

    public String getPongMsg(){
        return "HB";
    }
}
