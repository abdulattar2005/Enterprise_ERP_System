package com.erp.hr.dto;

import com.erp.hr.domain.Attendance;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceDto(Long id, Long employeeId, LocalDate workDate, LocalTime checkIn, LocalTime checkOut,
                            BigDecimal hoursWorked) {

    public static AttendanceDto from(Attendance a) {
        BigDecimal hours = BigDecimal.ZERO;
        if (a.getCheckIn() != null && a.getCheckOut() != null) {
            long minutes = Duration.between(a.getCheckIn(), a.getCheckOut()).toMinutes();
            hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }
        return new AttendanceDto(a.getId(), a.getEmployee().getId(), a.getWorkDate(), a.getCheckIn(),
                a.getCheckOut(), hours);
    }
}
