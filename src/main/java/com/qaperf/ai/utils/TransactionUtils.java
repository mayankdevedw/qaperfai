package com.qaperf.ai.utils;

import com.qaperf.ai.base.CommonBeans;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;


public class TransactionUtils {
    OSBeans osName =null;
    static Logger LOG=Logger.getLogger(TransactionUtils.class);
    public  String generateRefnumber(int length) {

        StringBuffer buffer = new StringBuffer();
        String characters = "";
        characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        int charactersLength = characters.length();

        for (int i = 0; i < length; i++) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();

    }

    private OSBeans checkOSType(){
        if(osName != null)
            return osName;

        osName = new OSBeans();
        String str = System.getProperty("os.name");
        LOG.debug(String.format("OS is %s", str));
        if(OSLocator.isMac(str)){
            osName.setMac(true);
            osName.setDirSeparator("/");
        }
        else if(OSLocator.isUnix(str)){
            osName.setUnix(true);
            osName.setDirSeparator("/");
        }
        else if(OSLocator.isWindows(str)){
            osName.setWindows(true);
            osName.setDirSeparator("\\");
        }
        return osName;
    }

    public void createDirectory(String dirName, CommonBeans cm){

        dirName = cm.getCurrentDir()+dirName;
        new File(dirName).mkdir();
    }

    private int countFilesInDirectory(File directory, String fileExtension) throws IOException {
        File dir = new File(directory.getAbsolutePath());
        if(!dir.exists()){
            if(!dir.mkdir()){
                IOException e = new IOException("Unable to create directory");
                throw e;
            }
        }
        String[] fileNames = directory.list();
        int total = 0;
        for (int i = 0; i< fileNames.length; i++)
        {
            if (fileNames[i].contains(fileExtension))
            {
                total++;
            }
        }
        return total;
    }

    public  boolean createDirectory(String dirPath){
        boolean result=false;
        File theDir = new File(dirPath);

        if (!theDir.exists()) {
            LOG.debug("creating directory: " + dirPath);

            result = false;

            try{
                theDir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
                LOG.debug("DIR created");
            }
        }
        return result;
    }
    private   boolean checkIfNewFileIsSmallerThanExisting(String newFile, String existingFile ){
        File f1 = new File(newFile);
        File f2 = new File(existingFile);

        double megaBytes1 = 0, megaBytes2 = 0;

        if(f1.exists()){
            double bytes1 = f1.length();
            megaBytes1 = bytes1/ (1024*1024);
        }

        if(f1.exists()){
            double bytes2 = f2.length();
            megaBytes2 = bytes2/ (1024*1024);
        }
        LOG.debug("New File:" + newFile + ", size:" + megaBytes1);
        LOG.debug("Existing File:" + existingFile + ", size:" + megaBytes2);

        if(megaBytes1<=megaBytes2){
            return true;
        }
        else{
            if((megaBytes1-megaBytes2)<=megaBytes2){
                return true;
            }
            return false;
        }
    }

    public String captureLogInFile(WebDriver driver, String fileName) throws IOException{
        if(osName == null){
            osName = checkOSType();
        }
        Logs logs = driver.manage().logs();

        List<LogEntry> perfLogEntries = logs.get(LogType.PERFORMANCE).getAll();

        saveRawNetwrokDump(perfLogEntries);

        return saveNetworkDumpDelta(fileName + "_" + generateRefnumber(4));

    }

    private void saveRawNetwrokDump(List<LogEntry> perfLogEntries) throws IOException{

       LOG.debug("Saving Network dump...");
        String tmpDirPath = System.getProperty("user.dir") + osName.getDirSeparator() + "networkdumpTmp";
        createDirectory(tmpDirPath);
        File tmpDir = new File(tmpDirPath);
        int fileCount = countFilesInDirectory(tmpDir, ".json");
        String tmpFileName = "networkDump_" + (fileCount+1) + ".json";
        File file = new File(tmpDir + osName.getDirSeparator() + tmpFileName);
        PrintWriter writer = new PrintWriter(file);
        file.createNewFile();
        for (LogEntry entry : perfLogEntries) {
            writer.println(entry.getMessage());
        }
        writer.flush();
        writer.close();
    }

    private String saveNetworkDumpDelta(String fileName) throws IOException {
        LOG.debug("Getting Deltas for 2 network dumps...");
        String tmpDirPath = System.getProperty("user.dir") + osName.getDirSeparator() + "networkdumpTmp" + osName.getDirSeparator();

        File tmpDir = new File(tmpDirPath);
        int fileCount = countFilesInDirectory(tmpDir, ".json");
        LOG.debug("FileCount::" + fileCount);
        String newFile =  fileName + ".json";

        if(fileCount<=1){
            LOG.debug("No or 1 file available");
            String tmpFileName1 = "networkDump_1.json";
            FileUtils.copyFile(new File(tmpDirPath + tmpFileName1), new File(newFile));
        }
        else if(checkIfNewFileIsSmallerThanExisting(tmpDirPath+"networkDump_" + (fileCount) + ".json", tmpDirPath+"networkDump_" + (fileCount-1) + ".json" )){
            LOG.debug("New network dump file is for 1 page");
            String tmpFileName1 = "networkDump_" + (fileCount) + ".json";
            FileUtils.copyFile(new File(tmpDirPath + tmpFileName1), new File(newFile));
        }
        else{
            LOG.debug("New network dump may contain last network dump data");
            LOG.debug("New network dump file is for 1 page");
            String tmpFileName1 = "networkDump_" + (fileCount) + ".json";
            FileUtils.copyFile(new File(tmpDirPath + tmpFileName1), new File(newFile));
        }
        return newFile;
    }

    public  String formatElementLocatorToPath(By elementLocator) {
        StackTraceElement[] callingStack = Thread.currentThread().getStackTrace();
        String className = "";
        for (int i = 1; i < callingStack.length; i++) {
            if (!callingStack[i].getClassName().contains("com.qaperf.ai")) {
                className = callingStack[i].getClassName();
                break;
            }
        }

        String elementFileName = className + "_" + elementLocator.toString();
        return elementFileName.replaceAll("[\\[\\]\\'\\/:]", "").replaceAll("[\\W\\s]", "_").replaceAll("_{2}", "_")
                .replaceAll("_{2}", "_").replaceAll("contains", "_contains").replaceAll("_$", "");
    }

    public  String getTestFileName() {
        StackTraceElement[] callingStack = Thread.currentThread().getStackTrace();
        String className = "";
        for (int i = 1; i < callingStack.length; i++) {
            if (!callingStack[i].getClassName().contains("com.qaperf.ai")) {
                className = callingStack[i].getClassName();
                break;
            }
        }
        return className;

    }


    /**
     * Save the GUI of the current driver instance in a file name identified by
     * filename.
     *
     * @param d
     * @param filename
     * @throws AWTException
     * @throws HeadlessException
     * @throws IOException
     */
    public static void saveScreenshot(WebDriver d, String filename) {

        File screenshot = ((TakesScreenshot) d).getScreenshotAs(OutputType.FILE);
        File destFile = new File(filename);

        try {
            FileUtils.copyFile(screenshot, destFile);
            screenshot = destFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
