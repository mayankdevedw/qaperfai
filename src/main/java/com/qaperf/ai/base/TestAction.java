package com.qaperf.ai.base;

import com.qaperf.ai.dataset.SetDataForWebPage;
import com.qaperf.ai.learn.TreePathStructureLearn;
import com.qaperf.ai.measure.ScreenShotDriver;
import com.qaperf.ai.measure.ScreenShotFlag;
import com.qaperf.ai.parser.ThreadMonitor;
import com.qaperf.ai.parser.PythonParserThread;
import com.qaperf.ai.utils.ComputerVision;
import com.qaperf.ai.utils.Config;
import com.qaperf.ai.utils.ConfigLoader;
import com.qaperf.ai.utils.Settings;
import com.qaperf.ai.xpathparser.DataModel;
import com.qaperf.ai.xpathparser.TreePathExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;


public class TestAction extends StepsImpl {
    TreePathExtractor extractor = new TreePathExtractor();
    ComputerVision vision = new ComputerVision();
    private String webPageName;
    public final static Logger LOG;
    public WebDriver driver;

    public String getWebPageName() {
        return webPageName;
    }

    public String getCurrentElementScreenShot() {
        return currentElementScreenShot;
    }

    public void setCurrentElementScreenShot(String currentElementScreenShot) {
        this.currentElementScreenShot = currentElementScreenShot;
    }

    private String currentElementScreenShot;

    public String getWebPageScreenShot() {
        return webPageScreenShot;
    }

    public void setWebPageScreenShot(String webPageScreenShot) {
        this.webPageScreenShot = webPageScreenShot;
    }

    private String webPageScreenShot;

    private void saveScreenshot(String fileName){
        //Save annotate visual and screenshot

        vision.saveScreenshot(driver, fileName);
    }


    public void setWebPageName(String webPageName) {
        this.webPageName = webPageName;
        utils.createDirectory(config.getBaseDir()+ File.separator +"TestSuites"+File.separator+webPageName);
        String webPageScreenShot = config.getBaseDir()+ File.separator +"TestSuites"+File.separator+webPageName+File.separator+webPageName+".png";
        setWebPageScreenShot(webPageScreenShot);
    }

    static {
        //PropertiesConfigurator is used to configure logger from properties file
        PropertyConfigurator.configure(System.getProperty("user.dir")+File.separator+"log4j.properties");
        LOG=Logger.getLogger(TestAction.class);

    }
    ActionBeans action = null;
    ScreenShotFlag flag=null;
    Config config = ConfigLoader.loadConfig();

    public WebElement findElement(By by){

        WebElement element=null;
        try {
            if (driver instanceof FindsByName) {
                element = this.driver.findElement(by);
                if (config.isTraining()) {
                    trainElement(element, by);
                }

            }


        }
        catch (NoSuchElementException ex){
            LOG.error(String.format("Failed to Find Element %s", by.toString()));
            LOG.info(String.format("Trying to Find Element %s using AI", by.toString()));
            String path = config.getBaseDir() + File.separator + "TestSuites" + File.separator + webPageName + File.separator + utils.formatElementLocatorToPath(by);
            String fileName = path+".json";
            String xpath = extractor.extract(by, fileName);
            LOG.info(String.format("Found Full Element Path %s using Learnt Data", xpath));
            element = extractor.checkIfElementExists(driver, TreePathExtractor.suggestNewXpath(driver, element, by));
            if(element != null){
                LOG.info(String.format("Found Element Using Full Path %s using Learnt Data", TreePathExtractor.suggestNewXpath(driver, element, by)));
                return element;
            }

            String imagePath= path+".png";
            xpath = vision.detect(driver, imagePath, by);
            LOG.info(String.format("Found Element XPath %s using Screenshot ", xpath));
            element = extractor.checkIfElementExists(driver, xpath);
            if(element != null){

                LOG.info(String.format("Found Element Using Sreenshot %s using Learnt Data", xpath));
                return element;
            }
        }

        return element;
    }

