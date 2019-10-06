package com.qaperf.ai.base;

public class ActionBeans {
    private String actionName;
    private long actionStartTime;
    private long actionEndTime;
    private long actionLoadTime;

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        return "ActionBeans{" +
                "actionName='" + actionName + '\'' +
                ", actionStartTime=" + actionStartTime +
                ", actionEndTime=" + actionEndTime +
                ", actionLoadTime=" + actionLoadTime +
                '}';
    }

    public long getActionStartTime() {
        return actionStartTime;
    }

    public void setActionStartTime(long actionStartTime) {
        this.actionStartTime = actionStartTime;
    }

    public long getActionEndTime() {
        return actionEndTime;
    }

    public void setActionEndTime(long actionEndTime) {
        this.actionEndTime = actionEndTime;
    }

    public long getActionLoadTime() {
        return actionLoadTime;
    }

    public void setActionLoadTime(long actionLoadTime) {
        this.actionLoadTime = actionLoadTime;
    }
}
