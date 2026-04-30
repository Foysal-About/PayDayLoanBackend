package com.paydayloan.controller;

import com.paydayloan.dto.EmployeeSalaryDTO;
import com.paydayloan.entity.Corporate;
import com.paydayloan.entity.Employee;
import com.paydayloan.entity.EmployeeSalary;
import com.paydayloan.exception.ApiResponse;
import com.paydayloan.repository.CorporateRepository;
import com.paydayloan.repository.EmployeeRepository;
import com.paydayloan.repository.EmployeeSalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee-salaries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeeSalaryController {

    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final EmployeeRepository employeeRepository;
    private final CorporateRepository corporateRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeSalaryDTO>> create(
            @RequestBody EmployeeSalaryDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        EmployeeSalary salary = toEntity(request, new EmployeeSalary());
        salary.setCreatedBy(userId != null ? userId : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toDTO(employeeSalaryRepository.save(salary))));
    }

    @GetMapping
    public ApiResponse<List<EmployeeSalaryDTO>> list(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long corporateId) {
        List<EmployeeSalary> salaries;
        if (employeeId != null) {
            salaries = employeeSalaryRepository.findByEmployeeEmployeeId(employeeId);
        } else if (corporateId != null) {
            salaries = employeeSalaryRepository.findByCorporateCorporateId(corporateId);
        } else {
            salaries = employeeSalaryRepository.findAll();
        }
        return ApiResponse.ok(salaries.stream().map(this::toDTO).toList());
    }

    @GetMapping("/{salaryId}")
    public ApiResponse<EmployeeSalaryDTO> get(@PathVariable Long salaryId) {
        return ApiResponse.ok(toDTO(find(salaryId)));
    }

    @GetMapping("/employee/{employeeId}/current")
    public ApiResponse<EmployeeSalaryDTO> getCurrent(@PathVariable Long employeeId) {
        return ApiResponse.ok(toDTO(employeeSalaryRepository
                .findByEmployeeEmployeeIdAndIsCurrentYn(employeeId, 1)
                .orElseThrow(() -> new RuntimeException("Current salary not found"))));
    }

    @PutMapping("/{salaryId}")
    public ApiResponse<EmployeeSalaryDTO> update(
            @PathVariable Long salaryId,
            @RequestBody EmployeeSalaryDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        EmployeeSalary salary = toEntity(request, find(salaryId));
        salary.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(employeeSalaryRepository.save(salary)));
    }

    private EmployeeSalary find(Long salaryId) {
        return employeeSalaryRepository.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Employee salary not found"));
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    private Corporate findCorporate(Long corporateId) {
        return corporateRepository.findById(corporateId)
                .orElseThrow(() -> new RuntimeException("Corporate not found"));
    }

    private EmployeeSalary toEntity(EmployeeSalaryDTO dto, EmployeeSalary entity) {
        if (dto.getEmployeeId() != null) {
            entity.setEmployee(findEmployee(dto.getEmployeeId()));
        }
        if (dto.getCorporateId() != null) {
            entity.setCorporate(findCorporate(dto.getCorporateId()));
        }
        entity.setMonthlySalary(dto.getMonthlySalary());
        if (dto.getEligiblePercent() != null) {
            entity.setEligiblePercent(dto.getEligiblePercent());
        }
        entity.setMaxEligibleAmount(dto.getMaxEligibleAmount());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
        if (dto.getIsCurrentYn() != null) {
            entity.setIsCurrentYn(dto.getIsCurrentYn());
        }
        if (dto.getApprovalStatus() != null) {
            entity.setApprovalStatus(dto.getApprovalStatus());
        }
        entity.setRemarks(dto.getRemarks());
        return entity;
    }

    private EmployeeSalaryDTO toDTO(EmployeeSalary entity) {
        return EmployeeSalaryDTO.builder()
                .salaryId(entity.getSalaryId())
                .employeeId(entity.getEmployee().getEmployeeId())
                .corporateId(entity.getCorporate().getCorporateId())
                .monthlySalary(entity.getMonthlySalary())
                .eligiblePercent(entity.getEligiblePercent())
                .maxEligibleAmount(entity.getMaxEligibleAmount())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .isCurrentYn(entity.getIsCurrentYn())
                .approvalStatus(entity.getApprovalStatus())
                .remarks(entity.getRemarks())
                .build();
    }
}
