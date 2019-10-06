package com.qaperf.ai.command;

import java.util.List;


public interface CommandExecutor {
    ProcessData executeCommandLocalHost(List<String> var1);

    ProcessData executeCommandLocalHost(String var1);
}