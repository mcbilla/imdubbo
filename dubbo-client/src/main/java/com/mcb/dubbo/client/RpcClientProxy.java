package com.mcb.dubbo.client;

import com.mcb.dubbo.common.model.RpcRequest;
import com.mcb.dubbo.common.model.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 代理类，通过动态代理为带有RpcReference注解的类绑定远程实现类
 */
public class RpcClientProxy {
    private RpcClientDiscovery discovery = new RpcClientDiscovery();

    private final Object obj = new Object();

    public <T> T create(final Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //创建RpcRequest，封装被代理类的属性
                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                //拿到声明这个方法的业务接口名称
                request.setClassName(method.getDeclaringClass()
                        .getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);

                // 获取远程服务的地址
                String path = discovery.discover(interfaceClass.getName());
                String[] array = path.split(":");
                String host = array[0];
                int port = Integer.parseInt(array[1]);

                // 发送请求
                // todo 这种方法可能存在线程不安全的问题
                RpcResponse response = new RpcResponse();
                send(request, response, host, port);
                return response.getResult();
            }
        });
    }

    /**
     * todo 目前都是短连接，后面再改成长连接
     * @param request
     */
    public void send(RpcRequest request, RpcResponse response, String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientChannelInitializer(response, obj))
                    .option(ChannelOption.SO_BACKLOG, 128);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(request).sync();
            synchronized (obj) {
                obj.wait();
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
