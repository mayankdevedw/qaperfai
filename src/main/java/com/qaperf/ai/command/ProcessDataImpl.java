package com.qaperf.ai.command;


import org.apache.log4j.Logger;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class ProcessDataImpl implements ProcessData {
    public static final Logger LOG = Logger.getLogger(ProcessDataImpl.class);
    private Process checked_process;
    private boolean printToConsole = false;
    private String outPutStream = null;
    private String errorStream = null;
    private Integer returnCode = null;
    private long silenceTimeout = 600000L;
    private final long unconditionalExitDelayMinutes = 30L;

    public ProcessDataImpl(Process connected_process, boolean printToConsole, int silenceTimeout, TimeUnit timeUnit) {
        this.checked_process = connected_process;
        this.printToConsole = printToConsole;
        this.silenceTimeout = TimeUnit.MILLISECONDS.convert((long)silenceTimeout, timeUnit);
    }

    public ProcessDataImpl(Process connected_process, boolean printToConsole, int silenceTimeoutSec) {
        this.checked_process = connected_process;
        this.printToConsole = printToConsole;
        this.silenceTimeout = TimeUnit.MILLISECONDS.convert((long)silenceTimeoutSec, TimeUnit.SECONDS);
    }

    public ProcessDataImpl(Process connected_process, boolean printToConsole) {
        this.checked_process = connected_process;
        this.printToConsole = printToConsole;
    }

    public ProcessDataImpl(Process connected_process) {
        this.checked_process = connected_process;
        this.printToConsole = true;
    }

    public static boolean isRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException var2) {
            return true;
        }
    }

    public String getData(ProcessDataType type) {
        switch(type) {
            case OUTPUT:
                return this.getOutPutStream();
            case ERROR:
                return this.getErrorStream();
            case EXIT_CODE:
                return Integer.toString(this.getExitCodeValue());
            case STREAMS_MERGED:
                return this.getOutPutStream() + "\n" + this.getErrorStream();
            default:
                throw new IllegalArgumentException("Data Type " + type + " not supported yet!");
        }
    }

    public int getExitCodeValue() {
        try {
            if (this.returnCode == null) {
                try {
                    this.buildOutputAndErrorStreamData();
                } catch (Exception var2) {
                    throw new RuntimeException("Couldn't retrieve Output Stream data from process: " + this.checked_process.toString(), var2);
                }
            }
        } catch (Exception var3) {
            throw new RuntimeException("Couldn't finish waiting for process " + this.checked_process + " termination", var3);
        }

        return this.returnCode;
    }

    public String getOutPutStream() {
        if (this.outPutStream == null) {
            try {
                this.buildOutputAndErrorStreamData();
            } catch (Exception var2) {
                throw new RuntimeException("Couldn't retrieve Output Stream data from process: " + this.checked_process.toString(), var2);
            }
        }

        this.outPutStream = removeUnneccessaryOutput(this.outPutStream);
        this.errorStream = removeUnneccessaryOutput(this.errorStream);
        return this.outPutStream;
    }

    public String getErrorStream() {
        if (this.errorStream == null) {
            try {
                this.buildOutputAndErrorStreamData();
            } catch (Exception var2) {
                throw new RuntimeException("Couldn't retrieve Error Stream data from process: " + this.checked_process.toString(), var2);
            }
        }

        this.outPutStream = removeUnneccessaryOutput(this.outPutStream);
        this.errorStream = removeUnneccessaryOutput(this.errorStream);
        return this.errorStream;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(String.format("[OUTPUT STREAM]\n%s\n", this.outPutStream));
        result.append(String.format("[ERROR STREAM]\n%s\n", this.errorStream));
        result.append(String.format("[EXIT CODE]\n%d", this.returnCode));
        return result.toString();
    }

    private void buildOutputAndErrorStreamData() throws IOException {
        StringBuilder sbInStream = new StringBuilder();
        StringBuilder sbErrorStream = new StringBuilder();

        try {
            InputStream in = this.checked_process.getInputStream();
            InputStream inErrors = this.checked_process.getErrorStream();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
            BufferedReader inReaderErrors = new BufferedReader(new InputStreamReader(inErrors));
            LOG.trace("Started retrieving data from streams of attached process: " + this.checked_process);
            long lastStreamDataTime = System.currentTimeMillis();
            long unconditionalExitTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(30L, TimeUnit.MINUTES);
            char[] charBuffer = new char[300];
            boolean outputProduced = true;

            while(isRunning(this.checked_process) || outputProduced) {
                outputProduced = false;
                sleep(100L, false);

                StringBuilder tempSB;
                int readCount;
                for(tempSB = new StringBuilder(); inReader.ready(); lastStreamDataTime = System.currentTimeMillis()) {
                    tempSB.setLength(0);
                    readCount = inReader.read(charBuffer, 0, 300);
                    if (readCount < 1) {
                        break;
                    }

                    tempSB.append(charBuffer, 0, readCount);
                    sbInStream.append(tempSB);
                    if (tempSB.length() > 0) {
                        outputProduced = true;
                    }
                }

                String temp;
                for(tempSB = new StringBuilder(); inReaderErrors.ready(); lastStreamDataTime = System.currentTimeMillis()) {
                    tempSB.setLength(0);
                    readCount = inReaderErrors.read(charBuffer, 0, 300);
                    if (readCount < 1) {
                        break;
                    }

                    tempSB.append(charBuffer, 0, readCount);
                    sbErrorStream.append(tempSB);
                    if (tempSB.length() > 0) {
                        outputProduced = true;
                        temp = new String(tempSB);
                        temp = temp.replaceAll("Pseudo-terminal will not be allocated because stdin is not a terminal.", "");
                        if (this.printToConsole && !temp.trim().equals("")) {
                            if (!temp.toLowerCase().contains("error") && !temp.toLowerCase().contains("failed")) {
                                LOG.debug(temp.trim());
                            } else if (!temp.trim().contains("Error: Nothing to do")) {
                                LOG.warn(temp.trim());
                            }
                        }
                    }
                }

                if (System.currentTimeMillis() - lastStreamDataTime > this.silenceTimeout || System.currentTimeMillis() > unconditionalExitTime) {
                    LOG.info("Conditions: " + (System.currentTimeMillis() - lastStreamDataTime > this.silenceTimeout) + " " + (System.currentTimeMillis() > unconditionalExitTime));
                    this.checked_process.destroy();

                    try {
                        if (System.currentTimeMillis() > unconditionalExitTime) {
                            LOG.error("!@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Unconditional exit occured@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@!\nsome process hag up for more than 30 minutes.");
                        }

                        LOG.error("!##################################!");
                        StringWriter sw = new StringWriter();
                        (new Exception(String.format("Exited from buildOutputAndErrorStreamData by timeoutOutput: %s\nErrors: %s", sbInStream.toString(), sbErrorStream.toString()))).printStackTrace(new PrintWriter(sw));
                        temp = sw.toString();
                        LOG.error(temp);
                    } catch (Exception var20) {
                    }
                    break;
                }
            }

            in.close();
            inErrors.close();
        } finally {
            this.outPutStream = sbInStream.toString();
            this.errorStream = sbErrorStream.toString();
            this.returnCode = this.checked_process.exitValue();
        }

    }

    public static void sleep(long millis, boolean logOutput) {
        if (logOutput) {
            LOG.info("Starting sleeping for " + millis / 1000L + " seconds...");
            LOG.info("Caller: " + Thread.currentThread().getStackTrace()[2]);
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }

        if (logOutput) {
            LOG.info("Finished.");
        }

    }

    private static String removeUnneccessaryOutput(String inputString) {
        inputString = inputString.replace("Pseudo-terminal will not be allocated because stdin is not a terminal.", "");
        inputString = inputString.replace("######## Hortonworks #############\nThis is MOTD message, added for testing in qe infra\n", "");
        inputString = inputString.replace("Welcome to Ubuntu 14.04.3 LTS (GNU/Linux 3.19.0-43-generic x86_64)\n\n * Documentation:  https://help.ubuntu.com/\n\n  Get cloud support with Ubuntu Advantage Cloud Guest:\n    http://www.ubuntu.com/business/services/cloud\n\n\n", "");
        return inputString;
    }
}
