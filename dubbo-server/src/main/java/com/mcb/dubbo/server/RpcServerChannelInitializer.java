package com.mcb.dubbo.server;

import com.mcb.dubbo.common.codec.RpcDecoder;
import com.mcb.dubbo.common.codec.RpcEncoder;
import com.mcb.dubbo.common.model.RpcRequest;
import com.mcb.dubbo.common.model.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

public class RpcServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> handlerMap;

    public RpcServerChannelInitializer(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new RpcDecoder(RpcRequest.class))
                .addLast(new RpcEncoder(RpcResponse.class))
                .addLast(new RpcServerHandler(handlerMap));
    }
}
