package com.xiaodong.im;

import com.xiaodong.im.netty.ClientChannelAttribute;
import com.xiaodong.im.netty.SeviceChannelPool;
import com.xiaodong.im.netty.WebSocketClient;
import com.xiaodong.im.service.BizSendAndReceiveTest;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class ConnectionSocketTest {

    /**
     * 连接websocket
     */
    public static void doConnectSocket() {
        try {
            ClientChannelAttribute channelAttribute = ClientChannelAttribute.builder()
                    .clientKey("liuxiaodong")
                    .url("wss://wpush.xiaodong.com/websocket/wo1230oRvHypGaYDGWKp67vij4cjUDg5_6-6X4xv3AmnIPs-g")
                    .bizSendAndReceive(new BizSendAndReceiveTest())
                    .build();
            WebSocketClient client = new WebSocketClient(channelAttribute);
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("创建长连接，添加会话失败 {}", e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch count = new CountDownLatch(2);
        doConnectSocket();
        count.await();
    }

}
