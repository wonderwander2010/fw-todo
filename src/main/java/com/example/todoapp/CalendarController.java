package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month, Model model) {
        YearMonth selectedMonth = (year == null || month == null)
                ? YearMonth.now()
                : YearMonth.of(year, month);
        LocalDate firstDay = selectedMonth.atDay(1);
        LocalDate lastDay = selectedMonth.atEndOfMonth();

        List<LocalDate> dates = new ArrayList<>();
        int leadingEmptyDays = firstDay.getDayOfWeek().getValue() % 7;
        int totalCells = ((leadingEmptyDays + selectedMonth.lengthOfMonth() + 6) / 7) * 7;
        for (int cell = 0; cell < totalCells; cell++) {
            int day = cell - leadingEmptyDays + 1;
            dates.add(day >= 1 && day <= selectedMonth.lengthOfMonth()
                    ? selectedMonth.atDay(day)
                    : null);
        }

        YearMonth previousMonth = selectedMonth.minusMonths(1);
        YearMonth nextMonth = selectedMonth.plusMonths(1);
        model.addAttribute("year", selectedMonth.getYear());
        model.addAttribute("month", selectedMonth.getMonthValue());
        model.addAttribute("dates", dates);
        model.addAttribute("from", firstDay);
        model.addAttribute("to", lastDay);
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        return "calendar";
    }
}
