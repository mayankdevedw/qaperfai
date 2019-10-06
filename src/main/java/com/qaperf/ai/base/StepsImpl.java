package com.qaperf.ai.base;

import com.qaperf.ai.utils.TransactionUtils;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StepsImpl implements TestSteps {
    CommonBeans cm=new CommonBeans();
    TransactionUtils utils=new TransactionUtils();
    TransactionBeans txnbeans =null;
    StepBeans stepBean=null;
    Map<String, List<StepBeans>> txnMap=new HashMap<>();

    @Override
    public void beginStep(String name) {
        stepBean=new StepBeans();
        stepBean.setStepName(name);
        stepBean.setStepStartTime(System.currentTimeMillis());
        utils.createDirectory(name.replace(' ','_'), cm);
        cm.setStepDir(cm.getTxnDir()+name.replace(' ','_'));
        cm.setCurrentDir(cm.getStepDir());

    }

    @Override
    public void endStep() {
        stepBean.setStepEndTime(System.currentTimeMillis());
        stepBean.setStepTime(stepBean.getStepStartTime()-stepBean.getStepEndTime());
        txnbeans.add(stepBean);
        cm.setCurrentDir(cm.getTxnDir());
        stepBean=null;
    }

    @Override
    public void beginTransaction() {
        if(txnbeans == null){
        txnbeans = new TransactionBeans();

        }
       String txnid= utils.generateRefnumber(10);
       utils.createDirectory(txnid, cm);

       txnbeans.setTransactionName(txnid);
       cm.setTxnDir(cm.getBaseDir()+txnid);
       cm.setCurrentDir(cm.getTxnDir());

    }

    @Override
    public void endTransaction() {
        txnMap.put(txnbeans.getTransactionName(), txnbeans.getStepBeans());
        cm = new CommonBeans();
        txnbeans=null;

    }



    public abstract void startTestAction(String name, WebDriver driver);
    public abstract void endTestAction();

}
