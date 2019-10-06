package com.qaperf.ai.command;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CommandExecutorImpl implements CommandExecutor {
    public static final Logger LOG = Logger.getLogger(CommandExecutorImpl.class);
    @Override
    public ProcessData executeCommandLocalHost(List<String> commands) {
        ProcessData result = null;

        String command;
        for(Iterator var3 = commands.iterator(); var3.hasNext(); result = this.executeCommandLocalHost(command)) {
            command = (String)var3.next();
        }

        return result;
    }

    @Override
    public ProcessData executeCommandLocalHost(String command) {
        String mergedCommand = StringUtils.join(new String[]{"bash", "-c", command}, " ");
        String[] bashCommand = new String[]{"bash", "-c", command};
        LOG.log(Level.DEBUG, "Sending command \"" + mergedCommand + "\" to localhost");
        ProcessBuilder processBuilder = new ProcessBuilder(bashCommand);
        Process process = null;

        try {
            process = processBuilder.start();
        } catch (IOException var7) {
            throw new RuntimeException(var7);
        }

        ProcessData data_of_process = new ProcessDataImpl(process);
        data_of_process.getData(ProcessDataType.EXIT_CODE);
        return data_of_process;
    }
}
