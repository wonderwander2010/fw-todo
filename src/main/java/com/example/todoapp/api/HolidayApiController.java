package com.example.todoapp.api;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController(HolidayClient holidayClient) {
        this.holidayClient = holidayClient;
    }

    @GetMapping("/api/holidays")
    public ResponseEntity<Map<String, String>> holidays(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        HolidayClient.HolidayResult result = holidayClient.getHolidays();
        Map<String, String> holidays = result.holidays().entrySet().stream()
                .filter(entry -> from == null || entry.getKey().compareTo(from) >= 0)
                .filter(entry -> to == null || entry.getKey().compareTo(to) <= 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.unavailable()) {
            response.header("X-Holidays-Unavailable", "true");
        }
        return response.body(holidays);
    }
}
