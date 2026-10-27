package com.example.todoapp.api;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherApiController {

    private final WeatherClient weatherClient;

    public WeatherApiController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @GetMapping("/api/weather")
    public ResponseEntity<Map<String, WeatherClient.DailyWeather>> weather(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        WeatherClient.WeatherResult result = weatherClient.getWeather();
        Map<String, WeatherClient.DailyWeather> weather = result.weather().entrySet().stream()
                .filter(entry -> from == null || entry.getKey().compareTo(from) >= 0)
                .filter(entry -> to == null || entry.getKey().compareTo(to) <= 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.unavailable()) {
            response.header("X-Weather-Unavailable", "true");
        }
        return response.body(weather);
    }
}
