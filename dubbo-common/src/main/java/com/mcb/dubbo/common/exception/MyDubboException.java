package com.mcb.dubbo.common.exception;

public class MyDubboException extends RuntimeException{

    private String code;

    public MyDubboException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public MyDubboException(String code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
    }
}
