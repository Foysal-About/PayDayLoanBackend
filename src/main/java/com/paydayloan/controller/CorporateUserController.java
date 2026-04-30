package com.paydayloan.controller;

import com.paydayloan.dto.CorporateUserDTO;
import com.paydayloan.entity.Corporate;
import com.paydayloan.entity.CorporateUser;
import com.paydayloan.exception.ApiResponse;
import com.paydayloan.repository.CorporateRepository;
import com.paydayloan.repository.CorporateUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/corporate-users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CorporateUserController {

    private final CorporateUserRepository corporateUserRepository;
    private final CorporateRepository corporateRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<CorporateUserDTO>> create(
            @RequestBody CorporateUserDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        CorporateUser corporateUser = toEntity(request, new CorporateUser());
        corporateUser.setCreatedBy(userId != null ? userId : "SYSTEM");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toDTO(corporateUserRepository.save(corporateUser))));
    }

    @GetMapping
    public ApiResponse<List<CorporateUserDTO>> list(
            @RequestParam(required = false) Long corporateId,
            @RequestParam(required = false) String status) {
        List<CorporateUser> users;
        if (corporateId != null) {
            users = corporateUserRepository.findByCorporateCorporateId(corporateId);
        } else if (status != null) {
            users = corporateUserRepository.findByStatus(status);
        } else {
            users = corporateUserRepository.findAll();
        }
        return ApiResponse.ok(users.stream().map(this::toDTO).toList());
    }

    @GetMapping("/{corporateUserId}")
    public ApiResponse<CorporateUserDTO> get(@PathVariable Long corporateUserId) {
        return ApiResponse.ok(toDTO(find(corporateUserId)));
    }

    @PutMapping("/{corporateUserId}")
    public ApiResponse<CorporateUserDTO> update(
            @PathVariable Long corporateUserId,
            @RequestBody CorporateUserDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        CorporateUser corporateUser = toEntity(request, find(corporateUserId));
        corporateUser.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(corporateUserRepository.save(corporateUser)));
    }

    @PatchMapping("/{corporateUserId}/status")
    public ApiResponse<CorporateUserDTO> updateStatus(
            @PathVariable Long corporateUserId,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        CorporateUser corporateUser = find(corporateUserId);
        corporateUser.setStatus(status);
        corporateUser.setUpdatedBy(userId != null ? userId : "SYSTEM");
        return ApiResponse.ok(toDTO(corporateUserRepository.save(corporateUser)));
    }

    private CorporateUser find(Long corporateUserId) {
        return corporateUserRepository.findById(corporateUserId)
                .orElseThrow(() -> new RuntimeException("Corporate user not found"));
    }

    private Corporate findCorporate(Long corporateId) {
        return corporateRepository.findById(corporateId)
                .orElseThrow(() -> new RuntimeException("Corporate not found"));
    }

    private CorporateUser toEntity(CorporateUserDTO dto, CorporateUser entity) {
        if (dto.getCorporateId() != null) {
            entity.setCorporate(findCorporate(dto.getCorporateId()));
        }
        entity.setUserLoginId(dto.getUserLoginId());
        entity.setUserName(dto.getUserName());
        entity.setEmail(dto.getEmail());
        entity.setMobileNo(dto.getMobileNo());
        entity.setRoleCode(dto.getRoleCode());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        return entity;
    }

    private CorporateUserDTO toDTO(CorporateUser entity) {
        return CorporateUserDTO.builder()
                .corporateUserId(entity.getCorporateUserId())
                .corporateId(entity.getCorporate().getCorporateId())
                .userLoginId(entity.getUserLoginId())
                .userName(entity.getUserName())
                .email(entity.getEmail())
                .mobileNo(entity.getMobileNo())
                .roleCode(entity.getRoleCode())
                .status(entity.getStatus())
                .build();
    }
}
