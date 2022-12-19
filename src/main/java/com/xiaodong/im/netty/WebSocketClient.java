package com.xiaodong.im.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebSocketClient {
    private final static EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap clientBootstrap;
    private final ClientChannelAttribute clientChannelAttribute;

    public WebSocketClient(ClientChannelAttribute clientChannelAttribute){
        this.clientBootstrap = getBootstrap();
        this.clientChannelAttribute = clientChannelAttribute;
    }

    private Bootstrap getBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap;
    }

    /**
     * 添加websocket客户端用户
     * @return
     */
    public ClientChannelAttribute connect(){
        SeviceChannelPool.closeChannel(clientChannelAttribute.getClientKey());
        ClientConnectResolveFactory connectResolveFactory = new ClientConnectResolveFactory(clientChannelAttribute);
        ClientChannelAttribute webSocketClient = connectResolveFactory.build();
        SeviceChannelPool.setWebSocketClient(clientChannelAttribute.getClientKey(), webSocketClient);
        ChannelFuture channelFuture = doConnect(clientBootstrap, webSocketClient.getHost(), webSocketClient.getPort(), clientChannelAttribute.getClientKey());
        webSocketClient.setFuture(channelFuture);
        return webSocketClient;
    }


    /**
     * 设置websocket通道处理器
     */
    private class ClientWebSocketChannel extends ChannelInitializer<SocketChannel> {
        private final String clientKey;

        private ClientWebSocketChannel(String clientKey) {
            this.clientKey = clientKey;
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ClientChannelAttribute webSocketClient = SeviceChannelPool.getWebSocketClient(clientKey);
            if(webSocketClient==null){
                log.error("没有添加对应的连接属性");
                return;
            }
            // 设置标识
            ChannelPipeline pipeline = ch.pipeline();
            // 添加自定义协议的编解码工具
            pipeline.addLast(new IdleStateHandler(30, 10, 0, TimeUnit.SECONDS));
            if (webSocketClient.getSslContext() != null) {
                pipeline.addLast(webSocketClient.getSslContext().newHandler(ch.alloc(), webSocketClient.getHost(), webSocketClient.getPort()));
            }
            pipeline.addLast(new HttpClientCodec());
            pipeline.addLast(new HttpObjectAggregator(8192));
            SocketPingPongFactory pingPongFactory = new SocketPingPongFactory();
            pipeline.addLast(new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(webSocketClient.getUri(), WebSocketVersion.V13,null,true, new DefaultHttpHeaders()),pingPongFactory, clientKey));
            pipeline.addLast(new ReceiveMsgHandler(clientChannelAttribute));
        }
    }


    /**
     * bootstrap建立连接
     * @param bootstrap
     * @param host
     * @param port
     */
    private ChannelFuture doConnect(Bootstrap bootstrap, String host, int port, String clientKey) {
        try {
            if (bootstrap != null) {
                bootstrap.handler(new ClientWebSocketChannel(clientKey));
                bootstrap.remoteAddress(host, port);
                ChannelFuture f = bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    //final EventLoop eventLoop = futureListener.channel().eventLoop();
                    if (!futureListener.isSuccess()) {
                        //连接tcp服务器不成功 10后重连
                        log.error("{}与服务端连接失败!", clientKey);
                        // eventLoop.schedule(() -> doConnect(bootstrap, host, port, clientKey), 10, TimeUnit.SECONDS);
                        // 记录通知连接失败的节点
                    }
                });
                return f;
            }
        } catch (Exception e) {
            System.out.println("客户端连接失败!" + e.getMessage());
            log.error("客户端连接失败! {}", e.getMessage());
        }
        return null;
    }

}
