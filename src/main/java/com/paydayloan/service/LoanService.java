package com.paydayloan.service;

import com.paydayloan.dto.*;
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
    private final DisbursementTxnRepository disbursementTxnRepository;
    private final RepaymentTxnRepository repaymentTxnRepository;

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

            validateEmployeeCanRequestLoan(employee, productConfig, requestDTO.getRequestedAmount());

            LoanSimulationDTO simulation = calculateLoanSimulation(employee, currentSalary, productConfig,
                    requestDTO.getRequestedAmount(), requestDTO.getRepaymentDate());
            if (!Boolean.TRUE.equals(simulation.getEligible())) {
                throw new RuntimeException("Requested amount exceeds eligible amount");
            }

            // Create loan request
            LoanRequest loanRequest = LoanRequest.builder()
                    .requestRefNo(generateRequestRefNo())
                    .productConfig(productConfig)
                    .corporate(employee.getCorporate())
                    .employee(employee)
                    .customerId(employee.getCustomerId())
                    .requestedAmount(requestDTO.getRequestedAmount())
                    .serviceChargeAmount(simulation.getServiceChargeAmount())
                    .netDisburseAmount(simulation.getNetDisburseAmount())
                    .eligibleAmount(simulation.getEligibleAmount())
                    .eligiblePercent(simulation.getEligiblePercent())
                    .monthlySalary(simulation.getMonthlySalary())
                    .repaymentDate(simulation.getRepaymentDate())
                    .purpose(requestDTO.getPurpose())
                    .requestChannel(requestDTO.getRequestChannel() != null ? requestDTO.getRequestChannel() : "MOBILE_APP")
                    .requestStatus("PENDING_CORP_APPROVAL")
                    .createdBy(createdBy)
                    .build();

            loanRequest = loanRequestRepository.save(loanRequest);

            // Log audit
            logAudit("PDL_LOAN",  "LoanRequest", loanRequest.getRequestId().toString(), "CREATE", createdBy, "EMPLOYEE", null,
                    "Loan request created: " + loanRequest.getRequestRefNo());

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

    @Transactional(readOnly = true)
    public EmployeeDashboardDTO getEmployeeDashboard(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        EmployeeSalary currentSalary = employeeSalaryRepository
                .findByEmployeeEmployeeIdAndIsCurrentYn(employeeId, 1)
                .orElseThrow(() -> new RuntimeException("Current salary not found for employee"));
        ProductConfig productConfig = productConfigRepository.findByStatus("ACTIVE").stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Active product config not found"));

        BigDecimal eligibleAmount = calculateEligibleAmount(currentSalary, productConfig);
        List<LoanAccount> activeLoans = loanAccountRepository.findByEmployeeEmployeeIdAndLoanStatus(employeeId, "ACTIVE");
        BigDecimal utilizedAmount = activeLoans.stream()
                .map(LoanAccount::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal availableLimit = eligibleAmount.subtract(utilizedAmount).max(BigDecimal.ZERO);

        return EmployeeDashboardDTO.builder()
                .employeeId(employee.getEmployeeId())
                .corporateId(employee.getCorporate().getCorporateId())
                .monthlySalary(currentSalary.getMonthlySalary())
                .eligiblePercent(productConfig.getMaxEligiblePercent())
                .eligibleAmount(eligibleAmount)
                .utilizedAmount(utilizedAmount)
                .availableLimit(availableLimit)
                .activeLoanCount(activeLoans.size())
                .maxActiveLoanPerEmployee(productConfig.getMaxActiveLoanPerEmp())
                .activeLoan(activeLoans.stream().findFirst().map(this::mapToActiveLoanDTO).orElse(null))
                .loanHistory(loanRequestRepository.findByEmployeeEmployeeId(employeeId).stream().map(this::mapToDTO).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public LoanSimulationDTO simulateLoan(Long employeeId, Long productConfigId, BigDecimal requestedAmount, LocalDate repaymentDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        EmployeeSalary currentSalary = employeeSalaryRepository
                .findByEmployeeEmployeeIdAndIsCurrentYn(employeeId, 1)
                .orElseThrow(() -> new RuntimeException("Current salary not found for employee"));
        ProductConfig productConfig = productConfigRepository.findById(productConfigId)
                .orElseThrow(() -> new RuntimeException("Product config not found"));

        validateEmployeeCanRequestLoan(employee, productConfig, requestedAmount);
        return calculateLoanSimulation(employee, currentSalary, productConfig, requestedAmount, repaymentDate);
    }

    @Transactional(readOnly = true)
    public List<LoanRequestDTO> getLoanRequests(Long corporateId, Long employeeId, String requestStatus) {
        List<LoanRequest> loanRequests;
        if (requestStatus != null) {
            loanRequests = loanRequestRepository.findByRequestStatus(requestStatus);
        } else if (employeeId != null) {
            loanRequests = loanRequestRepository.findByEmployeeEmployeeId(employeeId);
        } else if (corporateId != null) {
            loanRequests = loanRequestRepository.findByCorporateCorporateId(corporateId);
        } else {
            loanRequests = loanRequestRepository.findAll();
        }
        return loanRequests.stream().map(this::mapToDTO).toList();
    }

    /**
     * Approve loan request by corporate
     */
    public void approveLoanRequestByCoroperate(Long requestId, String approverUserId, String approverName, String remarks) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (!"PENDING_CORP_APPROVAL".equals(loanRequest.getRequestStatus())) {
            throw new RuntimeException("Loan request must be pending corporate approval");
        }

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
        logAudit("PDL_LOAN", "LoanRequest", requestId.toString(), "APPROVE", approverUserId, "CORPORATE", null,
                "Loan request approved by corporate: " + loanRequest.getRequestRefNo());

        log.info("Loan request approved by corporate: {}", loanRequest.getRequestRefNo());
    }

    public void rejectLoanRequestByCorporate(Long requestId, String approverUserId, String approverName, String remarks) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (!"PENDING_CORP_APPROVAL".equals(loanRequest.getRequestStatus())) {
            throw new RuntimeException("Loan request must be pending corporate approval");
        }

        loanRequest.setRequestStatus("REJECTED_BY_CORP");
        loanRequest.setStatusRemarks(remarks);
        loanRequest.setUpdatedAt(LocalDateTime.now());
        loanRequestRepository.save(loanRequest);

        RequestApproval approval = RequestApproval.builder()
                .loanRequest(loanRequest)
                .approvalLevel(1)
                .approverType("CORPORATE")
                .approverUserId(approverUserId)
                .approverName(approverName)
                .approvalStatus("REJECTED")
                .approvalDate(LocalDateTime.now())
                .approvalRemarks(remarks)
                .build();

        requestApprovalRepository.save(approval);

        logAudit("PDL_LOAN", "LoanRequest", requestId.toString(), "REJECT", approverUserId, "CORPORATE", null,
                "Loan request rejected by corporate: " + loanRequest.getRequestRefNo());
    }

    /**
     * Approve loan request by bank.
     */
    public void approveLoanRequestByBank(Long requestId, String approverUserId, String approverName, String remarks) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (!"APPROVED_BY_CORP".equals(loanRequest.getRequestStatus())
                && !"PENDING_BANK_REVIEW".equals(loanRequest.getRequestStatus())) {
            throw new RuntimeException("Loan request must be approved by corporate before bank approval");
        }

        loanRequest.setRequestStatus("APPROVED_BY_BANK");
        loanRequest.setStatusRemarks(remarks);
        loanRequest.setUpdatedAt(LocalDateTime.now());
        loanRequestRepository.save(loanRequest);

        RequestApproval approval = RequestApproval.builder()
                .loanRequest(loanRequest)
                .approvalLevel(2)
                .approverType("BANK")
                .approverUserId(approverUserId)
                .approverName(approverName)
                .approvalStatus("APPROVED")
                .approvalDate(LocalDateTime.now())
                .approvalRemarks(remarks)
                .build();

        requestApprovalRepository.save(approval);

        logAudit("PDL_LOAN", "LoanRequest", requestId.toString(), "APPROVE", approverUserId, "BANK", null,
                "Loan request approved by bank: " + loanRequest.getRequestRefNo());

        log.info("Loan request approved by bank: {}", loanRequest.getRequestRefNo());
    }

    public void rejectLoanRequestByBank(Long requestId, String approverUserId, String approverName, String remarks) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (!"APPROVED_BY_CORP".equals(loanRequest.getRequestStatus())
                && !"PENDING_BANK_REVIEW".equals(loanRequest.getRequestStatus())) {
            throw new RuntimeException("Loan request must be approved by corporate before bank rejection");
        }

        loanRequest.setRequestStatus("REJECTED_BY_BANK");
        loanRequest.setStatusRemarks(remarks);
        loanRequest.setUpdatedAt(LocalDateTime.now());
        loanRequestRepository.save(loanRequest);

        RequestApproval approval = RequestApproval.builder()
                .loanRequest(loanRequest)
                .approvalLevel(2)
                .approverType("BANK")
                .approverUserId(approverUserId)
                .approverName(approverName)
                .approvalStatus("REJECTED")
                .approvalDate(LocalDateTime.now())
                .approvalRemarks(remarks)
                .build();

        requestApprovalRepository.save(approval);

        logAudit("PDL_LOAN", "LoanRequest", requestId.toString(), "REJECT", approverUserId, "BANK", null,
                "Loan request rejected by bank: " + loanRequest.getRequestRefNo());
    }

    public void cancelLoanRequest(Long requestId, String cancelledBy, String remarks) {
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if ("DISBURSED".equals(loanRequest.getRequestStatus()) || "REPAID".equals(loanRequest.getRequestStatus())) {
            throw new RuntimeException("Disbursed or repaid loan request cannot be cancelled");
        }

        loanRequest.setRequestStatus("CANCELLED");
        loanRequest.setStatusRemarks(remarks);
        loanRequest.setCancelledBy(cancelledBy);
        loanRequest.setCancelledAt(LocalDateTime.now());
        loanRequest.setUpdatedAt(LocalDateTime.now());
        loanRequestRepository.save(loanRequest);

        logAudit("PDL_LOAN", "LoanRequest", requestId.toString(), "UPDATE", cancelledBy, "EMPLOYEE", null,
                "Loan request cancelled: " + loanRequest.getRequestRefNo());
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

        loanAccountRepository.findByLoanRequestRequestId(requestId)
                .ifPresent(existingLoan -> {
                    throw new RuntimeException("Loan account already exists for this request");
                });

        String disbursementAccountNo = loanRequest.getEmployee().getSalaryAccountNo();
        String repaymentAccountNo = loanRequest.getEmployee().getRepaymentAccountNo() != null
                ? loanRequest.getEmployee().getRepaymentAccountNo()
                : loanRequest.getEmployee().getSalaryAccountNo();

        LoanAccount loanAccount = LoanAccount.builder()
                .loanRefNo(generateLoanRefNo())
                .loanRequest(loanRequest)
                .corporate(loanRequest.getCorporate())
                .employee(loanRequest.getEmployee())
                .customerId(loanRequest.getCustomerId())
                .disbursementAccountNo(disbursementAccountNo)
                .repaymentAccountNo(repaymentAccountNo)
                .sanctionedAmount(loanRequest.getRequestedAmount())
                .serviceChargeAmount(loanRequest.getServiceChargeAmount())
                .disbursedAmount(loanRequest.getNetDisburseAmount())
                .outstandingAmount(loanRequest.getRequestedAmount())
                .disbursementDate(LocalDateTime.now())
                .valueDate(LocalDate.now())
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
                .chargeDue(BigDecimal.ZERO)
                .totalDue(loanRequest.getRequestedAmount())
                .outstandingDue(loanRequest.getRequestedAmount())
                .scheduleStatus("PENDING")
                .build();

        repaymentScheduleRepository.save(schedule);

        DisbursementTxn disbursementTxn = DisbursementTxn.builder()
                .loanAccount(loanAccount)
                .loanRequest(loanRequest)
                .txnRefNo(generateTxnRefNo("DISB"))
                .sourceAccountNo("BANK_PDL_POOL")
                .destinationAccountNo(disbursementAccountNo)
                .grossAmount(loanRequest.getRequestedAmount())
                .serviceChargeAmount(loanRequest.getServiceChargeAmount())
                .netAmount(loanRequest.getNetDisburseAmount())
                .txnStatus("SUCCESS")
                .build();

        disbursementTxnRepository.save(disbursementTxn);

        loanRequest.setRequestStatus("DISBURSED");
        loanRequest.setUpdatedAt(LocalDateTime.now());
        loanRequestRepository.save(loanRequest);

        // Log audit
        logAudit("PDL_LOAN", "LoanAccount", loanAccount.getLoanId().toString(), "DISBURSE", createdBy, "SYSTEM", null,
                "Loan disbursed: " + loanAccount.getLoanRefNo());

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

    @Transactional(readOnly = true)
    public List<RepaymentSchedule> getRepaymentSchedule(Long loanId) {
        return repaymentScheduleRepository.findByLoanAccountLoanId(loanId);
    }

    public void recoverLoan(RepaymentDTO repaymentDTO, String createdBy) {
        LoanAccount loanAccount = loanAccountRepository.findById(repaymentDTO.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan account not found"));

        if (!"ACTIVE".equals(loanAccount.getLoanStatus()) && !"OVERDUE".equals(loanAccount.getLoanStatus())) {
            throw new RuntimeException("Only active or overdue loans can be recovered");
        }

        BigDecimal paymentAmount = repaymentDTO.getAmount() != null ? repaymentDTO.getAmount() : loanAccount.getOutstandingAmount();
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Repayment amount must be positive");
        }
        if (paymentAmount.compareTo(loanAccount.getOutstandingAmount()) > 0) {
            throw new RuntimeException("Repayment amount exceeds outstanding amount");
        }

        RepaymentSchedule schedule = repaymentScheduleRepository.findByLoanAccountLoanId(loanAccount.getLoanId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Repayment schedule not found"));

        BigDecimal remainingOutstanding = loanAccount.getOutstandingAmount().subtract(paymentAmount);
        loanAccount.setOutstandingAmount(remainingOutstanding);
        loanAccount.setUpdatedBy(createdBy);
        if (remainingOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            loanAccount.setLoanStatus("REPAID");
            loanAccount.setClosedDate(LocalDate.now());
            loanAccount.getLoanRequest().setRequestStatus("REPAID");
            loanAccount.getLoanRequest().setUpdatedAt(LocalDateTime.now());
        }
        loanAccountRepository.save(loanAccount);

        schedule.setPaidAmount(nullToZero(schedule.getPaidAmount()).add(paymentAmount));
        schedule.setOutstandingDue(schedule.getOutstandingDue().subtract(paymentAmount).max(BigDecimal.ZERO));
        schedule.setScheduleStatus(schedule.getOutstandingDue().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PARTIAL");
        repaymentScheduleRepository.save(schedule);

        RepaymentTxn repaymentTxn = RepaymentTxn.builder()
                .loanAccount(loanAccount)
                .repaymentSchedule(schedule)
                .txnRefNo(generateTxnRefNo("REPAY"))
                .txnSource(repaymentDTO.getTxnSource() != null ? repaymentDTO.getTxnSource() : "SALARY_DEDUCTION")
                .debitAccountNo(repaymentDTO.getDebitAccountNo() != null ? repaymentDTO.getDebitAccountNo() : loanAccount.getRepaymentAccountNo())
                .creditAccountNo(repaymentDTO.getCreditAccountNo() != null ? repaymentDTO.getCreditAccountNo() : "BANK_PDL_POOL")
                .principalPaid(paymentAmount)
                .chargePaid(BigDecimal.ZERO)
                .penaltyPaid(BigDecimal.ZERO)
                .totalPaid(paymentAmount)
                .cbsTxnRefNo(repaymentDTO.getCbsTxnRefNo())
                .txnStatus("SUCCESS")
                .remarks(repaymentDTO.getRemarks())
                .build();
        repaymentTxnRepository.save(repaymentTxn);

        logAudit("PDL_LOAN", "LoanAccount", loanAccount.getLoanId().toString(), "REPAY", createdBy, "SYSTEM", null,
                "Loan recovery posted: " + repaymentTxn.getTxnRefNo());
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getCorporateSummary(Long corporateId) {
        return buildSummary(corporateId,
                loanRequestRepository.findByCorporateCorporateId(corporateId),
                loanAccountRepository.findByCorporateCorporateId(corporateId));
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getBankSummary() {
        return buildSummary(null, loanRequestRepository.findAll(), loanAccountRepository.findAll());
    }

    // Helper methods

    private String generateRequestRefNo() {
        return "REQ-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateLoanRefNo() {
        return "LOAN-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTxnRefNo(String prefix) {
        return prefix + "-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LoanSimulationDTO calculateLoanSimulation(Employee employee, EmployeeSalary currentSalary,
                                                      ProductConfig productConfig, BigDecimal requestedAmount,
                                                      LocalDate repaymentDate) {
        BigDecimal eligibleAmount = calculateEligibleAmount(currentSalary, productConfig);
        BigDecimal utilizedAmount = loanAccountRepository
                .findByEmployeeEmployeeIdAndLoanStatus(employee.getEmployeeId(), "ACTIVE")
                .stream()
                .map(LoanAccount::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal availableLimit = eligibleAmount.subtract(utilizedAmount).max(BigDecimal.ZERO);
        BigDecimal serviceCharge = calculateServiceCharge(productConfig, requestedAmount);
        LocalDate effectiveRepaymentDate = repaymentDate != null
                ? repaymentDate
                : LocalDate.now().plusDays(productConfig.getRepaymentDueDays());
        boolean eligible = requestedAmount.compareTo(availableLimit) <= 0;

        return LoanSimulationDTO.builder()
                .employeeId(employee.getEmployeeId())
                .productConfigId(productConfig.getProductConfigId())
                .requestedAmount(requestedAmount)
                .monthlySalary(currentSalary.getMonthlySalary())
                .eligiblePercent(productConfig.getMaxEligiblePercent())
                .eligibleAmount(eligibleAmount)
                .availableLimit(availableLimit)
                .serviceChargeAmount(serviceCharge)
                .netDisburseAmount(requestedAmount.subtract(serviceCharge))
                .repaymentDate(effectiveRepaymentDate)
                .eligible(eligible)
                .message(eligible ? "Eligible" : "Requested amount exceeds available limit")
                .build();
    }

    private BigDecimal calculateEligibleAmount(EmployeeSalary currentSalary, ProductConfig productConfig) {
        return currentSalary.getMonthlySalary()
                .multiply(productConfig.getMaxEligiblePercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateServiceCharge(ProductConfig productConfig, BigDecimal requestedAmount) {
        BigDecimal charge = requestedAmount
                .multiply(productConfig.getServiceChargePercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return charge.max(productConfig.getMinServiceCharge());
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private void validateEmployeeCanRequestLoan(Employee employee, ProductConfig productConfig, BigDecimal requestedAmount) {
        if (!"ACTIVE".equals(employee.getStatus()) || !"ACTIVE".equals(employee.getEmploymentStatus())) {
            throw new RuntimeException("Employee is not active");
        }
        if (employee.getEligibilityYn() == null || employee.getEligibilityYn() != 1) {
            throw new RuntimeException("Employee is not eligible for payday loan");
        }
        if (!"ACTIVE".equals(productConfig.getStatus())) {
            throw new RuntimeException("Product config is not active");
        }
        if (requestedAmount.compareTo(productConfig.getMinRequestAmount()) < 0) {
            throw new RuntimeException("Requested amount is below minimum request amount");
        }
        if (productConfig.getMaxRequestAmount() != null
                && requestedAmount.compareTo(productConfig.getMaxRequestAmount()) > 0) {
            throw new RuntimeException("Requested amount exceeds maximum request amount");
        }
        int activeLoanCount = loanAccountRepository
                .findByEmployeeEmployeeIdAndLoanStatus(employee.getEmployeeId(), "ACTIVE")
                .size();
        if (activeLoanCount >= productConfig.getMaxActiveLoanPerEmp()) {
            throw new RuntimeException("Employee already has maximum allowed active loans");
        }
    }

    private DashboardSummaryDTO buildSummary(Long corporateId, List<LoanRequest> requests, List<LoanAccount> accounts) {
        BigDecimal totalDisbursed = accounts.stream()
                .map(LoanAccount::getDisbursedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingExposure = accounts.stream()
                .filter(account -> "ACTIVE".equals(account.getLoanStatus()) || "OVERDUE".equals(account.getLoanStatus()))
                .map(LoanAccount::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal serviceChargeCollected = accounts.stream()
                .map(LoanAccount::getServiceChargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryDTO.builder()
                .corporateId(corporateId)
                .totalRequests(requests.size())
                .pendingRequests(countRequestStatus(requests, "PENDING_CORP_APPROVAL") + countRequestStatus(requests, "APPROVED_BY_CORP"))
                .approvedRequests(countRequestStatus(requests, "APPROVED_BY_CORP") + countRequestStatus(requests, "APPROVED_BY_BANK"))
                .rejectedRequests(countRequestStatus(requests, "REJECTED_BY_CORP") + countRequestStatus(requests, "REJECTED_BY_BANK"))
                .disbursedRequests(countRequestStatus(requests, "DISBURSED"))
                .activeLoanCount(accounts.stream().filter(account -> "ACTIVE".equals(account.getLoanStatus())).count())
                .repaidLoanCount(accounts.stream().filter(account -> "REPAID".equals(account.getLoanStatus())).count())
                .totalDisbursedAmount(totalDisbursed)
                .outstandingExposure(outstandingExposure)
                .serviceChargeCollected(serviceChargeCollected)
                .build();
    }

    private long countRequestStatus(List<LoanRequest> requests, String status) {
        return requests.stream().filter(request -> status.equals(request.getRequestStatus())).count();
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

}
