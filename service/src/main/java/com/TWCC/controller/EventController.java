package com.TWCC.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.TWCC.data.Event;
import com.TWCC.data.EventStatistics;
import com.TWCC.exception.InvalidRequestException;
import com.TWCC.repository.EventRepository;
import com.TWCC.security.JwtUtils;
import com.TWCC.security.UserDetailsExt;
import com.TWCC.security.UserDetailsServiceExt;
import com.TWCC.service.EventStatisticService;

@RestController
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceExt userDetailsService;

    @Autowired
    private EventStatisticService eventStatisticService;

    @GetMapping("/hello")
    public String hello() {
        return "Hi there";
    }

    @GetMapping("/events")
    public List<Event> getEvents() {
        System.out.println("getEvents() is called");
        return eventRepository.findAll();
    }

    @GetMapping("/events/{id}")
    public Optional<Event> getEventsById(@PathVariable final Integer id) {

        Optional<Event> result = eventRepository.findById(id);

        if (result == null) {
            throw new InvalidRequestException("Event ID: "
                    + id + " does not exist");
        }

        return result;
    }

    @GetMapping("/events/byaddress/{address}")
    public List<Event> getEventsByAddress(@PathVariable final String address) {
        return eventRepository.findByAddress(address);
    }

    @GetMapping("/events/beforedate/{date}")
    public List<Event> getEventsBeforeDate(@PathVariable final String date) {
        return null;
    }

    @PostMapping("/events")
    public Event createEvent(@RequestBody final Event newEvent, @RequestHeader (name="Authorization") String jwt) {
        // Get user details from JWT and set host Id to new event
        String username = jwtUtils.getUserNameFromJwtToken(jwt.substring("Bearer ".length()));
        UserDetailsExt userDetails = (UserDetailsExt) userDetailsService.loadUserByUsername(username);
        newEvent.setHost(userDetails.getId());
        System.out.println("new event: " + newEvent.toString());
        return eventRepository.save(newEvent);
    }

    @PutMapping("/events")
    public Event updateEvent(@RequestBody Event eventRecord) throws NotFoundException {
        Optional<Event> optionalEvent = eventRepository.findById(eventRecord.getId());
        if (optionalEvent.isEmpty()) {
            throw new NotFoundException();
        }

        Event existingEvent = optionalEvent.get();

        existingEvent.setAddress(eventRecord.getAddress());
        existingEvent.setAgeLimit(eventRecord.getAgeLimit());
        existingEvent.setCost(eventRecord.getCost());
        existingEvent.setDescription(eventRecord.getDescription());
        existingEvent.setEndTimestamp(eventRecord.getEndTimestamp());
        existingEvent.setLatitude(eventRecord.getLatitude());
        existingEvent.setLongitude(eventRecord.getLongitude());
        existingEvent.setMedia(eventRecord.getMedia());
        existingEvent.setName(eventRecord.getName());
        existingEvent.setStartTimestamp(eventRecord.getStartTimestamp());

        return eventRepository.save(existingEvent);
    }

    @DeleteMapping("/events/{eventId}")
    public void deleteEventById(@PathVariable(value = "eventId") Integer eventId) throws NotFoundException{
        if (eventRepository.findById(eventId).isEmpty()){
            throw new NotFoundException();
        }
        eventRepository.deleteById(eventId);
    }

    /**
     * Gets various statistics about TWCC events including:
     * total number of events, number of events by category,
     * average age limit for events, average age limit of events
     * by category, average cost for events, average cost of
     * events by category, and number of events by category in
     * multiple time ranges
     * 
     * @return ResponseEntity containing various TWCC event statistics
     */
    @GetMapping("/events/statistics")
    public ResponseEntity<?> getEventStatistics() {
        EventStatistics eventStats = new EventStatistics();
        List<Event> events = eventRepository.findAll();
        
        eventStats = eventStats
            .setTotalNumberOfEvents(
                events.size()
            ).setNumberOfEventsByCategory(
                eventStatisticService.getNumberOfEventsByCategory(events)
            ).setAverageAgeLimitForEvents(
                eventStatisticService.getAverageAgeLimitForEvents(events)
            ).setAverageAgeLimitOfEventsByCategory(
                eventStatisticService.getAverageAgeLimitOfEventsByCategory(events)
            ).setAverageCostForEvents(
                eventStatisticService.getAverageCostForEvents(events)
            ).setAverageCostOfEventsByCategory(
                eventStatisticService.getAverageCostOfEventsByCategory(events)
            ).setNumberOfEventsByCategoryTimeRanges(
                eventStatisticService.getNumberOfEventsByCategoryTimeRanges(events)
            );

        return ResponseEntity.ok().body(eventStats);
    }
}



