package com.mcb.dubbo.client;

import com.mcb.dubbo.common.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 负责接收RpcResponse
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private RpcResponse response;

    private final Object obj;

    public RpcClientHandler(RpcResponse response, Object obj) {
        this.response = response;
        this.obj = obj;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        if (!rpcResponse.isError()) {
            response = rpcResponse;
            log.info("获得返回结果成功 {}", rpcResponse.getResult());
            synchronized (obj) {
                obj.notifyAll();
            }
            channelHandlerContext.close();
        } else {
            log.error("获得返回结果失败 {}", rpcResponse.getError());
            channelHandlerContext.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
}
