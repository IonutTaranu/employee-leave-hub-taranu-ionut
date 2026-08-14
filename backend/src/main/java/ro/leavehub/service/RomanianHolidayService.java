package ro.leavehub.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class RomanianHolidayService {

    public int workingDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw ApiException.badRequest("Perioada selectata nu este valida.");
        }
        var holidaysByYear = new java.util.HashMap<Integer, Set<LocalDate>>();
        var count = 0;
        for (var day = start; !day.isAfter(end); day = day.plusDays(1)) {
            var weekend = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
            var holidays = holidaysByYear.computeIfAbsent(day.getYear(), this::holidays);
            if (!weekend && !holidays.contains(day)) {
                count++;
            }
        }
        if (count == 0) {
            throw ApiException.badRequest("Perioada selectata nu contine nicio zi lucratoare.");
        }
        return count;
    }

    public boolean isWorkingDay(LocalDate date) {
        var weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        return !weekend && !holidays(date.getYear()).contains(date);
    }

    public Set<LocalDate> holidays(int year) {
        var result = new HashSet<LocalDate>();
        result.add(LocalDate.of(year, 1, 1));
        result.add(LocalDate.of(year, 1, 2));
        result.add(LocalDate.of(year, 1, 6));
        result.add(LocalDate.of(year, 1, 7));
        result.add(LocalDate.of(year, 1, 24));
        result.add(LocalDate.of(year, 5, 1));
        result.add(LocalDate.of(year, 6, 1));
        result.add(LocalDate.of(year, 8, 15));
        result.add(LocalDate.of(year, 11, 30));
        result.add(LocalDate.of(year, 12, 1));
        result.add(LocalDate.of(year, 12, 25));
        result.add(LocalDate.of(year, 12, 26));

        var easter = orthodoxEaster(year);
        result.add(easter.minusDays(2));
        result.add(easter);
        result.add(easter.plusDays(1));
        result.add(easter.plusDays(49));
        result.add(easter.plusDays(50));
        return Set.copyOf(result);
    }

    private LocalDate orthodoxEaster(int year) {
        int a = year % 4;
        int b = year % 7;
        int c = year % 19;
        int d = (19 * c + 15) % 30;
        int e = (2 * a + 4 * b - d + 34) % 7;
        int month = (d + e + 114) / 31;
        int day = ((d + e + 114) % 31) + 1;
        var julianDate = LocalDate.of(year, month, day);
        return julianDate.plusDays(year >= 2100 ? 14 : 13);
    }
}
