package com.mcb.dubbo.registry;

import com.mcb.dubbo.common.constant.ErrorCode;
import com.mcb.dubbo.common.exception.MyDubboException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServiceRegistry {

    // 连接zk的客户端，使用单例模式初始化
    private static volatile CuratorFramework curatorClient = null;

    private static String registryAddress = "127.0.0.1:2181";

    private static int sessionTimeout = 5000;

    private static int connectionTimeout = 5000;

    private ServiceRegistry() {
    }

    // 单例模式，因为根据zk官网
    // 在一个应用中,只需要一个ZK实例就足够了.CuratorFramework实例都是线程安全的，你应该在你的应用中共享同一个CuratorFramework实例
    public static CuratorFramework getClient() {
        if (curatorClient == null) {
            synchronized (ServiceRegistry.class) {
                if (curatorClient == null) {
                    curatorClient = CuratorFrameworkFactory.builder()
                            // Zookeeper 服务器地址字符串
                            .connectString(registryAddress)
                            // 连接超时时间
                            .connectionTimeoutMs(connectionTimeout)
                            // session 会话超时时间
                            .sessionTimeoutMs(sessionTimeout)
                            // 使用哪种重连策略，可重连3次，并增加每次重连之间的睡眠时间
                            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                            .build();
                    curatorClient.start();
                    return curatorClient;
                }
            }
        }
        return curatorClient;
    }

    /**
     * 创建节点
     */
    public static void createNode(final String path, String data) {
        CuratorFramework client = getClient();
        try {
            if (client.checkExists().forPath(path) == null) {
                String rs = client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path, data.getBytes());
                log.info("createNode success {} {}", rs, data);
            } else {
                log.info("createNode fail, node already exist");
            }
        } catch (Exception e) {
            throw new MyDubboException(ErrorCode.CREATE_NODE_FAIL, "创建节点失败", e);
        }
    }

    /**
     * 更新节点
     */
    public static void updateNode(final String path, String data) {
        CuratorFramework client = getClient();
        try {
            if (client.checkExists().forPath(path) != null) {
                client.setData().forPath(path, data.getBytes());
                log.info("updateNode success {} {}", path, data);
            } else {
                log.info("updateNode fail");
            }
        } catch (Exception e) {
            throw new MyDubboException(ErrorCode.CREATE_NODE_FAIL, "创建节点失败", e);
        }
    }

    /**
     * 获取节点
     * @return
     */
    public static String getNode(final String path) {
        CuratorFramework client = getClient();
        try {
            Stat stat = new Stat();
            byte[] nodeData = client.getData().storingStatIn(stat).forPath(path);
            return new String(nodeData);
        } catch (Exception e) {
            throw new MyDubboException(ErrorCode.GET_NODE_FAIL, "获取节点失败", e);
        }
    }

    /**
     * 判断节点是否存在
     * @return
     */
    public static Boolean existNode(final String path) {
        CuratorFramework client = getClient();
        try {
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            throw new MyDubboException(ErrorCode.EXIST_NODE_FAIL, "检查节点存在失败", e);
        }
    }

    /**
     * 监控节点更新，因为要一直监控所以不用关闭连接
     */
    public static void watchNode(final String path) {
        CuratorFramework client = getClient();
        try {
            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
            final List<String> repos = new ArrayList<>();
            PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {

                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                }
            };
            pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new MyDubboException(ErrorCode.EXIST_NODE_FAIL, "监控节点失败", e);
        }
    }

    /**
     * springboot停止前关闭连接
     */
    @PreDestroy
    public void end(){
        curatorClient.close();
    }
}
