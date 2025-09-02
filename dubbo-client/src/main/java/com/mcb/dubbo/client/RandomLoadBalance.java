package com.mcb.dubbo.client;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {
    private static Random random = new Random();

    @Override
    public String select(List<String> repos) {
        int len = repos.size();
        if (len <= 0) {
            throw new RuntimeException("未发现注册的服务");
        } else if (len == 1) {
            return repos.get(0);
        } else {
            return repos.get(random.nextInt(len));
        }
    }
}
