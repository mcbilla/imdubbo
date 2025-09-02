package com.mcb.dubbo.common.utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.mcb.dubbo.common.constant.ErrorCode;
import com.mcb.dubbo.common.exception.MyDubboException;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationUtil {
    // Protostuff序列化的时候需要用到schema对象，如果每次调用序列化方法和反序列化方法时都需要重新生成一个schema对象有点浪费，把每个类对应的schema对象缓存起来，可以提高序列化和反序列化的速度
    private static Map<Class<?>, Schema> cachedSchema = new ConcurrentHashMap<>();

    // Objenesis是一个轻量级的Java库，作用是绕过构造器创建一个实例。Spring引入Objenesis后，Bean不再必须提供无参构造器了。
    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil() {
    }

    // 通过缓存的方式获取类的schema对象
    public static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>)cachedSchema.get(cls);
        if(schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }

    // 序列化（对象 -> 字节数组）
    public static <T> byte[] serialize(T obj) {
        Class<T> clz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new MyDubboException(ErrorCode.SERIALIZE_FAIL, "序列化失败");
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> clz) {
        try {
            /*
             * 如果一个类没有参数为空的构造方法时候，那么你直接调用newInstance方法试图得到一个实例对象的时候是会抛出异常的
             * 通过ObjenesisStd可以完美的避开这个问题
             * */
            T message = (T)objenesis.newInstance(clz);
            Schema<T> schema = getSchema(clz);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new MyDubboException(ErrorCode.DESERIALIZE_FAIL, "反序列化失败");
        }
    }
}
