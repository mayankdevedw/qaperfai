package com.qaperf.ai.base;

import java.util.ArrayList;
import java.util.List;

public class TransactionBeans {
private String transactionName;

    public List<StepBeans> getStepBeans() {
        if(stepBeans == null){
            stepBeans = new ArrayList<>();
        }
        return stepBeans;
    }


    private List<StepBeans> stepBeans;

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }


    public void add(StepBeans stepBeans){
        getStepBeans().add(stepBeans);

    }

    @Override
    public String toString() {
        return "TransactionBeans{" +
                "transactionName='" + transactionName + '\'' +
                ", stepBeans=" + stepBeans +
                '}';
    }
}
