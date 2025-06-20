package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicEventController {

    final EventService eventService;

    @GetMapping
    public List<EventShortDto> findEventsPublic(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
                                                @RequestParam(required = false) List<Integer> categories,
                                                @RequestParam(required = false) Boolean paid,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                LocalDateTime rangeStart,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                @RequestParam(required = false, defaultValue = "EVENT_DATE")
                                                    @Pattern(regexp = "EVENT_DATE|VIEWS",
                                                            message = "Sort must be either 'EVENT_DATE' or 'VIEWS'")
                                                    String sort,
                                                @RequestParam(defaultValue = "0") @Min(value = 0) Integer from,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                HttpServletRequest httpServletRequest) {
        return eventService.findEventsPublic(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size, httpServletRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventByIdPublic(@PathVariable Long eventId, HttpServletRequest httpServletRequest) {
        return eventService.findEventByIdPublic(eventId, httpServletRequest);
    }
}

