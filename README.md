# 描述
通过Netty实现的websocket客户端，包括连接远程socket,自动发送心跳（心跳格式需要修改成自己的），支持实现wss的websocket连接

example:
```
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
```

## 修改心跳数据格式
在SocketPingPongFactory中修改心跳PING\PONG数据格式

