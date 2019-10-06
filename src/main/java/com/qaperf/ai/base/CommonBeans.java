package com.qaperf.ai.base;

import com.qaperf.ai.utils.Config;
import com.qaperf.ai.utils.ConfigLoader;

import java.io.File;

public class CommonBeans {

    Config config = ConfigLoader.loadConfig();
    public CommonBeans(){
        this.baseDir = config.getBaseDir();
        this.currentDir = config.getBaseDir();

    }

    private String baseDir;

    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    private String currentDir;

    public String getBaseDir() {
        return baseDir;
    }

    private String txnDir;
    private String stepDir;

    public String getTxnDir() {
        return txnDir;
    }

    public void setTxnDir(String txnDir) {
        this.txnDir = txnDir+ File.separator;
    }

    public String getStepDir() {
        return stepDir;
    }

    public void setStepDir(String stepDir) {
        this.stepDir = stepDir+File.separator;
    }

    public String getActionDir() {
        return actionDir;
    }

    public void setActionDir(String actionDir) {
        this.actionDir = actionDir+File.separator;
    }

    private String actionDir;
}
