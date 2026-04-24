package com.paydayloan.controller;

import com.paydayloan.dto.ActiveLoanDTO;
import com.paydayloan.dto.LoanRequestDTO;
import com.paydayloan.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/loans")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoanController {

    private final LoanService loanService;

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

    // Helper method for error responses
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);

        return ResponseEntity.status(status).body(response);
    }
}
