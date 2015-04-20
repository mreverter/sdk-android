package com.mercadopago.model;

public class ApiException {

    private Cause cause;
    private String error;
    private String message;
    private Integer status;

    public ApiException() {
    }

    public ApiException(String message, Integer status, String error, Cause cause) {

        this.message = message;
        this.status = status;
        this.error = error;
        this.cause = cause;
    }

    public Cause getCause() {
        return cause;
    }

    public void setCause(Cause cause) {
        this.cause = cause;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
