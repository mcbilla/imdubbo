package com.mcb.dubbo.client;

import java.util.List;

public interface LoadBalance {
    String select(List<String> repos);
}
