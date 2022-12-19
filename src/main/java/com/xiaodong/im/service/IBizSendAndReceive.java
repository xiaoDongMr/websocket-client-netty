package com.xiaodong.im.service;


public interface IBizSendAndReceive {

    /**
     * 接收消息
     * @param paramString
     */
    void receiveMsg(String paramString, String clientKey);

    /**
     * 发送消息
     * @param content
     */
    void sendMsg(String content);
}
