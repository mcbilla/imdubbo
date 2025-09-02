package com.mcb.dubbo.spring.boot.autoconfigure;

import com.mcb.dubbo.client.RpcClient;
import com.mcb.dubbo.server.RpcServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ImDubboConfigurationProperties.class)
@ConditionalOnProperty(prefix = "imdubbo", name = "enabled", matchIfMissing = true)
public class ImDubboAutoConfiguration {

    private final ImDubboConfigurationProperties properties;

    public ImDubboAutoConfiguration(ImDubboConfigurationProperties properties) {
        this.properties = properties;
    }

    @ConditionalOnProperty(prefix = "imdubbo.registry", name = "type", havingValue = "provider")
    @Bean
    public RpcServer rpcServer() {
        return new RpcServer(properties.getApplication().getName(), properties.getProtocol().getPort());
    }

    @ConditionalOnProperty(prefix = "imdubbo.registry", name = "type", havingValue = "consumer")
    @Bean
    public RpcClient rpcClient() {
        return new RpcClient();
    }
}
