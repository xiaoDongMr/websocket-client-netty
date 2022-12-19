package com.xiaodong.im.service;

import com.xiaodong.im.netty.ClientChannelAttribute;
import com.xiaodong.im.netty.SeviceChannelPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BizSendAndReceiveTest implements IBizSendAndReceive{
    @Override
    public void receiveMsg(String paramString, String clientKey) {
        log.info("接收到消息，执行业务");

    }

    @Override
    public void sendMsg(String content) {
        log.info("发送消息");
        ClientChannelAttribute channelAttribute = SeviceChannelPool.getWebSocketClient("xiaodong");
        try {
            SeviceChannelPool.sendMsg(channelAttribute.getFuture(), content);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
