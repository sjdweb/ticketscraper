package com.scraper;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public abstract class Scraper {

    protected String startUrl;

    protected static int jsoupTimeout = 6000;
    protected static Logger logger = Logger.getLogger(Scraper.class);

    public abstract void Start();

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    /**
     * Load a URL via Jsoup and return the Document object.
     * @param url
     * @return
     */
    public Document GetUrl(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .userAgent(Config.get("userAgent"))
                    .timeout(jsoupTimeout)
                    .get();
        }
        catch(IOException e) {
            logger.error("Unable to load " + url + " Reason: " + e.getMessage());
        }
        return doc;
    }
}
