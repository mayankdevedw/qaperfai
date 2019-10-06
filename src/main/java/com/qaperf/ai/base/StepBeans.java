package com.qaperf.ai.base;

import java.util.ArrayList;
import java.util.List;

public class StepBeans  {

    private long stepTime;
    private long stepStartTime;
    private long stepEndTime;
    private String stepName;
    private List<ActionBeans> actionBeans;

    public List<ActionBeans> getActionBeans() {
        if (actionBeans == null) {
            actionBeans = new ArrayList<>();
            }

        return actionBeans;
    }

    public void addAction(ActionBeans action){
        actionBeans.add(action);
    }



    public long getStepResourceSize() {
        return stepResourceSize;
    }

    public void setStepResourceSize(long stepResourceSize) {
        this.stepResourceSize = stepResourceSize;
    }

    private long stepResourceSize;

    public long getStepStartTime() {
        return stepStartTime;
    }

    public void setStepStartTime(long stepStartTime) {
        this.stepStartTime = stepStartTime;
    }

    public long getStepEndTime() {
        return stepEndTime;
    }

    @Override
    public String toString() {
        return "StepBeans{" +
                "stepTime=" + stepTime +
                ", stepStartTime=" + stepStartTime +
                ", stepEndTime=" + stepEndTime +
                ", stepName='" + stepName + '\'' +
                ", actionBeans=" + actionBeans +
                ", stepResourceSize=" + stepResourceSize +
                '}';
    }

    public void setStepEndTime(long stepEndTime) {
        this.stepEndTime = stepEndTime;
    }

    public long getStepTime() {
        return stepTime;
    }

    public void setStepTime(long stepTime) {
        this.stepTime = stepTime;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
}
