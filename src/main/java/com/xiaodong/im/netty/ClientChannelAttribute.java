package com.xiaodong.im.netty;

import com.xiaodong.im.service.IBizSendAndReceive;
import io.netty.channel.ChannelFuture;
import io.netty.handler.ssl.SslContext;
import lombok.Builder;
import lombok.Data;

import java.net.URI;

@Data
@Builder
public class ClientChannelAttribute {
    /**
     * 客户端连接名称
     */
    private String clientKey;
    /**
     * 连接url
     */
    private String url;
    /**
     * 业务数据发送与接收
     */
    private IBizSendAndReceive bizSendAndReceive;

    private String scheme;
    private String host;
    private Integer port;
    private URI uri;
    private SslContext sslContext;
    private ChannelFuture future;
}
