package com.scraper.wegottickets.items;

import java.util.ArrayList;
import java.util.List;

public class EventList {
    private List<Event> list;

    public EventList(){
        list = new ArrayList<Event>();
    }

    public void add(Event e){
        list.add(e);
    }
}
