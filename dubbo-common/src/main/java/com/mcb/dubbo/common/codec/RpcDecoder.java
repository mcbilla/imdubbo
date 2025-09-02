package com.mcb.dubbo.common.codec;

import com.mcb.dubbo.common.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * netty解码器
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;

    public RpcDecoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes() < 4) {
            return;
        }

        // 标记位，可用于异常恢复
        byteBuf.markReaderIndex();

        // 长度异常，直接关闭channel
        int dataLength = byteBuf.readInt();
        if(dataLength < 0) {
            channelHandlerContext.close();
        }

        // 可读长度小于理论长度，可能存在拆包情况，等下再度
        if(byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
        }

        // 开始正常解码
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        Object obj = SerializationUtil.deserialize(data, clazz);
        list.add(obj);
    }
}
