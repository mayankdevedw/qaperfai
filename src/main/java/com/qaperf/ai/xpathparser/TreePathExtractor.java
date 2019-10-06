package com.qaperf.ai.xpathparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaperf.ai.learn.ElementStructure;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.openqa.selenium.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public class TreePathExtractor {
    static Logger LOG= Logger.getLogger(TreePathExtractor.class);
    private org.dom4j.Element getElement(JtidyDom4jXPathReader der, String xpath) throws Exception {

        org.dom4j.Node result = der.getNode(xpath);

        if (result instanceof Element)
            return (Element) result;
        else
            return null;

    }


    public Element train(DataModel datamodel) throws Exception {
        JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();;
        der.setHtml(datamodel.getHtml());
       return getElement(der, datamodel.getRegion().getXpath());
    }

    public String extract(By by, String fileName) {

//read json file data to String
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get(fileName));
            //create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            //convert json string to object
            ElementStructure emp = objectMapper.readValue(jsonData, ElementStructure.class);

            return emp.getUniquePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public WebElement checkIfElementExists(WebDriver driver, String xpath){
        try{
          return   driver.findElement(By.xpath(xpath));
        }
        catch (NoSuchElementException ex){
            return null;
        }


    }

    public static String suggestNewXpath(WebDriver driver, WebElement targetElement, By deprecatedElementLocator){

        // attempt to find an optimal xpath for the targerElement
        int maximumXpathNodes = 6;
        String newXpath = "";
        for (int i = 0; i < maximumXpathNodes; i++) {
            String xpathFindingAlgorithm = JSHelpers.GET_XPATH.getValue();
            /**
             * $$GetIndex$$ $$GetId$$ $$GetName$$ $$GetType$$ $$GetClass$$ $$GetText$$
             * $$MaxCount$$
             */
            String maxCount = String.valueOf(i);
            String getId = String.valueOf(true);
            String getIndex;
            String getName;
            String getType;
            String getClass;
            String getText;
            getIndex = getName = getType = getClass = getText = String.valueOf(false);

            if (i == 0) {
                maxCount = String.valueOf(1);
            } else if (i == 1 || i == 2) {
                getName = String.valueOf(true);
                getType = String.valueOf(true);
                getText = String.valueOf(true);
            } else if (i == 3 || i == 4) {
                getName = String.valueOf(true);
                getType = String.valueOf(true);
                getClass = String.valueOf(true);
                getText = String.valueOf(true);
            } else {
                getIndex = String.valueOf(true);
                getName = String.valueOf(true);
                getType = String.valueOf(true);
                getText = String.valueOf(true);
                getClass = String.valueOf(true);
            }

            xpathFindingAlgorithm = xpathFindingAlgorithm.replaceAll("\\$\\$MaxCount\\$\\$", maxCount)
                    .replaceAll("\\$\\$GetId\\$\\$", getId).replaceAll("\\$\\$GetIndex\\$\\$", getIndex)
                    .replaceAll("\\$\\$GetName\\$\\$", getName).replaceAll("\\$\\$GetType\\$\\$", getType)
                    .replaceAll("\\$\\$GetClass\\$\\$", getClass).replaceAll("\\$\\$GetText\\$\\$", getText);

            try {
                newXpath = (String) ((JavascriptExecutor) driver).executeScript(xpathFindingAlgorithm, targetElement);
                if (newXpath != null && driver.findElements(By.xpath(newXpath)).size() == 1) {
                    // if unique element was found, break, else keep iterating
                    break;
                }
            } catch (JavascriptException e) {
                LOG.error(e);
                LOG.info("Failed to suggest a new XPath for the target element with this deprecated locator ["
                        + deprecatedElementLocator + "]");
            }
        }
        if (newXpath != null) {

            LOG.info("New AI-Suggested XPath [" + newXpath.replace("\"", "'") + "]");

            return newXpath;
        } else {
            LOG.error("Failed to suggest a new XPath for the target element with this deprecated locator ["

                    + deprecatedElementLocator + "]");
            return null;
        }
    }



}
