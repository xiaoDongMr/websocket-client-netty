package com.xiaodong.im.netty;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class ClientConnectResolveFactory {
    private final ClientChannelAttribute attribute;

    public ClientConnectResolveFactory(ClientChannelAttribute clientChannelAttribute){
        this.attribute = clientChannelAttribute;
    }

    public ClientChannelAttribute build(){
        try {
            URI uri = new URI(attribute.getUrl());
            String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                throw new RuntimeException("Only WS(S) is supported.");
            }
            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
            attribute.setUri(uri);
            attribute.setHost(host);
            attribute.setScheme(scheme);
            attribute.setPort(getWsPort(uri, scheme));

            final boolean ssl = "wss".equalsIgnoreCase(scheme);
            final SslContext sslCtx;
            if (ssl) {
                sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } else {
                sslCtx = null;
            }
            attribute.setSslContext(sslCtx);
            return attribute;
        } catch (Exception  e) {
            e.printStackTrace();
            log.error("创建ws连接失败 url->{} err->{}", attribute.getUrl(), e);
        }
        return null;
    }

    public Integer getWsPort(URI uri, String scheme){
        int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }
        return port;
    }

}
