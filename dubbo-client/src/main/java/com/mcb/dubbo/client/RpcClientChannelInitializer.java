package com.mcb.dubbo.client;

import com.mcb.dubbo.common.codec.RpcDecoder;
import com.mcb.dubbo.common.codec.RpcEncoder;
import com.mcb.dubbo.common.model.RpcRequest;
import com.mcb.dubbo.common.model.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class RpcClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private RpcResponse response;

    private final Object obj;

    public RpcClientChannelInitializer(RpcResponse response, Object obj) {
        this.response = response;
        this.obj = obj;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new RpcDecoder(RpcResponse.class))
                .addLast(new RpcEncoder(RpcRequest.class))
                .addLast(new RpcClientHandler(response, obj));
    }
}
