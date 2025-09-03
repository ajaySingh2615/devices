package com.cadt.devices.exception;

public class ApiException extends RuntimeException {

    private final String code;

    public ApiException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
