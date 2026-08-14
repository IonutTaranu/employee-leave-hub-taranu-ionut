package ro.leavehub.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class RomanianHolidayServiceTest {

    private final RomanianHolidayService service = new RomanianHolidayService();

    @Test
    void excludesWeekendsAndRomanianPublicHolidays() {
        assertThat(service.workingDays(LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 2)))
                .isEqualTo(1);
    }

    @Test
    void includesOrthodoxEasterHolidays() {
        var holidays = service.holidays(2026);
        assertThat(holidays)
                .contains(LocalDate.of(2026, 4, 10))
                .contains(LocalDate.of(2026, 4, 13));
    }

    @Test
    void rejectsPeriodsWithoutWorkingDays() {
        assertThatThrownBy(() -> service.workingDays(
                LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 16)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("nicio zi lucratoare");
    }
}
