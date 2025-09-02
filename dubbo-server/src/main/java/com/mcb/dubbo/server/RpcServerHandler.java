package com.mcb.dubbo.server;

import com.mcb.dubbo.common.model.RpcRequest;
import com.mcb.dubbo.common.model.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    // handlerMap存储着所有带有RpcService注解的类名
    private Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        try {
            Object rs = handlerMsg(rpcRequest);
            response.setResult(rs);
        }catch (Exception e) {
            response.setError(e);
        }
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    // 思路是根据反射，调用request指定类名的实现类
    private Object handlerMsg(RpcRequest request) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取类名、方法名、参数类型和参数值
        String className = request.getClassName();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // 获取实现类
        Class<?> forName = Class.forName(className);

        // 获取实现类对象
        Object serverBean = handlerMap.get(className);

        // 根据方法名和参数类型，获取方法名
        Method method = forName.getMethod(methodName, parameterTypes);

        // 实现类对象传入指定参数值调用指定方法
        return method.invoke(serverBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
