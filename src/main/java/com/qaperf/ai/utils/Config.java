package com.qaperf.ai.utils;

public class Config {
    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    private String baseDir;
    private int maxTime;
    private int captureInterval;

    public int getMaxTime() {
        return maxTime;
    }

    public boolean isTraining() {
        return training;
    }

    public void setTraining(boolean training) {
        this.training = training;
    }

    private boolean training;

    @Override
    public String toString() {
        return "Config{" +
                "baseDir='" + baseDir + '\'' +
                ", maxTime=" + maxTime +
                ", captureInterval=" + captureInterval +
                '}';
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public int getCaptureInterval() {
        return captureInterval;
    }

    public void setCaptureInterval(int captureInterval) {
        this.captureInterval = captureInterval;
    }
}
