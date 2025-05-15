package com.mongodbmodfactory.kitchensink_boot.helpers;

import org.springframework.context.event.EventListener;
import org.springframework.data.rest.core.event.AfterCreateEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestEventListener {

    final List<AfterCreateEvent> afterCreateEvents = new ArrayList<>();

    @EventListener
    void onAfterCreateEvent(AfterCreateEvent event) {
        afterCreateEvents.add(event);
    }

    public void reset() {
        afterCreateEvents.clear();
    }

    public List<AfterCreateEvent> getAfterCreateEvents() {
        return afterCreateEvents;
    }
}
