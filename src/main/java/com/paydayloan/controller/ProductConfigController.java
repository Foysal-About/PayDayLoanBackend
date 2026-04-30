package com.paydayloan.controller;

import com.paydayloan.dto.ProductConfigDTO;
import com.paydayloan.entity.ProductConfig;
import com.paydayloan.exception.ApiResponse;
import com.paydayloan.repository.ProductConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductConfigController {

    private final ProductConfigRepository productConfigRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductConfigDTO>> create(
            @RequestBody ProductConfigDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ProductConfig productConfig = toEntity(request, new ProductConfig());
        productConfig.setCreatedBy(userId != null ? userId : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toDTO(productConfigRepository.save(productConfig))));
    }

    @GetMapping
    public ApiResponse<List<ProductConfigDTO>> list(@RequestParam(required = false) String status) {
        List<ProductConfig> products = status == null
                ? productConfigRepository.findAll()
                : productConfigRepository.findByStatus(status);
        return ApiResponse.ok(products.stream().map(this::toDTO).toList());
    }

    @GetMapping("/{productConfigId}")
    public ApiResponse<ProductConfigDTO> get(@PathVariable Long productConfigId) {
        return ApiResponse.ok(toDTO(find(productConfigId)));
    }

    @PutMapping("/{productConfigId}")
    public ApiResponse<ProductConfigDTO> update(
            @PathVariable Long productConfigId,
            @RequestBody ProductConfigDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ProductConfig productConfig = toEntity(request, find(productConfigId));
        productConfig.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(productConfigRepository.save(productConfig)));
    }

    @PatchMapping("/{productConfigId}/status")
    public ApiResponse<ProductConfigDTO> updateStatus(
            @PathVariable Long productConfigId,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        ProductConfig productConfig = find(productConfigId);
        productConfig.setStatus(status);
        productConfig.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(productConfigRepository.save(productConfig)));
    }

    private ProductConfig find(Long productConfigId) {
        return productConfigRepository.findById(productConfigId)
                .orElseThrow(() -> new RuntimeException("Product config not found"));
    }

    private ProductConfig toEntity(ProductConfigDTO dto, ProductConfig entity) {
        entity.setProductCode(dto.getProductCode());
        entity.setProductName(dto.getProductName());
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
        if (dto.getMinRequestAmount() != null) {
            entity.setMinRequestAmount(dto.getMinRequestAmount());
        }
        entity.setMaxRequestAmount(dto.getMaxRequestAmount());
        if (dto.getRepaymentDueDays() != null) {
            entity.setRepaymentDueDays(dto.getRepaymentDueDays());
        }
        if (dto.getEmployerApprovalRequiredYn() != null) {
            entity.setEmployerApprovalRequiredYn(dto.getEmployerApprovalRequiredYn());
        }
        if (dto.getAutoDisbursYn() != null) {
            entity.setAutoDisbursYn(dto.getAutoDisbursYn());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setEffectiveFrom(dto.getEffectiveFrom());
        entity.setEffectiveTo(dto.getEffectiveTo());
        return entity;
    }

    private ProductConfigDTO toDTO(ProductConfig entity) {
        return ProductConfigDTO.builder()
                .productConfigId(entity.getProductConfigId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .maxEligiblePercent(entity.getMaxEligiblePercent())
                .serviceChargePercent(entity.getServiceChargePercent())
                .minServiceCharge(entity.getMinServiceCharge())
                .maxActiveLoanPerEmp(entity.getMaxActiveLoanPerEmp())
                .minRequestAmount(entity.getMinRequestAmount())
                .maxRequestAmount(entity.getMaxRequestAmount())
                .repaymentDueDays(entity.getRepaymentDueDays())
                .employerApprovalRequiredYn(entity.getEmployerApprovalRequiredYn())
                .autoDisbursYn(entity.getAutoDisbursYn())
                .status(entity.getStatus())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .build();
    }
}
