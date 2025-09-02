package com.mcb.dubbo.server;

import com.mcb.dubbo.common.annotation.RpcService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * rpc服务端启动类
 */
@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean {

    //  用于存储业务接口和实现类的实例对象
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    private RpcServerDiscovery discovery = new RpcServerDiscovery();

    private final String name;

    private final Integer bindPort;

    public RpcServer(String name, Integer bindPort) {
        this.name = name;
        this.bindPort = bindPort;
    }

    // 收集带有RpcService注解的类名及类对象
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取带有RpcService注解的所有类
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!serviceBeanMap.isEmpty()) {
            for (Object serviceBean : serviceBeanMap.values()) {
                // 获取带有RpcService注解的类名
                String interfaceName = serviceBean.getClass().getInterfaces()[0].getName();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    // 为bean提供了属性初始化后的处理方法
    @Override
    public void afterPropertiesSet() throws Exception {
        runServer();
    }

    // 把provider的所有接口注册到zk

    private void runServer() {
        // 1、把服务注册到zk上
        List<String> providerInterfaces = handlerMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        try {
            discovery.register(providerInterfaces, name, bindPort);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // 2、启动服务器，等待连接
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServerChannelInitializer(handlerMap))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(bindPort).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
