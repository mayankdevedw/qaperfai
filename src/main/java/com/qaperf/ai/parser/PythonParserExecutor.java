package com.qaperf.ai.parser;

import com.qaperf.ai.command.CommandExecutorImpl;
import com.qaperf.ai.command.ProcessData;
import com.qaperf.ai.command.ProcessDataImpl;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class PythonParserExecutor {
    private static CommandExecutorImpl commandExecutor = new CommandExecutorImpl();
    public final static Logger LOG = Logger.getLogger(PythonParserExecutor.class);


    private ProcessData executeCommandSequence(List<String> commandList, boolean printToConsole) {

        ProcessDataImpl data_of_process = (ProcessDataImpl)commandExecutor.executeCommandLocalHost(commandList);
        if (!printToConsole)
            LOG.debug(data_of_process.toString());
        else
            LOG.debug(data_of_process);

        int exit_code = data_of_process.getExitCodeValue();
        LOG.info(String.format("Status of Command is %s", exit_code));

        return data_of_process;
    }

    public  ProcessData executeCommand(final String com, boolean printToConsole) {
        List<String> commandList = new ArrayList<String>(){{
            add(com);}
        };
        return executeCommandSequence( commandList, printToConsole);
    }
}
