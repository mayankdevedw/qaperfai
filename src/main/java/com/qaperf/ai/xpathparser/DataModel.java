package com.qaperf.ai.xpathparser;

import java.util.HashSet;
import java.util.Set;

public class DataModel {
    private String webPage;
    private String html;
    private Region region;

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }




    public String getWebPage() {
        return webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }



    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }


}
