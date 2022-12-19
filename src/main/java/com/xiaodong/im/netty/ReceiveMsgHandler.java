package com.xiaodong.im.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class ReceiveMsgHandler extends SimpleChannelInboundHandler<String> {

    private final ClientChannelAttribute clientChannelAttribute;

    public ReceiveMsgHandler(ClientChannelAttribute clientChannelAttribute) {
        this.clientChannelAttribute = clientChannelAttribute;
    }


    /*** 客户端读取到网络数据后的处理*/
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if(clientChannelAttribute.getBizSendAndReceive()!=null){
            // IO线程复用，需要清理下线程局部变量
            clientChannelAttribute.getBizSendAndReceive().receiveMsg(msg, clientChannelAttribute.getClientKey());
        } else {
            log.info("接收到数据{}，无业务处理", msg);
        }
    }

    /*** 发生异常后的处理*/
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        log.error("发生异常");
    }

}