package com.xiaodong.im.netty;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    //负责和服务器进行握手
    private final WebSocketClientHandshaker handshaker;
    // 握手的结果
    private ChannelPromise handshakeFuture;
    private final String clientKey;
    private final SocketPingPongFactory pingPongFactory;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, SocketPingPongFactory pingPongFactory, String clientKey) {
        this.handshaker = handshaker;
        this.pingPongFactory = pingPongFactory;
        this.clientKey = clientKey;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    //当前Handler被添加到ChannelPipeline时，
    // new出握手的结果的实例，以备将来使用
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    //通道建立，进行握手
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    //通道关闭
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("websocket通道关闭 {}", clientKey);
        SeviceChannelPool.reconnect(clientKey);
    }

    //读取数据
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        //握手未完成，完成握手
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                log.info("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                log.info("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        //处理websocket报文
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            String paramString = textFrame.text();
            log.debug("接收到消息 clientKey:{}, msg: {}", clientKey, paramString);
            if(pingPongFactory.getPongMsg().equals(paramString) || "成功".equals(paramString)){
                return;
            }
            // 透传到下一个Handler
            ctx.fireChannelRead(paramString);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                log.debug("发送心跳给服务端 {}", clientKey);
                String msg = pingPongFactory.getPingMsg();
                WebSocketFrame frame = new TextWebSocketFrame(msg);
                ctx.channel().writeAndFlush(frame);
            }else if(IdleState.READER_IDLE.equals(event.state())){
                log.debug("连接通道无响应重新连接 {}", clientKey);
                // 如果都通道处于空闲，服务端没有响应，就进行重连
                SeviceChannelPool.reconnect(clientKey);
            }
        }
    }


}
