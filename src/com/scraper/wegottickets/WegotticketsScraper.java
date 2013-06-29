package com.scraper.wegottickets;
import com.scraper.Config;
import com.scraper.Scraper;
import com.scraper.wegottickets.items.Event;
import com.scraper.wegottickets.items.EventList;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class WegotticketsScraper extends Scraper {

    private EventList eventList;
    private static int maxPages = 5;

    public WegotticketsScraper() {
        setStartUrl("http://www.wegottickets.com/searchresults/page/1/all");
        eventList = new EventList();
    }

    @Override
    public void Start() {
        ScrapeSearch(getStartUrl(), 1);
        SaveEventsToXml();
    }

    /**
     * Scrape the search page
     * @param url
     * @param pageCount
     */
    private void ScrapeSearch(String url, int pageCount) {
        Document page = GetUrl(url);

        // Loop .ListingWhite for .ListingAct
        Elements listingDivs = page.select(".ListingWhite");

        for(Element parentDiv : listingDivs) {
            // Check price doesn't have a 'FestTicketInfo' link
            Element festTicketCheck = parentDiv.select(".FestTicketInfo").first();
            if(festTicketCheck == null) {
                Element listingDiv = parentDiv.select(".ListingAct").first();
                // Pass on to parse
                ParseListingDiv(listingDiv);
            }
        }

        // Check for more pages
        Element nextPageLink = page.select(".pagination_current").first().nextElementSibling();
        if(nextPageLink != null) {

            // Hack so we can test the XML output with 5 pages. There are 800+ usually.
            if(pageCount++ > maxPages) {
                logger.info("Reached the maximum number of pages (for a usable example), outputting to XML.");
                return;
            }

            logger.info("Reached end of page, continuing to " + nextPageLink.attr("href"));
            ScrapeSearch(nextPageLink.attr("href"), pageCount++);
        }
    }

    /**
     * Parse listing div row
     * @param listing
     */
    private void ParseListingDiv(Element listing) {
        Element heading = listing.select("h3 a").first();
        String eventUrl = heading.attr("href");

        // Send event URL to the parser to handle the magic. Catch comedy.
        if( ! heading.text().contains("COMEDY")) {
            ParseEvent(eventUrl);
        }
    }

    /**
     * Parse event page
     * @param url
     */
    private void ParseEvent(String url) {
        Event event = new Event();
        Document page = GetUrl(url);

        logger.info("Parsing event " + page.select("#Page h1").first().text());

        // URL
        event.setUrl(url);

        // Artist
        event.setArtist(page.select("#Page h1").first().text());

        // Support
        Element support = page.select(".support").first();
        if(support != null)
            event.setSupport(support.text());

        // Date
        event.setDate(page.select(".VenueDetails h2").first().text());

        // City
        event.setCity(page.select(".venuetown").first().text().replace(":", "").trim());

        // Check stock for price
        Element stock = page.select(".stockAvailable").first();
        if(stock != null) {
            String priceText = stock.select("strong").first().text().replace("£", "");
            event.setPrice(new BigDecimal(priceText));
        }

        // Venue
        event.setVenue(page.select(".venuename").first().text());

        // Add to list
        eventList.add(event);
    }

    /**
     * Output event list XML to file
     */
    private void SaveEventsToXml() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("event", Event.class);
        xstream.alias("events", EventList.class);
        xstream.addImplicitCollection(EventList.class, "list");

        String xml = xstream.toXML(eventList);

        try {
            java.util.Date date= new java.util.Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Config.get("eventXmlPath") + "events_" + sdf.format(date).toString() + ".xml"));
            out.writeObject(xml);
            out.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
