package com.mcb.dubbo.server;

import com.mcb.dubbo.registry.Constant;
import com.mcb.dubbo.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Slf4j
public class RpcServerDiscovery {
    public void register(List<String> providerInterfaces, int port) throws UnknownHostException {
        // todo 目前支持注册单机
        InetAddress address = InetAddress.getLocalHost();
        String hostAddress = address.getHostAddress() + ":" + port;
        providerInterfaces.forEach(i -> {
            StringBuilder builder = new StringBuilder();
            String path = builder.append(Constant.ZK_REGISTRY_PATH)
                    .append("/")
                    .append(i)
                    .append(Constant.ZK_DATA_PATH)
                    .toString();
            if (!ServiceRegistry.existNode(path)) {
                log.info("创建节点：{} {}", path, hostAddress);
                ServiceRegistry.createNode(path, hostAddress);
            } else {
                log.info("节点已存在，更新内容：{} {}", path, hostAddress);
                ServiceRegistry.updateNode(path, hostAddress);
            }
        });
    }
}
