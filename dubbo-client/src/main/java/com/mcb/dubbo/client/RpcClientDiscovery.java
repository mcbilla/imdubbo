package com.mcb.dubbo.client;

import com.mcb.dubbo.common.constant.ErrorCode;
import com.mcb.dubbo.common.exception.MyDubboException;
import com.mcb.dubbo.registry.Constant;
import com.mcb.dubbo.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class RpcClientDiscovery {
    private LoadBalance loadBalance = new RandomLoadBalance();

    List<String> repos = new ArrayList<String>();

    public String discover(String interfaceName) {
        StringBuilder builder = new StringBuilder();
        String path = builder.append(Constant.ZK_REGISTRY_PATH)
                .append("/")
                .append(interfaceName)
                .append(Constant.ZK_DATA_PATH)
                .toString();
        String data = ServiceRegistry.getNode(path);
        repos = Arrays.asList(data.split(","));
        if (repos.size() > 0) {
            // 监听节点内容变化
            ServiceRegistry.watchNode(path);
            return loadBalance.select(repos);
        } else {
            throw new MyDubboException(ErrorCode.NONMETHOD_FAIL, "接口 " + interfaceName + " 暂时没有实现类");
        }
    }
}
