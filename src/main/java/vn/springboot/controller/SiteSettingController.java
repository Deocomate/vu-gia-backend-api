package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.setting.SiteSettingUpdateRequest;
import vn.springboot.dto.response.setting.SiteSettingResponse;
import vn.springboot.service.SiteSettingService;

/**
 * Site-wide settings. Reads are public (storefront polls on load); the
 * {@code cartEnabled} switch is a high-impact global toggle, so writes are
 * restricted to {@code SUPERADMIN} only (matching {@code UserController.changeRole}).
 */
@RestController
@RequestMapping("/api/site-settings")
@RequiredArgsConstructor
public class SiteSettingController {

    private final SiteSettingService siteSettingService;

    @GetMapping
    public ApiResponse<SiteSettingResponse> get() {
        return ApiResponse.success(siteSettingService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ApiResponse<SiteSettingResponse> update(@Valid @RequestBody SiteSettingUpdateRequest request) {
        return ApiResponse.success("Updated successfully", siteSettingService.updateSettings(request));
    }
}
