package com.paydayloan.service;

import com.paydayloan.dto.ActiveLoanDTO;
import com.paydayloan.dto.LoanRequestDTO;
import com.paydayloan.entity.*;
import com.paydayloan.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LoanService {

    private final LoanRequestRepository loanRequestRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final ProductConfigRepository productConfigRepository;
    private final CorporateRepository corporateRepository;
    private final RequestApprovalRepository requestApprovalRepository;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final EmployeeLimitRepository employeeLimitRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Create a new loan request
     */
    public LoanRequestDTO createLoanRequest(LoanRequestDTO requestDTO, String createdBy) {
        try {
            log.info("Creating loan request for employee: {}", requestDTO.getEmployeeId());

            // Validate employee and get salary details
            Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            EmployeeSalary currentSalary = employeeSalaryRepository
                    .findByEmployeeEmployeeIdAndIsCurrentYn(requestDTO.getEmployeeId(), 1)
                    .orElseThrow(() -> new RuntimeException("Current salary not found for employee"));

            // Get product config
            ProductConfig productConfig = productConfigRepository.findById(requestDTO.getProductConfigId())
                    .orElseThrow(() -> new RuntimeException("Product config not found"));

            // Calculate eligible amount
            BigDecimal eligiblePercent = productConfig.getMaxEligiblePercent();
            BigDecimal eligibleAmount = currentSalary.getMonthlySalary()
                    .multiply(eligiblePercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Validate requested amount
            if (requestDTO.getRequestedAmount().compareTo(eligibleAmount) > 0) {
                throw new RuntimeException("Requested amount exceeds eligible amount");
            }

            // Calculate service charge
            BigDecimal serviceCharge = requestDTO.getRequestedAmount()
                    .multiply(productConfig.getServiceChargePercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (serviceCharge.compareTo(productConfig.getMinServiceCharge()) < 0) {
                serviceCharge = productConfig.getMinServiceCharge();
            }

            // Create loan request
            LoanRequest loanRequest = LoanRequest.builder()
                    .requestRefNo(generateRequestRefNo())
                    .productConfig(productConfig)
                    .corporate(employee.getCorporate())
                    .employee(employee)
                    .customerId(employee.getCustomerId())
                    .requestedAmount(requestDTO.getRequestedAmount())
                    .serviceChargeAmount(serviceCharge)
                    .netDisburseAmount(requestDTO.getRequestedAmount().subtract(serviceCharge))
                    .eligibleAmount(eligibleAmount)
                    .eligiblePercent(eligiblePercent)
                    .monthlySalary(currentSalary.getMonthlySalary())
                    .repaymentDate(requestDTO.getRepaymentDate())
                    .purpose(requestDTO.getPurpose())
                    .requestChannel(requestDTO.getRequestChannel() != null ? requestDTO.getRequestChannel() : "MOBILE_APP")
                    .requestStatus("PENDING_CORP_APPROVAL")
                    .createdBy(createdBy)
                    .build();

            loanRequest = loanRequestRepository.save(loanRequest);

            // Log audit
            logAudit("PDL_LOAN",  "LoanRequest", loanRequest.getRequestId().toString(), "CREATE", createdBy, "EMPLOYEE", null, toJson(loanRequest));

            requestDTO.setRequestId(loanRequest.getRequestId());
            requestDTO.setRequestRefNo(loanRequest.getRequestRefNo());
            requestDTO.setRequestStatus(loanRequest.getRequestStatus());
            requestDTO.setEligibleAmount(loanRequest.getEligibleAmount());
            requestDTO.setServiceChargeAmount(loanRequest.getServiceChargeAmount());
            requestDTO.setNetDisburseAmount(loanRequest.getNetDisburseAmount());

            log.info("Loan request created successfully: {}", loanRequest.getRequestRefNo());
            return requestDTO;

        } catch (Exception e) {
            log.error("Error creating loan request", e);
            throw new RuntimeException("Error creating loan request: " + e.getMessage());
        }
    }

    /**
     * Get loan request by ID
     */
    @Transactional(readOnly = true)
    public LoanRequestDTO getLoanRequest(Long requestId) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        return mapToDTO(loanRequest);
    }

    /**
     * Approve loan request by corporate
     */
    public void approveLoanRequestByCoroperate(Long requestId, String approverUserId, String approverName, String remarks) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        loanRequest.setRequestStatus("APPROVED_BY_CORP");
        loanRequest.setStatusRemarks(remarks);
        loanRequest.setUpdatedAt(LocalDateTime.now());
        loanRequestRepository.save(loanRequest);

        // Create approval record
        RequestApproval approval = RequestApproval.builder()
                .loanRequest(loanRequest)
                .approvalLevel(1)
                .approverType("CORPORATE")
                .approverUserId(approverUserId)
                .approverName(approverName)
                .approvalStatus("APPROVED")
                .approvalDate(LocalDateTime.now())
                .approvalRemarks(remarks)
                .build();

        requestApprovalRepository.save(approval);

        // Log audit
        logAudit("PDL_LOAN", "LoanRequest", requestId.toString(), "APPROVE", approverUserId, "CORPORATE", toJson(loanRequest), toJson(loanRequest));

        log.info("Loan request approved by corporate: {}", loanRequest.getRequestRefNo());
    }

    /**
     * Create loan account (disbursement)
     */
    public void createLoanAccount(Long requestId, String createdBy) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (!loanRequest.getRequestStatus().equals("APPROVED_BY_BANK")) {
            throw new RuntimeException("Loan request must be approved by bank before disbursement");
        }

        LoanAccount loanAccount = LoanAccount.builder()
                .loanRefNo(generateLoanRefNo())
                .loanRequest(loanRequest)
                .corporate(loanRequest.getCorporate())
                .employee(loanRequest.getEmployee())
                .customerId(loanRequest.getCustomerId())
                .disbursementAccountNo(loanRequest.getEmployee().getRepaymentAccountNo())
                .repaymentAccountNo(loanRequest.getEmployee().getSalaryAccountNo())
                .sanctionedAmount(loanRequest.getRequestedAmount())
                .serviceChargeAmount(loanRequest.getServiceChargeAmount())
                .disbursedAmount(loanRequest.getNetDisburseAmount())
                .outstandingAmount(loanRequest.getRequestedAmount().add(loanRequest.getServiceChargeAmount()))
                .maturityDate(loanRequest.getRepaymentDate())
                .loanStatus("ACTIVE")
                .createdBy(createdBy)
                .build();

        loanAccount = loanAccountRepository.save(loanAccount);

        // Create repayment schedule
        RepaymentSchedule schedule = RepaymentSchedule.builder()
                .loanAccount(loanAccount)
                .installmentNo(1)
                .dueDate(loanRequest.getRepaymentDate())
                .principalDue(loanRequest.getRequestedAmount())
                .chargeDue(loanRequest.getServiceChargeAmount())
                .totalDue(loanRequest.getRequestedAmount().add(loanRequest.getServiceChargeAmount()))
                .outstandingDue(loanRequest.getRequestedAmount().add(loanRequest.getServiceChargeAmount()))
                .scheduleStatus("PENDING")
                .build();

        repaymentScheduleRepository.save(schedule);

        // Log audit
        logAudit("PDL_LOAN", "LoanAccount", loanAccount.getLoanId().toString(), "CREATE", createdBy, "SYSTEM", null, toJson(loanAccount));

        log.info("Loan account created successfully: {}", loanAccount.getLoanRefNo());
    }

    /**
     * Get all pending loan requests for a corporate
     */
    @Transactional(readOnly = true)
    public List<LoanRequestDTO> getPendingLoanRequestsForCorporate(Long corporateId) {
        return loanRequestRepository.findByCorporateCorporateIdAndRequestStatus(corporateId, "PENDING_CORP_APPROVAL")
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Get all active loans for an employee
     */
    @Transactional(readOnly = true)
    public List<ActiveLoanDTO> getActiveLoansByEmployee(Long employeeId) {
        return loanAccountRepository.findByEmployeeEmployeeIdAndLoanStatus(employeeId, "ACTIVE")
                .stream()
                .map(this::mapToActiveLoanDTO)
                .toList();
    }

    // Helper methods

    private String generateRequestRefNo() {
        return "REQ-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateLoanRefNo() {
        return "LOAN-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LoanRequestDTO mapToDTO(LoanRequest loanRequest) {
        return LoanRequestDTO.builder()
                .requestId(loanRequest.getRequestId())
                .requestRefNo(loanRequest.getRequestRefNo())
                .employeeId(loanRequest.getEmployee().getEmployeeId())
                .customerId(loanRequest.getCustomerId())
                .corporateId(loanRequest.getCorporate().getCorporateId())
                .productConfigId(loanRequest.getProductConfig().getProductConfigId())
                .requestedAmount(loanRequest.getRequestedAmount())
                .repaymentDate(loanRequest.getRepaymentDate())
                .purpose(loanRequest.getPurpose())
                .requestChannel(loanRequest.getRequestChannel())
                .requestStatus(loanRequest.getRequestStatus())
                .eligibleAmount(loanRequest.getEligibleAmount())
                .serviceChargeAmount(loanRequest.getServiceChargeAmount())
                .netDisburseAmount(loanRequest.getNetDisburseAmount())
                .build();
    }

    private ActiveLoanDTO mapToActiveLoanDTO(LoanAccount loanAccount) {
        return ActiveLoanDTO.builder()
                .loanId(loanAccount.getLoanId())
                .loanRefNo(loanAccount.getLoanRefNo())
                .requestId(loanAccount.getLoanRequest().getRequestId())
                .employeeId(loanAccount.getEmployee().getEmployeeId())
                .sanctionedAmount(loanAccount.getSanctionedAmount())
                .serviceChargeAmount(loanAccount.getServiceChargeAmount())
                .disbursedAmount(loanAccount.getDisbursedAmount())
                .outstandingAmount(loanAccount.getOutstandingAmount())
                .maturityDate(loanAccount.getMaturityDate())
                .loanStatus(loanAccount.getLoanStatus())
                .build();
    }

    private void logAudit(String moduleName, String entityName, String entityId, String actionType, String actionBy, String actionByType, String oldValue, String newValue) {
        AuditLog auditLog = AuditLog.builder()
                .moduleName(moduleName)
                .entityName(entityName)
                .entityId(entityId)
                .actionType(actionType)
                .actionBy(actionBy)
                .actionByType(actionByType)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        auditLogRepository.save(auditLog);
    }

    private String toJson(Object obj) {
        // Simple JSON conversion - in production use Jackson ObjectMapper
        return obj.toString();
    }
}
