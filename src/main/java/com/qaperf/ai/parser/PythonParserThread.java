package com.qaperf.ai.parser;

import com.qaperf.ai.command.ProcessData;
import com.qaperf.ai.utils.Settings;
import org.apache.log4j.Logger;

import java.io.File;

public class PythonParserThread implements Runnable {
    public final static Logger LOG= Logger.getLogger(PythonParserThread.class);;
    private String datasetLocation;
    private String parser_file = System.getProperty("user.dir")+ File.separator + Settings.pythonFolderName +File.separator +"%s";
    private String command="python %s %s %s ";
    private boolean printToConsole=false;
    private String actionName;
    private String fullImage;
    private String visualImage;
    private String annotateImage;
    private String pythonScript;



    public PythonParserThread(String datasetLocation, String actionName, String args, String parser_file){
        this.datasetLocation = datasetLocation;
        this.command = String.format(this.command, String.format(this.parser_file, parser_file), args ,this.datasetLocation);
        this.actionName = actionName;
    }

    public PythonParserThread(String datasetLocation, boolean printToConsole){
        this.datasetLocation = datasetLocation;
        this.command = String.format(this.command, this.parser_file, this.datasetLocation);
        this.printToConsole=printToConsole;
    }





    @Override
    public void run() {
        LOG.debug(String.format("Parser thread started for %s", actionName));
        PythonParserExecutor executor=new PythonParserExecutor();
        ProcessData data_of_process = executor.executeCommand(this.command,this.printToConsole);
        LOG.info(data_of_process);
        ThreadMonitor.decrement();
        LOG.debug(String.format("Parser thread Ended %s", actionName));
        LOG.debug(String.format("Active Parser thread %s", ThreadMonitor.getActiveThread()));
    }


}
