package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.showroom.ShowroomCreateRequest;
import vn.springboot.dto.request.showroom.ShowroomSearchRequest;
import vn.springboot.dto.request.showroom.ShowroomUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.showroom.ShowroomResponse;
import vn.springboot.service.ShowroomService;

/**
 * Showroom endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/showrooms")
@RequiredArgsConstructor
public class ShowroomController {

    private final ShowroomService showroomService;

    @GetMapping
    public ApiResponse<PageResponse<ShowroomResponse>> search(
            @ModelAttribute ShowroomSearchRequest request) {
        return ApiResponse.success(showroomService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShowroomResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(showroomService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ShowroomResponse> create(
            @Valid @RequestBody ShowroomCreateRequest request) {
        return ApiResponse.success("Created successfully", showroomService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ShowroomResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ShowroomUpdateRequest request) {
        return ApiResponse.success("Updated successfully", showroomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        showroomService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
