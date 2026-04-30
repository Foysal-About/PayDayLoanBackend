package com.paydayloan.controller;

import com.paydayloan.dto.ActiveLoanDTO;
import com.paydayloan.dto.RepaymentDTO;
import com.paydayloan.dto.LoanRequestDTO;
import com.paydayloan.entity.RepaymentSchedule;
import com.paydayloan.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/loans")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoanController {

    private final LoanService loanService;

    /**
     * Lightweight application health check.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "UP");
        response.put("service", "paydayloan-backend");
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new loan request
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> createLoanRequest(
            @RequestBody LoanRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            log.info("Creating loan request for employee: {}", requestDTO.getEmployeeId());
            String createdBy = userId != null ? userId : "SYSTEM";

            LoanRequestDTO createdRequest = loanService.createLoanRequest(requestDTO, createdBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan request created successfully");
            response.put("data", createdRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating loan request", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Employee dashboard with eligibility, active loan, and loan history.
     */
    @GetMapping("/employee/{employeeId}/dashboard")
    public ResponseEntity<Map<String, Object>> getEmployeeDashboard(@PathVariable Long employeeId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", loanService.getEmployeeDashboard(employeeId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching employee dashboard", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Pre-submit loan summary and charge calculation.
     */
    @GetMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateLoan(
            @RequestParam Long employeeId,
            @RequestParam Long productConfigId,
            @RequestParam BigDecimal requestedAmount,
            @RequestParam(required = false) LocalDate repaymentDate) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", loanService.simulateLoan(employeeId, productConfigId, requestedAmount, repaymentDate));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error simulating loan", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * List loan requests with optional filters.
     */
    @GetMapping("/requests")
    public ResponseEntity<Map<String, Object>> getLoanRequests(
            @RequestParam(required = false) Long corporateId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        try {
            List<LoanRequestDTO> loanRequests = loanService.getLoanRequests(corporateId, employeeId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", loanRequests.size());
            response.put("data", loanRequests);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching loan requests", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Approve loan request by bank.
     */
    @PutMapping("/request/{requestId}/bank-approve")
    public ResponseEntity<Map<String, Object>> approveLoanRequestByBank(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            log.info("Bank approving loan request: {}", requestId);
            String approverUserId = userId != null ? userId : "SYSTEM";
            String approverName = userName != null ? userName : "System";

            loanService.approveLoanRequestByBank(requestId, approverUserId, approverName, remarks);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan request approved by bank successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error bank approving loan request", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Reject loan request by bank.
     */
    @PutMapping("/request/{requestId}/bank-reject")
    public ResponseEntity<Map<String, Object>> rejectLoanRequestByBank(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            String approverUserId = userId != null ? userId : "SYSTEM";
            String approverName = userName != null ? userName : "System";

            loanService.rejectLoanRequestByBank(requestId, approverUserId, approverName, remarks);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan request rejected by bank successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error bank rejecting loan request", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Get loan request details
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<Map<String, Object>> getLoanRequest(@PathVariable Long requestId) {
        try {
            LoanRequestDTO loanRequest = loanService.getLoanRequest(requestId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", loanRequest);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching loan request", e);
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Approve loan request by corporate
     */
    @PutMapping("/request/{requestId}/approve")
    public ResponseEntity<Map<String, Object>> approveLoanRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            log.info("Approving loan request: {}", requestId);
            String approverUserId = userId != null ? userId : "SYSTEM";
            String approverName = userName != null ? userName : "System";

            loanService.approveLoanRequestByCoroperate(requestId, approverUserId, approverName, remarks);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan request approved successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error approving loan request", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Reject loan request by corporate.
     */
    @PutMapping("/request/{requestId}/reject")
    public ResponseEntity<Map<String, Object>> rejectLoanRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        try {
            String approverUserId = userId != null ? userId : "SYSTEM";
            String approverName = userName != null ? userName : "System";

            loanService.rejectLoanRequestByCorporate(requestId, approverUserId, approverName, remarks);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan request rejected successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error rejecting loan request", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Cancel loan request.
     */
    @PutMapping("/request/{requestId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelLoanRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            String cancelledBy = userId != null ? userId : "SYSTEM";

            loanService.cancelLoanRequest(requestId, cancelledBy, remarks);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan request cancelled successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling loan request", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Create loan account (disbursement)
     */
    @PostMapping("/{requestId}/disburse")
    public ResponseEntity<Map<String, Object>> disburseLoan(
            @PathVariable Long requestId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            log.info("Disbursing loan for request: {}", requestId);
            String createdBy = userId != null ? userId : "SYSTEM";

            loanService.createLoanAccount(requestId, createdBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan disbursed successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error disbursing loan", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Get loan requests waiting for bank approval.
     */
    @GetMapping("/bank/pending")
    public ResponseEntity<Map<String, Object>> getPendingBankLoans() {
        try {
            List<LoanRequestDTO> pendingLoans = loanService.getLoanRequests(null, null, "APPROVED_BY_CORP");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", pendingLoans.size());
            response.put("data", pendingLoans);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching pending bank loans", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Bank monitoring dashboard.
     */
    @GetMapping("/bank/summary")
    public ResponseEntity<Map<String, Object>> getBankSummary() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", loanService.getBankSummary());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching bank summary", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Get pending loan requests for a corporate
     */
    @GetMapping("/corporate/{corporateId}/pending")
    public ResponseEntity<Map<String, Object>> getPendingLoans(@PathVariable Long corporateId) {
        try {
            log.info("Fetching pending loans for corporate: {}", corporateId);

            List<LoanRequestDTO> pendingLoans = loanService.getPendingLoanRequestsForCorporate(corporateId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", pendingLoans.size());
            response.put("data", pendingLoans);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching pending loans", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Get active loans for an employee
     */
    @GetMapping("/employee/{employeeId}/active")
    public ResponseEntity<Map<String, Object>> getActiveLoans(@PathVariable Long employeeId) {
        try {
            log.info("Fetching active loans for employee: {}", employeeId);

            List<ActiveLoanDTO> activeLoans = loanService.getActiveLoansByEmployee(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", activeLoans.size());
            response.put("data", activeLoans);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching active loans", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Corporate reporting dashboard.
     */
    @GetMapping("/corporate/{corporateId}/summary")
    public ResponseEntity<Map<String, Object>> getCorporateSummary(@PathVariable Long corporateId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", loanService.getCorporateSummary(corporateId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching corporate summary", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Get repayment schedule for a loan.
     */
    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<Map<String, Object>> getRepaymentSchedule(@PathVariable Long loanId) {
        try {
            List<Map<String, Object>> schedules = loanService.getRepaymentSchedule(loanId)
                    .stream()
                    .map(this::mapSchedule)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", schedules.size());
            response.put("data", schedules);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching repayment schedule", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Recover loan from salary/designated account.
     */
    @PostMapping("/{loanId}/recover")
    public ResponseEntity<Map<String, Object>> recoverLoan(
            @PathVariable Long loanId,
            @RequestBody RepaymentDTO repaymentDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            repaymentDTO.setLoanId(loanId);
            loanService.recoverLoan(repaymentDTO, userId != null ? userId : "SYSTEM");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan recovery posted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recovering loan", e);
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Helper method for error responses
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);

        return ResponseEntity.status(status).body(response);
    }

    private Map<String, Object> mapSchedule(RepaymentSchedule schedule) {
        Map<String, Object> data = new HashMap<>();
        data.put("scheduleId", schedule.getScheduleId());
        data.put("loanId", schedule.getLoanAccount().getLoanId());
        data.put("installmentNo", schedule.getInstallmentNo());
        data.put("dueDate", schedule.getDueDate());
        data.put("principalDue", schedule.getPrincipalDue());
        data.put("chargeDue", schedule.getChargeDue());
        data.put("totalDue", schedule.getTotalDue());
        data.put("paidAmount", schedule.getPaidAmount());
        data.put("outstandingDue", schedule.getOutstandingDue());
        data.put("scheduleStatus", schedule.getScheduleStatus());
        return data;
    }
}
