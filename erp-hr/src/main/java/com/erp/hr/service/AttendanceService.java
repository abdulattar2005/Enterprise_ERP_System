package com.erp.hr.service;

import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.hr.domain.Attendance;
import com.erp.hr.domain.Employee;
import com.erp.hr.domain.EmployeeStatus;
import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.repository.AttendanceRepository;
import com.erp.hr.repository.EmployeeRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    public AttendanceService(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository,
                             Clock clock) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.clock = clock;
    }

    public AttendanceDto checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Employee", employeeId));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessRuleException("Only ACTIVE employees can check in");
        }
        LocalDate today = LocalDate.now(clock);
        if (attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).isPresent()) {
            throw new BusinessRuleException("Employee already checked in today");
        }
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setWorkDate(today);
        attendance.setCheckIn(LocalTime.now(clock).truncatedTo(ChronoUnit.SECONDS));
        return AttendanceDto.from(attendanceRepository.save(attendance));
    }

    public AttendanceDto checkOut(Long employeeId) {
        LocalDate today = LocalDate.now(clock);
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BusinessRuleException("Employee has not checked in today"));
        if (attendance.getCheckOut() != null) {
            throw new BusinessRuleException("Employee already checked out today");
        }
        attendance.setCheckOut(LocalTime.now(clock).truncatedTo(ChronoUnit.SECONDS));
        return AttendanceDto.from(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> history(Long employeeId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessRuleException("'to' must not be before 'from'");
        }
        return attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDate(employeeId, from, to)
                .stream().map(AttendanceDto::from).toList();
    }
}
