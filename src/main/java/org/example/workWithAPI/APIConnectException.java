package org.example.workWithAPI;

public class APIConnectException extends Exception{
    private final String detail;
    public APIConnectException(String errorMessage) { detail = errorMessage; }

    @Override
    public String getMessage() {return "APIConnectException [" + detail + "]"; }
}
