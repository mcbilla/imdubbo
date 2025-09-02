package com.mcb.dubbo.common;


import com.mcb.dubbo.common.utils.SerializationUtil;
import org.junit.Test;

import java.util.Arrays;

public class DubboCommonApplicationTests {

    @Test
    public void testGetSchema() {
        User user = new User("mcb", 123);
        System.out.println(SerializationUtil.getSchema(User.class));
        byte[] bytes = SerializationUtil.serialize(user);
        System.out.println("原来的对象：" + user);
        System.out.println("序列化后：" + Arrays.toString(bytes));
        System.out.println("反序列化后：" + SerializationUtil.deserialize(bytes, User.class));

    }

}

class User {
    private String name;

    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("User{");
        sb.append("name='").append(name).append('\'');
        sb.append(", age=").append(age);
        sb.append('}');
        return sb.toString();
    }
}
