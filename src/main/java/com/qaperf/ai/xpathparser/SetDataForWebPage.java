package com.qaperf.ai.xpathparser;

import com.qaperf.ai.xpathparser.DataModel;
import com.qaperf.ai.xpathparser.Region;
import org.openqa.selenium.WebDriver;

import java.util.*;

public class SetDataForWebPage {
    private WebDriver driver;
    public SetDataForWebPage(WebDriver driver){
        this.driver = driver;
    }

    public DataModel createExtractorTree(String pageName, String extractors){

        DataModel dataset = new DataModel();
        dataset.setHtml(this.driver.getPageSource());
        dataset.setWebPage(pageName);
        Region r = new Region();
        r.setXpath(extractors);
        dataset.setRegion(r);
        return dataset;

    }



}


