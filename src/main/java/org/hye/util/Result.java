package org.hye.util;

import lombok.Data;

@Data
public class Result<T> {
    T info;
    String msg;
    Integer code;

    public Result() {
    }

    public Result(T info, String msg) {
        this.info = info;
        this.msg = msg;
    }

    public Result(T info, Integer code) {
        this.info = info;
        this.code = code;
    }

    public Result(T info, String msg, Integer code) {
        this.info = info;
        this.msg = msg;
        this.code = code;
    }

    public Result(String msg) {
        this.msg = msg;
    }

    public Result(String msg, Integer code) {
        this.msg = msg;
        this.code = code;
    }
}
