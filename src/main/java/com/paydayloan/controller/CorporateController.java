package com.paydayloan.controller;

import com.paydayloan.dto.CorporateDTO;
import com.paydayloan.entity.Corporate;
import com.paydayloan.exception.ApiResponse;
import com.paydayloan.repository.CorporateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/corporates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CorporateController {

    private final CorporateRepository corporateRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<CorporateDTO>> create(
            @RequestBody CorporateDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Corporate corporate = toEntity(request, new Corporate());
        corporate.setCreatedBy(userId != null ? userId : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toDTO(corporateRepository.save(corporate))));
    }

    @GetMapping
    public ApiResponse<List<CorporateDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String agreementStatus) {
        List<Corporate> corporates;
        if (status != null) {
            corporates = corporateRepository.findByStatus(status);
        } else if (agreementStatus != null) {
            corporates = corporateRepository.findByAgreementStatus(agreementStatus);
        } else {
            corporates = corporateRepository.findAll();
        }
        return ApiResponse.ok(corporates.stream().map(this::toDTO).toList());
    }

    @GetMapping("/{corporateId}")
    public ApiResponse<CorporateDTO> get(@PathVariable Long corporateId) {
        return ApiResponse.ok(toDTO(find(corporateId)));
    }

    @PutMapping("/{corporateId}")
    public ApiResponse<CorporateDTO> update(
            @PathVariable Long corporateId,
            @RequestBody CorporateDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Corporate corporate = toEntity(request, find(corporateId));
        corporate.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(corporateRepository.save(corporate)));
    }

    @PatchMapping("/{corporateId}/status")
    public ApiResponse<CorporateDTO> updateStatus(
            @PathVariable Long corporateId,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Corporate corporate = find(corporateId);
        corporate.setStatus(status);
        corporate.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(corporateRepository.save(corporate)));
    }

    private Corporate find(Long corporateId) {
        return corporateRepository.findById(corporateId)
                .orElseThrow(() -> new RuntimeException("Corporate not found"));
    }

    private Corporate toEntity(CorporateDTO dto, Corporate entity) {
        entity.setCorporateCode(dto.getCorporateCode());
        entity.setCorporateName(dto.getCorporateName());
        entity.setShortName(dto.getShortName());
        entity.setCustomerId(dto.getCustomerId());
        if (dto.getAgreementStatus() != null) {
            entity.setAgreementStatus(dto.getAgreementStatus());
        }
        entity.setAgreementDate(dto.getAgreementDate());
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
        if (dto.getGuaranteeMode() != null) {
            entity.setGuaranteeMode(dto.getGuaranteeMode());
        }
        if (dto.getMaxEligiblePercent() != null) {
            entity.setMaxEligiblePercent(dto.getMaxEligiblePercent());
        }
        if (dto.getServiceChargePercent() != null) {
            entity.setServiceChargePercent(dto.getServiceChargePercent());
        }
        if (dto.getMinServiceCharge() != null) {
            entity.setMinServiceCharge(dto.getMinServiceCharge());
        }
        if (dto.getMaxActiveLoanPerEmp() != null) {
            entity.setMaxActiveLoanPerEmp(dto.getMaxActiveLoanPerEmp());
        }
        if (dto.getRepaymentMode() != null) {
            entity.setRepaymentMode(dto.getRepaymentMode());
        }
        if (dto.getAutoDisbursYn() != null) {
            entity.setAutoDisbursYn(dto.getAutoDisbursYn());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setRemarks(dto.getRemarks());
        return entity;
    }

    private CorporateDTO toDTO(Corporate entity) {
        return CorporateDTO.builder()
                .corporateId(entity.getCorporateId())
                .corporateCode(entity.getCorporateCode())
                .corporateName(entity.getCorporateName())
                .shortName(entity.getShortName())
                .customerId(entity.getCustomerId())
                .agreementStatus(entity.getAgreementStatus())
                .agreementDate(entity.getAgreementDate())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .guaranteeMode(entity.getGuaranteeMode())
                .maxEligiblePercent(entity.getMaxEligiblePercent())
                .serviceChargePercent(entity.getServiceChargePercent())
                .minServiceCharge(entity.getMinServiceCharge())
                .maxActiveLoanPerEmp(entity.getMaxActiveLoanPerEmp())
                .repaymentMode(entity.getRepaymentMode())
                .autoDisbursYn(entity.getAutoDisbursYn())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .build();
    }
}
