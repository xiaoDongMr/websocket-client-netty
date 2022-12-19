package com.xiaodong.im.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 美团服务端通道
 */
@Slf4j
public class SeviceChannelPool {
    private static ConcurrentHashMap<String, ClientChannelAttribute> mtWebSocketClients = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, ClientChannelAttribute> getMtWebSocketClients(){
        return mtWebSocketClients;
    }

    public static ClientChannelAttribute getWebSocketClient(String clientKey) {
        return mtWebSocketClients.get(clientKey);
    }

    public static void setWebSocketClient(String clientKey, ClientChannelAttribute webSocketClient) {
        mtWebSocketClients.put(clientKey, webSocketClient);
    }

    public static boolean containsWebSocketClientKey(String clientKey){
        return mtWebSocketClients.containsKey(clientKey);
    }

    public static ClientChannelAttribute removeWebSocketClient(String clientKey){
        return mtWebSocketClients.remove(clientKey);
    }

    public static void closeChannel(String clientKey){
        if(containsWebSocketClientKey(clientKey)){
            log.info("关闭通道连接 {}", clientKey);
            getWebSocketClient(clientKey).getFuture().channel().close();
            removeWebSocketClient(clientKey);
        }
    }

    public static void reconnect(String clientKey){
        if(containsWebSocketClientKey(clientKey)){
            log.info("重新连接通道 {}", clientKey);
            ClientChannelAttribute webSocketClient = getWebSocketClient(clientKey);
            WebSocketClient client = new WebSocketClient(webSocketClient);
            client.connect();
        }
    }

    public static void sendMsg(ChannelFuture future, String msg) throws InterruptedException {
        if (future != null && future.channel().isActive()) {
            Channel channel = future.channel();
            WebSocketFrame webSocketMsg = new TextWebSocketFrame(msg);
            channel.writeAndFlush(webSocketMsg).sync();
        } else {
            log.error("22-06-1001","消息发送失败,连接尚未建立!");
        }
    }


    public static String getClientKey(String platform, Long groupId){
        return platform.concat("-").concat(String.valueOf(groupId));
    }
}
