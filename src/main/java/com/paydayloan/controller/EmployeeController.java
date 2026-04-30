package com.paydayloan.controller;

import com.paydayloan.dto.EmployeeDTO;
import com.paydayloan.entity.Corporate;
import com.paydayloan.entity.Employee;
import com.paydayloan.exception.ApiResponse;
import com.paydayloan.repository.CorporateRepository;
import com.paydayloan.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final CorporateRepository corporateRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> create(
            @RequestBody EmployeeDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Employee employee = toEntity(request, new Employee());
        employee.setCreatedBy(userId != null ? userId : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toDTO(employeeRepository.save(employee))));
    }

    @GetMapping
    public ApiResponse<List<EmployeeDTO>> list(
            @RequestParam(required = false) Long corporateId,
            @RequestParam(required = false) String status) {
        List<Employee> employees;
        if (corporateId != null) {
            employees = employeeRepository.findByCorporateCorporateId(corporateId);
        } else if (status != null) {
            employees = employeeRepository.findByStatus(status);
        } else {
            employees = employeeRepository.findAll();
        }
        return ApiResponse.ok(employees.stream().map(this::toDTO).toList());
    }

    @GetMapping("/{employeeId}")
    public ApiResponse<EmployeeDTO> get(@PathVariable Long employeeId) {
        return ApiResponse.ok(toDTO(find(employeeId)));
    }

    @PutMapping("/{employeeId}")
    public ApiResponse<EmployeeDTO> update(
            @PathVariable Long employeeId,
            @RequestBody EmployeeDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Employee employee = toEntity(request, find(employeeId));
        employee.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(employeeRepository.save(employee)));
    }

    @PatchMapping("/{employeeId}/status")
    public ApiResponse<EmployeeDTO> updateStatus(
            @PathVariable Long employeeId,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Employee employee = find(employeeId);
        employee.setStatus(status);
        employee.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(employeeRepository.save(employee)));
    }

    @PatchMapping("/{employeeId}/eligibility")
    public ApiResponse<EmployeeDTO> updateEligibility(
            @PathVariable Long employeeId,
            @RequestParam Integer eligibilityYn,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Employee employee = find(employeeId);
        employee.setEligibilityYn(eligibilityYn);
        employee.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(employeeRepository.save(employee)));
    }

    private Employee find(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    private Corporate findCorporate(Long corporateId) {
        return corporateRepository.findById(corporateId)
                .orElseThrow(() -> new RuntimeException("Corporate not found"));
    }

    private Employee toEntity(EmployeeDTO dto, Employee entity) {
        if (dto.getCorporateId() != null) {
            entity.setCorporate(findCorporate(dto.getCorporateId()));
        }
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setEmployeeName(dto.getEmployeeName());
        entity.setCustomerId(dto.getCustomerId());
        entity.setCifId(dto.getCifId());
        entity.setMobileNo(dto.getMobileNo());
        entity.setEmail(dto.getEmail());
        entity.setNidNo(dto.getNidNo());
        entity.setDesignation(dto.getDesignation());
        entity.setDepartment(dto.getDepartment());
        entity.setJoinDate(dto.getJoinDate());
        if (dto.getEmploymentStatus() != null) {
            entity.setEmploymentStatus(dto.getEmploymentStatus());
        }
        entity.setSalaryAccountNo(dto.getSalaryAccountNo());
        entity.setRepaymentAccountNo(dto.getRepaymentAccountNo());
        if (dto.getEligibilityYn() != null) {
            entity.setEligibilityYn(dto.getEligibilityYn());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        return entity;
    }

    private EmployeeDTO toDTO(Employee entity) {
        return EmployeeDTO.builder()
                .employeeId(entity.getEmployeeId())
                .corporateId(entity.getCorporate().getCorporateId())
                .employeeCode(entity.getEmployeeCode())
                .employeeName(entity.getEmployeeName())
                .customerId(entity.getCustomerId())
                .cifId(entity.getCifId())
                .mobileNo(entity.getMobileNo())
                .email(entity.getEmail())
                .nidNo(entity.getNidNo())
                .designation(entity.getDesignation())
                .department(entity.getDepartment())
                .joinDate(entity.getJoinDate())
                .employmentStatus(entity.getEmploymentStatus())
                .salaryAccountNo(entity.getSalaryAccountNo())
                .repaymentAccountNo(entity.getRepaymentAccountNo())
                .eligibilityYn(entity.getEligibilityYn())
                .status(entity.getStatus())
                .build();
    }
}
