package com.agent.enums;

public enum FluxType {

    COMPLETED("completed", true, "执行完成"),
    CANCELLED("cancelled", false, "用户取消"),
    FAILED("failed", false, "执行失败"),
    UNKNOWN("failed", false, "未知错误");

    private final String status;
    private final boolean ok;
    private final String description;

    FluxType(String status, boolean ok, String description) {
        this.status = status;
        this.ok = ok;
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOk() {
        return ok;
    }

    public String getDescription() {
        return description;
    }
}
