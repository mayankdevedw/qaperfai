package com.qaperf.ai.parser;

public class ThreadMonitor {

    public static int threadCountMonitor = 0;

    public static void increment(){
        threadCountMonitor++;
    }

    public static void decrement(){
        threadCountMonitor--;
    }

    public static int getActiveThread(){
        return threadCountMonitor;
    }

}
