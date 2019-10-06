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
        driver.get("http://www.ankpro.com");

       driver.navigate().to("https://34.222.81.56:8443/auth/realms/cdpe2e-dflt-acnt-mow-dev-realm/protocol/saml/clients/samlclient");
        action.endStep();
       // driver.manage().window().maximize();
        Thread.sleep(5000);
        action.beginStep("Enter Username and password");
        action.setWebPageName("LoginPage");
        action.findElement(By.name("username_")).sendKeys("cdpe2edevuser@keycloak.com");
        driver.findElement(By.name("password")).sendKeys("password");
        action.startTestAction("click login",driver);
        driver.findElement(By.id("kc-login")).click();
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
