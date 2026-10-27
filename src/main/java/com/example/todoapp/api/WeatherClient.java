package com.example.todoapp.api;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WeatherClient {

    private static final String WEATHER_API_URL = "https://api.open-meteo.com";
    private static final double UENO_LATITUDE = 35.71063;
    private static final double UENO_LONGITUDE = 139.77536;

    private final RestClient restClient = createRestClient();

    public WeatherResult getWeather() {
        try {
            ForecastResponse forecast = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", UENO_LATITUDE)
                            .queryParam("longitude", UENO_LONGITUDE)
                            .queryParam("daily", "weather_code,temperature_2m_min,temperature_2m_max")
                            .queryParam("timezone", "Asia/Tokyo")
                            .queryParam("past_days", 92)
                            .queryParam("forecast_days", 16)
                            .build())
                    .retrieve()
                    .body(ForecastResponse.class);

            if (forecast == null || forecast.daily() == null || forecast.daily().time() == null) {
                return new WeatherResult(Map.of(), true);
            }

            DailyForecast daily = forecast.daily();
            Map<String, DailyWeather> weatherByDate = new LinkedHashMap<>();
            int count = Math.min(daily.time().size(), Math.min(
                    sizeOf(daily.weatherCodes()), Math.min(sizeOf(daily.temperatureMin()), sizeOf(daily.temperatureMax()))));
            for (int index = 0; index < count; index++) {
                int code = daily.weatherCodes().get(index);
                weatherByDate.put(daily.time().get(index), new DailyWeather(
                        code, weatherLabel(code), weatherIcon(code),
                        daily.temperatureMin().get(index), daily.temperatureMax().get(index)));
            }
            return new WeatherResult(weatherByDate, false);
        } catch (RestClientException exception) {
            return new WeatherResult(Map.of(), true);
        }
    }

    private static int sizeOf(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private static String weatherLabel(int code) {
        return switch (code) {
            case 0 -> "快晴";
            case 1 -> "晴れ";
            case 2 -> "一部曇り";
            case 3 -> "曇り";
            case 45, 48 -> "霧";
            case 51, 53, 55, 56, 57 -> "霧雨";
            case 61, 63, 65, 66, 67 -> "雨";
            case 71, 73, 75, 77 -> "雪";
            case 80, 81, 82 -> "にわか雨";
            case 85, 86 -> "にわか雪";
            case 95, 96, 99 -> "雷雨";
            default -> "天気情報";
        };
    }

    private static String weatherIcon(int code) {
        return switch (code) {
            case 0 -> "☀️";
            case 1 -> "🌤️";
            case 2 -> "⛅";
            case 3 -> "☁️";
            case 45, 48 -> "🌫️";
            case 51, 53, 55, 56, 57 -> "🌦️";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️";
            case 71, 73, 75, 77, 85, 86 -> "🌨️";
            case 95, 96, 99 -> "⛈️";
            default -> "🌡️";
        };
    }

    private static RestClient createRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .baseUrl(WEATHER_API_URL)
                .requestFactory(requestFactory)
                .build();
    }

    public record WeatherResult(Map<String, DailyWeather> weather, boolean unavailable) {
    }

    public record DailyWeather(int code, String description, String icon,
            double temperatureMin, double temperatureMax) {
    }

    public record ForecastResponse(DailyForecast daily) {
    }

    public record DailyForecast(
            List<String> time,
            @JsonProperty("weather_code") List<Integer> weatherCodes,
            @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax) {
    }
}
