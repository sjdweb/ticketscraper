package com.scraper;

import com.scraper.wegottickets.WegotticketsScraper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        PropertyConfigurator.configure(Config.get("loggingConfigPath"));
        logger.debug("Scraper Launching...");

        WegotticketsScraper wgt = new WegotticketsScraper();
        wgt.Start();
    }
}
