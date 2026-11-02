package com.erp.hr.web;

import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.service.AttendanceService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/attendance")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping("/{employeeId}/check-in")
    public AttendanceDto checkIn(@PathVariable Long employeeId) {
        return service.checkIn(employeeId);
    }

    @PostMapping("/{employeeId}/check-out")
    public AttendanceDto checkOut(@PathVariable Long employeeId) {
        return service.checkOut(employeeId);
    }

    @GetMapping("/{employeeId}")
    public List<AttendanceDto> history(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.history(employeeId, from, to);
    }
}
