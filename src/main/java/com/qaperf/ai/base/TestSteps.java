package com.qaperf.ai.base;

import org.openqa.selenium.WebDriver;

public interface TestSteps {

    public void beginStep(String name);
    public void endStep();
    public void beginTransaction();
    public void endTransaction();
    public WebDriver openBrowser();
}
