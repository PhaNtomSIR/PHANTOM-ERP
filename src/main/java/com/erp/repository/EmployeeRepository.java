package com.erp.repository;

import com.erp.model.hr.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    List<Employee> findByDepartmentId(Long departmentId);
    long countByStatus(Employee.EmployeeStatus status);
}
