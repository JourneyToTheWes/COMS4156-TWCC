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

@RestController
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceExt userDetailsService;

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

    @GetMapping("/events/statistics")
    public ResponseEntity<?> getEventStatistics() {
        EventStatistics eventStats = new EventStatistics();
        List<Event> events = eventRepository.findAll();
        
        // Parse by category
        Map<String, Integer> eventsByCategory = new HashMap<String, Integer>();
        for (Event event: events) {
            if (event.getCategories() != null) {
                String[] categories = event.getCategories().split(",");
    
                for (int i = 0; i < categories.length; i++) {
                    String category = categories[i];
    
                    if (eventsByCategory.containsKey(category)) {
                        eventsByCategory.put(category, eventsByCategory.get(category) + 1);
                    } else {
                        eventsByCategory.put(category, 1);
                    }
                }
            }
        }

        eventStats = eventStats.setEventsByCategory(eventsByCategory);

        // return eventStats.setEventsByCategory(eventsByCategory);
        return ResponseEntity.ok().body(eventStats);
    }
}



