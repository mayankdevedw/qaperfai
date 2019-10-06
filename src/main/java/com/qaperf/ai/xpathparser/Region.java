package com.qaperf.ai.xpathparser;

public class Region{

    public String xpath;
    public String xpathFull;

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public Region(){

    }

    public Region(String xpath, String xpathFull, String label, String suffix, String prefix) {
        this.xpath = xpath;
        this.xpathFull = xpathFull;
        this.label = label;
        this.suffix = suffix;
        this.prefix = prefix;
    }

    public String getXpathFull() {
        return xpathFull;
    }

    public void setXpathFull(String xpathFull) {
        this.xpathFull = xpathFull;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String label;
    public String suffix;
    public String prefix;
}