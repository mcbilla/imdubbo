package com.mcb.dubbo.client;

import com.mcb.dubbo.common.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * rpc客户端启动类
 *
 * 和服务器不太一样，服务器是启动之后就运行netty服务，然后等待客户端连上来
 * 而客户端在启动的时候不需要建立netty服务，只需要在zk上查找实现类，然后在方法调用的时候再通过代理类RpcClientProxy建立netty服务
 */
public class RpcClient implements BeanPostProcessor {

    private RpcClientProxy proxy = new RpcClientProxy();

    public RpcClient() {
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    /**
     * 收集带有RpcReference注解的类名及类对象，并为其绑定远程实现类
     *
     * @throws Exception
     */
    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        ReflectionUtils.doWithLocalFields(o.getClass(), new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if(field.getDeclaredAnnotation(RpcReference.class) != null) {
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, o, proxy.create(field.getType()));
                }
            }
        });
        return o;
    }
}
