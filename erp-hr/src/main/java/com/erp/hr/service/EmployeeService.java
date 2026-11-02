package com.erp.hr.service;

import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.hr.domain.Employee;
import com.erp.hr.domain.EmployeeStatus;
import com.erp.hr.dto.EmployeeRequest;
import com.erp.hr.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;
    private final AuditService audit;

    public EmployeeService(EmployeeRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public Employee create(EmployeeRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("An employee with this email already exists: " + request.email());
        }
        Employee employee = new Employee();
        apply(employee, request);
        Employee saved = repository.save(employee);
        audit.log("CREATE_EMPLOYEE", "Created employee " + saved.getFullName() + " (id=" + saved.getId() + ")");
        return saved;
    }

    @Transactional(readOnly = true)
    public Employee get(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Employee", id));
    }

    @Transactional(readOnly = true)
    public Page<Employee> list(String department, Pageable pageable) {
        if (department != null && !department.isBlank()) {
            return repository.findByDepartmentIgnoreCase(department, pageable);
        }
        return repository.findAll(pageable);
    }

    public Employee update(Long id, EmployeeRequest request) {
        Employee employee = get(id);
        if (!employee.getEmail().equalsIgnoreCase(request.email())
                && repository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("An employee with this email already exists: " + request.email());
        }
        apply(employee, request);
        audit.log("UPDATE_EMPLOYEE", "Updated employee id=" + id);
        return employee;
    }

    public Employee terminate(Long id) {
        Employee employee = get(id);
        employee.setStatus(EmployeeStatus.TERMINATED);
        audit.log("TERMINATE_EMPLOYEE", "Terminated employee id=" + id);
        return employee;
    }

    private void apply(Employee e, EmployeeRequest r) {
        e.setFirstName(r.firstName().trim());
        e.setLastName(r.lastName().trim());
        e.setEmail(r.email().trim().toLowerCase());
        e.setDepartment(r.department());
        e.setJobTitle(r.jobTitle());
        e.setMonthlySalary(r.monthlySalary());
        e.setHireDate(r.hireDate());
    }
}