    private void trainElement(WebElement element, By by){
        try {
            if (!(new File(getWebPageScreenShot()).exists())){
                saveScreenshot(getWebPageScreenShot());
            }
            String fileName = utils.formatElementLocatorToPath(by);
            setCurrentElementScreenShot(config.getBaseDir() + File.separator + "TestSuites" + File.separator + webPageName + File.separator + fileName);
            vision.saveVisualLocator(driver, getWebPageScreenShot(), element, getCurrentElementScreenShot() + ".png");
            SetDataForWebPage webPage = new SetDataForWebPage(this.driver);
            DataModel model = webPage.createExtractorTree(this.getWebPageName(), String.format("//*[@name='%s']", by.toString().split(":")[1].trim()));
            Element trainedElement = extractor.train(model);
            String arg = String.format( "--first %s --second %s %s", getWebPageScreenShot(), getCurrentElementScreenShot()+".png", Settings.annotated_arg);
            PythonParserThread pythonParserThread= new PythonParserThread(getCurrentElementScreenShot() + "_annotated.png", by.toString()+"_locator",arg, Settings.annotated_file);
            new Thread(pythonParserThread).start();
            ThreadMonitor.increment();
            TreePathStructureLearn learn = new TreePathStructureLearn(trainedElement, model, fileName, getWebPageName());
            new Thread(learn).start();
            ThreadMonitor.increment();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    @Override
    public void startTestAction(String name, WebDriver driver) {

        action = new ActionBeans();
        flag = new ScreenShotFlag();
        action.setActionName(name);
        action.setActionStartTime(System.currentTimeMillis());
        utils.createDirectory( name.replace(' ','_'), cm);
        cm.setActionDir(cm.getStepDir()+name.replace(' ','_'));
        cm.setCurrentDir(cm.getActionDir());
        ScreenShotDriver screenShotDriver=new ScreenShotDriver(driver, cm.getCurrentDir(), action, flag);
        new Thread(screenShotDriver).start();
        flag.setScreenShotFlag(true);


    }



    @Override
    public void endTestAction() {
        action.setActionEndTime(System.currentTimeMillis());
        PythonParserThread parserThread = new PythonParserThread(cm.getActionDir(), action.getActionName(), Settings.datasetArg, Settings.parser_file);
        new Thread(parserThread).start();
        ThreadMonitor.increment();
        saveNetworkDump();
        cm.setCurrentDir(cm.getStepDir());
        flag=null;
        action = null;

    }




    private void saveNetworkDump() {

        try {
           String fileName= utils.captureLogInFile(driver, cm.getCurrentDir()+ File.separator+ action.getActionName().replace(' ','_'));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public WebDriver openBrowser() {
        LOG.info("Starting opening Browser");



        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.DRIVER, Level.INFO);
        logs.enable(LogType.SERVER, Level.FINE);
        logs.enable(LogType.CLIENT, Level.ALL);
        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.PERFORMANCE, Level.ALL);

        ChromeOptions options = new ChromeOptions();
        options.setCapability("platform", Platform.MAC);
        //options.addArguments("window-size=1980,960");
        options.addArguments("screenshot");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-infobars");
        options.setCapability(ChromeOptions.CAPABILITY, options);
        options.setCapability("goog:loggingPrefs", logs);
        options.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
        options.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE, false);
        options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File("/tmp/chromedriver"))
                .usingAnyFreePort()
                .build();
        RemoteWebDriver driver = new ChromeDriver(service, options);
        this.driver=driver;
        return driver;

    }
    public void measureActionPerformance(WebDriver driver) {
        flag.setScreenShotFlag(false);
        File scrFile;
        long end_time = action.getActionStartTime() + config.getMaxTime();
        try {


            for (int i = 0; i < config.getMaxTime(); i += config.getCaptureInterval()) {
                if (System.currentTimeMillis() < end_time) {
                    scrFile = ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.FILE);
                    String filename = cm.getCurrentDir() + "cap_"
                            + System.currentTimeMillis() + ".png";
                    LOG.debug("Test Action Files "+filename);
                    if(new File(filename).exists()){
                        filename=filename+"_copy";
                    }
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
