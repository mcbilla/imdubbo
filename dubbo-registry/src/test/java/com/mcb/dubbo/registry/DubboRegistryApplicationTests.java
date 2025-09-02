package com.mcb.dubbo.registry;


import org.junit.Test;

public class DubboRegistryApplicationTests {
    @Test
    public void contextLoads() {
        ServiceRegistry.createNode("com.mcb.test", "192.168.10.1");
    }

}
