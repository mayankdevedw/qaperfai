package com.qaperf.ai.measure;

import com.qaperf.ai.base.ActionBeans;
import com.qaperf.ai.utils.Config;
import com.qaperf.ai.utils.ConfigLoader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;

public class ScreenShotDriver implements Runnable {
    static Logger LOG=Logger.getLogger(ScreenShotDriver.class);
    private Config config= ConfigLoader.loadConfig();
    private WebDriver driver;
    private String baseLocation;
    private ActionBeans action;
    ScreenShotFlag scFlag;
    public ScreenShotDriver(WebDriver driver, String baseLocation, ActionBeans action, ScreenShotFlag scFlag){
        this.driver=driver;
        this.baseLocation = baseLocation;
        this.action=action;
        this.scFlag=scFlag;
    }

    @Override
    public void run() {
        LOG.debug("Screenshot Driver Started");
        while (scFlag.getScreenShotFlag()){
            measureActionPerformance(this.driver, this.scFlag);
        }
        LOG.debug("Screenshot Driver Ended");
    }

    public void measureActionPerformance(WebDriver driver, ScreenShotFlag scFlag) {

        File scrFile;
        long base_time = action.getActionStartTime();

        scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String base = this.baseLocation + "cap_" + base_time + "_base.png";
        long end_time = action.getActionStartTime() + config.getMaxTime();
        try {
            FileUtils.copyFile(scrFile, new File(base));

            for (int i = 0; i < config.getMaxTime(); i += config.getCaptureInterval()) {
                if (System.currentTimeMillis() < end_time) {
                    if(!scFlag.getScreenShotFlag())
                        break;

                    scrFile = ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.FILE);
                    String filename = this.baseLocation + "cap_"
                            + System.currentTimeMillis() + ".png";
                    LOG.debug("Screenshot Driver File "+filename);
                    FileUtils.copyFile(scrFile, new File(filename));

                    Thread.sleep(config.getCaptureInterval());
                } else {
                    i = config.getMaxTime();
                }

            }
        } catch (InterruptedException | IOException e) {

        }


    }
}
