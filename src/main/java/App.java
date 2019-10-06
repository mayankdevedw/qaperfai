import com.qaperf.ai.base.TestAction;
import com.qaperf.ai.parser.ThreadMonitor;
import nu.pattern.OpenCV;
import org.openqa.selenium.*;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        OpenCV.loadLocally();
        TestAction action = new TestAction();
        WebDriver driver= action.openBrowser();
        action.beginTransaction();
        action.beginStep("Navigate to URL");

         driver.navigate().to("");
        action.endStep();
       // driver.manage().window().maximize();
        Thread.sleep(5000);
        action.beginStep("Enter Username and password");
        action.setWebPageName("LoginPage");
        action.findElement(By.name("username_")).sendKeys("");
        driver.findElement(By.name("password")).sendKeys("");
        action.startTestAction("click login",driver);
        driver.findElement(By.id("login")).click();
        action.measureActionPerformance(driver);
        action.endTestAction();
        action.endStep();

        action.endTransaction();
        System.out.println(System.currentTimeMillis());
        while(ThreadMonitor.getActiveThread() != 0)
        {
            System.out.println(ThreadMonitor.getActiveThread());
        }
        driver.quit();

    }
}
